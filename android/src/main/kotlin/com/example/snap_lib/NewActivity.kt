package com.example.snap_lib
// Import statements for Android and Compose
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable

// Import statements for Camera functionality
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.camera.core.Preview as CameraPreview
import androidx.camera.core.CameraSelector
import androidx.compose.ui.platform.LocalLifecycleOwner

// Import statements for Compose UI elements
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

// Import statements for Android permissions and logging
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission

class NewActivity : AppCompatActivity() {

    private val CAMERA_REQUEST_CODE = 2001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Greeting("Android", this)
                     CameraPreview()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setContent {
                MaterialTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        Greeting("Android", this@NewActivity)
                         CameraPreview()
                    }
                }
            }
        }
    }
}

@Composable
fun CameraPreview(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = CameraPreview.Builder().build()
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview
                )

                preview.setSurfaceProvider(previewView.surfaceProvider)
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
    )
}

@Composable
fun Greeting(name: String, activity: AppCompatActivity) {
    Column {
        Text(text = "Hello Pond $name!")
        Button(onClick = {
            println("Button clicked. Checking camera permission.")
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                println("Camera permission not granted. Requesting permission.")
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA), 2001)
            } else {
                println("Camera permission already granted.")
            }
        }) {
            Text(text = "Request Camera Access")
        }
    }
}

private fun checkPermissions(activity: AppCompatActivity) {
    if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA), 0)
    }
}

private fun checkAndRequestCameraPermission(activity: AppCompatActivity, requestCode: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.d("NativeDemo", "Permission not granted. Requesting permission.")
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA), requestCode)
        } else {
            Log.d("NativeDemo", "Permission already granted. Proceeding with photo capture.")
            // capturePhoto()
        }
    }
}


