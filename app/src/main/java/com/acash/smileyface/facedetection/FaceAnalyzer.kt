package com.acash.smileyface.facedetection

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Environment.DIRECTORY_DCIM
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.acash.smileyface.R
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.android.synthetic.main.activity_face_detection.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class FaceAnalyzer(private val context: Context, private val imageCapture: ImageCapture,private val imageAnalysis: ImageAnalysis) :
    ImageAnalysis.Analyzer {

    companion object{
        lateinit var bitmap: Bitmap
    }

    private val faceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .build()
    )

    private fun createNextPhoto(): Pair<Uri, OutputStream> {
        val displayName = "IMG_${SimpleDateFormat("yyyyMMdd_hhmmss").format(Date())}"
        val fileName = "SmileyFace" + File.separator +
                displayName + ".jpg"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        DIRECTORY_DCIM + File.separator + fileName
                )
            }
            val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val fileUri = context.contentResolver.insert(contentUri, contentValues)
            return Pair(fileUri!!, context.contentResolver.openOutputStream(fileUri)!!)
        } else {
            val dir = Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM)
            val photoDir = File(dir, "SmileyFace")
            if (!photoDir.exists()) {
                photoDir.mkdirs()
            }
            val file = File(photoDir, "$displayName.jpg")
            return Pair(file.toUri(), FileOutputStream(file))
        }
    }

    private fun takePicture() {
        val outputDetails = createNextPhoto()
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(outputDetails.second).build()

        imageCapture.takePicture(
            outputFileOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,outputDetails.first)
                        context.sendBroadcast(mediaScanIntent)
                    }
                    bitmap = BitmapFactory.decodeFile(outputDetails.first.path)
                    context.startActivity(Intent(context, CapturedPicActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("Save Image", "Error Saving Image!Try Again $exception")
                }
            })
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        imageProxy.image?.let {
            val inputImage = InputImage.fromMediaImage(it, imageProxy.imageInfo.rotationDegrees)
            faceDetector.process(inputImage)
                .addOnSuccessListener { faces ->
                    for(face in faces){
                        if (face.smilingProbability!=null) {
                            if(face.smilingProbability!! >=0.7f) {
                                imageAnalysis.clearAnalyzer()
                                (context as Activity).tv.apply {
                                    visibility = View.VISIBLE
                                    text = context.getString(R.string.perfect)
                                }
                                takePicture()
                            }else{
                                (context as Activity).tv.apply {
                                    visibility = View.VISIBLE
                                    text = context.getString(R.string.smile_harder)
                                }
                            }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("Face Detection", "Error Detecting Face", exception)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } ?: imageProxy.close()
    }
}