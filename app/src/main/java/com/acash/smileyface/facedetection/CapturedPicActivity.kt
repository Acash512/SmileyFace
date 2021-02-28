package com.acash.smileyface.facedetection

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.acash.smileyface.R
import kotlinx.android.synthetic.main.activity_captured_pic.*

class CapturedPicActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_captured_pic)
        capturedImg.setImageBitmap(FaceAnalyzer.bitmap)

        newPicButton.setOnClickListener{
            startActivity(Intent(this,FaceDetectionActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

}