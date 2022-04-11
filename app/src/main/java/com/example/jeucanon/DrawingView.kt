package com.example.jeucanon

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.os.Bundle
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.MotionEvent
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity

class DrawingView @JvmOverloads constructor (context: Context, attributes: AttributeSet? = null, defStyleAttr: Int = 0): SurfaceView(context, attributes,defStyleAttr), SurfaceHolder.Callback, Runnable {
    lateinit var canvas: Canvas
    val backgroundPaint = Paint()
    val textPaint = Paint()
    var screenWidth = 0f
    var screenHeight = 0f
    var drawing = false
    lateinit var thread: Thread
    val canon = Canon(0f, 0f, 0f, 0f, this)
    val obstacle = Obstacle(0f, 0f, 0f, 0f, 0f, this)
    val cible = Cible(0f, 0f, 0f, 0f, 0f,  this)
    val balle = BalleCanon(this, obstacle, cible)
    var shotsFired: Int = 0
    var timeLeft = 0.0
    var MISS_PENALTY = 2 //temps que l'on perd à chac choc sur l'obstacle
    var HIT_REWARD = 3 // bonus de temps quand on abbat une cible
    var gameOver = false
    val activity = context as FragmentActivity
    var totalElapsedTime = 0.0
    var originalTime = 10.0
    var currentTime = 0.0

    init {
        backgroundPaint.color = Color.GREEN
        textPaint.textSize = screenWidth/20
        textPaint.color = Color.BLACK
        timeLeft = 10.0
    }

    fun pause() {
        drawing = false
        thread.join()
    }

    fun resume() {
        drawing = true
        thread = Thread(this)
        thread.start()
    }

    override fun run() { //gere la micro-tâche et doit s'assurer de ses déplacements
        var previousFrameTime = System.currentTimeMillis()
        var elapsedTimeMS:Double=(currentTime-previousFrameTime).toDouble()
        totalElapsedTime += elapsedTimeMS / 1000.0
        while (drawing) {
            val currentTime = System.currentTimeMillis()
            var elapsedTimeMS = (currentTime-previousFrameTime).toDouble()
            updatePositions(elapsedTimeMS)
            draw()
            previousFrameTime = currentTime
        }
    }

    override fun onSizeChanged(w:Int, h:Int, oldw:Int, oldh:Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        screenWidth = w.toFloat()
        screenHeight = h.toFloat()

        canon.canonBaseRadius = (h / 32f)
        canon.canonLongueur = (w / 8f)
        canon.largeur = (w / 32f)
        canon.setFinCanon(h / 2f)
        canon.setFinCanon(h / 2f)

        balle.canonballRadius= (w / 32f)
        balle.canonballVitesse = (w * 3 / 2f)

        obstacle.obstacleDistance = (w * 5 / 8f)
        obstacle.obstacleDebut = (h / 8f)
        obstacle.obstacleFin = (h * 4 / 8f)
        obstacle.width = (w / 15f)
        obstacle.initialObstacleVitesse= (h / 2f)
        obstacle.setRect()

        cible.width = (w / 24f)
        cible.cibleDistance= (w * 7 / 8f)
        cible.cibleDebut = (h / 8f)
        cible.cibleFin = (h * 7 / 8f)
        cible.cibleVitesseInitiale = (-h / 4f)
        cible.setRect()

        textPaint.setTextSize(w / 20f)
        textPaint.isAntiAlias = true
    }

    fun draw() {
        if (holder.surface.isValid) {
            canvas = holder.lockCanvas()
            canvas.drawRect(0f, 0f, canvas.width.toFloat(),
                canvas.height.toFloat(), backgroundPaint)
            val formatted = String.format("%.2f", timeLeft)
            canvas.drawText("Il reste $formatted secondes", 30f, 50f, textPaint)
            canon.draw(canvas)
            if (balle.canonballOnScreen) balle.draw(canvas)
            obstacle.draw(canvas)
            cible.draw(canvas)
            holder.unlockCanvasAndPost(canvas)
        }
    }

    override fun onTouchEvent(e: MotionEvent): Boolean { // MotionEvent : retransmet la localisation du click de souris
        val action = e.action
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            fireCanonball(e)
        }
        return true
    }

    fun fireCanonball(event: MotionEvent) {
        if (! balle.canonballOnScreen) { //ne s'exécute que si aucune balle n'est présente à l'écran
            val angle = alignCanon(event) // calcul de l'angle du canon
            balle.launch(angle)
            ++shotsFired // incrémente le nombre de coups tirés
        }
    }

    fun alignCanon(event: MotionEvent): Double { //récupère les données du clic et s'en sert pour calculer l'angle du tir
        val touchPoint = Point(event.x.toInt(), event.y.toInt())
        val centerMinusY = screenHeight / 2 - touchPoint.y
        var angle = 0.0
        if (centerMinusY != 0.0f)
            angle = Math.atan((touchPoint.x).toDouble()/ centerMinusY)
        if (touchPoint.y > screenHeight / 2)
            angle += Math.PI
        canon.align(angle)
        return angle
    }

    fun updatePositions(elapsedTimeMS: Double) { // Pour que les deux composants entament leur va-et-vient vertical
        val interval = elapsedTimeMS / 1000.0 // temps qui s'écoule entre 2 exécutions de la micro-tâche
        timeLeft -= interval // diminue le timer a chaque dixieme de seconde qui passe
        obstacle.update(interval)
        cible.update(interval)
        balle.update(interval)

        if (timeLeft <= 0.0) { // quand la partie est perdue
            timeLeft = 0.0
            gameOver = true
            drawing = false
            showGameOverDialog(R.string.lose)
        }

    }

    fun reduceTimeLeft() {
        timeLeft -= MISS_PENALTY
    }

    fun increaseTimeLeft(){
        timeLeft += HIT_REWARD
    }

    fun gameOver() { // quand la partie est gagnée
        drawing = false
        showGameOverDialog(R.string.win)
        originalTime -= 1
        gameOver = true
    }

    fun showGameOverDialog() {
        class GameResult: DialogFragment() {
            override fun onCreateDialog(bundle: Bundle?): Dialog {
                val builder = AlertDialog.Builder(getActivity())
                builder.setTitle(resources.getString(messageId))  //titre de la bulle de dialogue
                builder.setMessage( //message transmis
                    resources.getString(
                        R.string.results_format, shotsFired, totalElapsedTime // déclaration du temps restant à la fin de la partie
                    )
                )
                builder.setPositiveButton(R.string.reset_game, // activation du boutton à texte
                    DialogInterface.OnClickListener { _, _->newGame()}
                )
                return builder.create()
            }
        }
        activity.runOnUiThread(
            Runnable {
                val ft = activity.supportFragmentManager.beginTransaction() // pour faire apparaitre le fragment
                val prev =
                    activity.supportFragmentManager.findFragmentByTag("dialog")
                if (prev != null) {
                    ft.remove(prev)
                }
                ft.addToBackStack(null)
                val gameResult = GameResult()
                gameResult.setCancelable(false)
                gameResult.show(ft,"dialog") // fait apparaitre la fenetre
        )
    }}

    fun newGame() {
        cible.resetCible()
        obstacle.resetObstacle()
        timeLeft = originalTime
        balle.resetCanonBall()
        shotsFired = 0
        totalElapsedTime = 0.0
        drawing = true
        if (gameOver) {
            gameOver = false
            thread = Thread(this)
            thread.start()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int,
                                width: Int, height: Int) {}

    override fun surfaceCreated(holder: SurfaceHolder) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {}
}