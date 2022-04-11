package com.example.jeucanon

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.jeucanon.DrawingView
import com.example.jeucanon.R
import java.util.*

class MainActivity: AppCompatActivity() {

    lateinit var drawingView: DrawingView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawingView = findViewById(R.id.vMain)
    }

    override fun onPause() {
        super.onPause()
        drawingView.pause()
    }

    override fun onResume() {
        super.onResume()
        drawingView.resume()
    }
}