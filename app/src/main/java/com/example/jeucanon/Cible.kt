package com.example.jeucanon

import android.graphics.*

class Cible (var cibleDistance: Float, var cibleDebut: Float, var cibleFin: Float, var cibleVitesseInitiale: Float, var width: Float, var view: DrawingView) {
    val CIBLE_PIECES = 11
    val cible = RectF(cibleDistance, cibleDebut, cibleDistance + width, cibleFin)
    var cibleTouchee = BooleanArray(CIBLE_PIECES)
    val ciblePaint = Paint()
    var longueurPiece = 0f
    var cibleVitesse = cibleVitesseInitiale
    var nbreCiblesTouchees = 0
    var acc = 2f
    fun draw(canvas: Canvas) {
        val currentPoint = PointF()
        currentPoint.x = cible.left
        currentPoint.y = cible.top
        for (i in 0 until CIBLE_PIECES) {
            if (!cibleTouchee[i]) {
                if (i % 2 != 0)
                    ciblePaint.color = Color.BLUE
                else
                    ciblePaint.color = Color.YELLOW
                canvas.drawRect(currentPoint.x,currentPoint.y,cible.right,
                    currentPoint.y+longueurPiece,ciblePaint)
            }
            currentPoint.y += longueurPiece
        }
    }
    fun setRect() {
        cible.set(cibleDistance, cibleDebut,
            cibleDistance + width, cibleFin)
        cibleVitesse = cibleVitesseInitiale
        longueurPiece = (cibleFin - cibleDebut) / CIBLE_PIECES
    }

    fun update(interval: Double) { // Change la vitesse des cilbes en fonction de leur position
        var up = (interval * cibleVitesse).toFloat()
        cible.offset(0f, up)
        if (cible.top < 0 || cible.bottom > view.screenHeight) {
            cibleVitesse *= -1f
            up = (interval * 3 * cibleVitesse).toFloat()
            cible.offset(0f, up)
        }
    }

    fun detectChoc(balle: BalleCanon) {
        val section=((balle.canonball.y-cible.top)/longueurPiece).toInt()
        if (section>=0 && section<CIBLE_PIECES && !cibleTouchee[section]) {
            cibleTouchee[section] = true
            balle.resetCanonBall()
            view.increaseTimeLeft()
            view.playCibleSound()
        }
        if (++nbreCiblesTouchees == CIBLE_PIECES) view.gameOver()
    }

    fun resetCible() {
        for (n in 1..CIBLE_PIECES) {
            if (cibleTouchee[n] == true) cibleTouchee[n] = false
        }
        nbreCiblesTouchees = 0
        cibleVitesse = cibleVitesseInitiale
        cible.set(cibleDistance, cibleDebut, cibleDistance+width, cibleFin)
    }
}