package com.example.snap_lib

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat

class CameraActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val imageView = ImageView(this)
        val captureButton = Button(this).apply {
            text = "Capture"
            setOnClickListener {
                val resultIntent = Intent()
                resultIntent.putExtra("imagePath", "path_to_image.jpg")
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }

        setContentView(imageView)
    }
}