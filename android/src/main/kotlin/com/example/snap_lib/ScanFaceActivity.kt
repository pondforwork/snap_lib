package com.example.snap_lib

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanFaceActivity : ComponentActivity() {
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    FaceDetectionScreen()
                }
            }
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}

@Composable
fun FaceDetectionScreen() {
    val context = LocalContext.current
    var detectedFace by remember { mutableStateOf<Face?>(null) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val executor = ContextCompat.getMainExecutor(context)

    val previewView = androidx.camera.view.PreviewView(context)
    val imageAnalyzer = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()

    val faceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .build()
    )

    imageAnalyzer.setAnalyzer(executor) { imageProxy ->
//        val mediaImage = imageProxy.image
//        if (mediaImage != null) {
//            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
//            faceDetector.process(image)
//                .addOnSuccessListener { faces ->
//                    detectedFace = faces.firstOrNull() // Use the first detected face
//                    imageProxy.close()
//                }
//                .addOnFailureListener { e ->
//                    Log.e("FaceDetection", "Face detection failed", e)
//                    imageProxy.close()
//                }
//        } else {
//            imageProxy.close()
//        }
    }

    LaunchedEffect(Unit) {
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    context as ComponentActivity,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview, imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e("CameraX", "Use case binding failed", exc)
            }
        }, executor)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        CameraOverlay(
            guideText = if (detectedFace != null) "ถือค้างไว้" else "ให้ใบหน้าอยู่ในกรอบที่กำหนด",
            instructionText = "ไม่มีปิดตา จมูก ปาก และคาง",
            borderColorSuccess = if (detectedFace != null) Color.Green else Color.Red
        )
    }
}

@Composable
fun CameraOverlay(
    modifier: Modifier = Modifier,
    guideText: String = "ให้ใบหน้าอยู่ในกรอบที่กำหนด",
    instructionText: String = "ไม่มีปิดตา จมูก ปาก และคาง",
    borderColorSuccess: Color = Color.Green,
    borderColorDefault: Color = Color.Red,
    borderWidth: Float = 8f,
    guideTextStyle: TextStyle = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Yellow),
    instructionTextStyle: TextStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.Cyan),
) {
    val borderColor by animateColorAsState(
        targetValue = if (guideText == "ถือค้างไว้") borderColorSuccess else borderColorDefault,
        animationSpec = tween(durationMillis = 500)
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val ovalWidth = canvasWidth * 0.7f
            val ovalHeight = canvasHeight * 0.5f
            val ovalLeft = (canvasWidth - ovalWidth) / 2
            val ovalTop = (canvasHeight - ovalHeight) / 2

            drawRect(
                color = Color.Black.copy(alpha = 0.6f),
                size = size
            )

            drawOval(
                color = Color.Transparent,
                topLeft = Offset(ovalLeft, ovalTop),
                size = Size(ovalWidth, ovalHeight),
                blendMode = BlendMode.Clear
            )

            drawOval(
                color = borderColor,
                topLeft = Offset(ovalLeft, ovalTop),
                size = Size(ovalWidth, ovalHeight),
                style = Stroke(width = borderWidth.dp.toPx())
            )
        }

        BasicText(
            text = guideText,
            style = guideTextStyle,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        )

        BasicText(
            text = instructionText,
            style = instructionTextStyle.copy(color = Color.Magenta),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}
