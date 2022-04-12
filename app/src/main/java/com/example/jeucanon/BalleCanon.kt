package com.example.jeucanon

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF

class BalleCanon (var view: DrawingView, val obstacle: Obstacle, val cible: Cible) {
    var canonball = PointF()
    var canonballVitesse = 0f
    var canonballVitesseX = 0f
    var canonballVitesseY = 0f
    var canonballOnScreen = true
    var canonballRadius = 0f
    var canonballPaint = Paint()

    init {
        canonballPaint.color = Color.RED
    }

    fun launch(angle: Double) {
        canonball.x = canonballRadius
        canonball.y = view.screenHeight / 2f
        canonballVitesseX=(canonballVitesse*Math.sin(angle)).toFloat()
        canonballVitesseY=(-canonballVitesse*Math.cos(angle)).toFloat()
        canonballOnScreen = true
    }

    fun draw(canvas: Canvas) {
        canvas.drawCircle(canonball.x, canonball.y, canonballRadius, canonballPaint)
    }

    fun update(interval: Double) {
        if (canonballOnScreen) {
            canonball.x += (interval * canonballVitesseX).toFloat()
            canonball.y += (interval * canonballVitesseY).toFloat()
            /* Vérifions si la balle touche l'obstacle ou pas */
            if (canonball.x + canonballRadius > obstacle.obstacle.left
                && canonball.y + canonballRadius > obstacle.obstacle.top
                && canonball.y - canonballRadius < obstacle.obstacle.bottom
            ) {
                canonballVitesseX *= -1 // si la balle touche l'obstacle, elle est renvoyée dans l'autre sens
                canonball.offset((3 * canonballVitesseX * interval).toFloat(), 0f)
                view.reduceTimeLeft()
                view.playObstacleSound()
            }
            // Si elle sort de l'écran
            else if (canonball.x + canonballRadius > view.screenWidth
                || canonball.x - canonballRadius < 0
            ) {
                canonballOnScreen = false
            } else if (canonball.y + canonballRadius > view.screenHeight
                || canonball.y - canonballRadius < 0
            ) {
                canonballOnScreen = false
            } else if (canonball.x + canonballRadius > cible.cible.left
                && canonball.y + canonballRadius > cible.cible.top
                && canonball.y - canonballRadius < cible.cible.bottom
            ) {
                cible.detectChoc(this) // provoque l'action si la balle touche la cible

            }
        }
    }
    fun resetCanonBall() {
        canonballOnScreen = false
    }
}