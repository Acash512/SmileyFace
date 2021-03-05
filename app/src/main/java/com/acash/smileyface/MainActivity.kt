package com.acash.smileyface

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import com.acash.smileyface.facedetection.FaceDetectionActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity() : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        object:CountDownTimer(5000, 1000){
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                this@MainActivity.startActivity(Intent(this@MainActivity, FaceDetectionActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK ))
            }
        }.start()

    }
}
