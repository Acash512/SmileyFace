package com.acash.smileyface.facedetection

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.acash.smileyface.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_face_detection.*

const val CAMERA_PERMISSIONS_REQUEST_CODE = 1234

class FaceDetectionActivity : AppCompatActivity() {

    private var cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
    private lateinit var imageAnalysis: ImageAnalysis
    private lateinit var imageAnalyzer: ImageAnalysis.Analyzer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_detection)
        checkCameraPermissions()

        btnChangeLens.setOnClickListener {
            cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA)
                CameraSelector.DEFAULT_BACK_CAMERA
            else CameraSelector.DEFAULT_FRONT_CAMERA

            startCamera()
        }
    }

    private fun checkCameraPermissions() {
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else requestPermissions(arrayOf(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE), CAMERA_PERMISSIONS_REQUEST_CODE)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(
            {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder()
                    .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                    .build()

                preview.setSurfaceProvider(previewView.surfaceProvider)

                val imageCapture = ImageCapture.Builder()
                    .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .build()

                imageAnalysis = ImageAnalysis.Builder().build()

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalysis)

                imageAnalyzer = FaceAnalyzer(this,imageCapture,imageAnalysis)
                startScanner()

            },
            ContextCompat.getMainExecutor(this)
        )
    }

    private fun startScanner() {
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this),imageAnalyzer)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == CAMERA_PERMISSIONS_REQUEST_CODE) {
            for(result in grantResults){
                if(result == PackageManager.PERMISSION_DENIED) {
                    MaterialAlertDialogBuilder(this).apply {
                        setTitle("Permission Error")
                        setMessage("Cannot Proceed Without Permissions")
                        setPositiveButton("OK") { _, _ ->
                            finish()
                        }
                        setCancelable(false)
                    }.show()
                }
            }
            startCamera()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}