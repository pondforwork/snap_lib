package com.example.snap_lib

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

class ScanFaceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val cameraPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                setCameraContent(getOverlaySettings())
            } else {
                Log.e("CameraX", "Camera permission denied")
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED) {
            setCameraContent(getOverlaySettings())
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    /** ✅ Extract all intent extras into a function */
    private fun getOverlaySettings(): OverlaySettings {
        return OverlaySettings(
            guideText = intent.getStringExtra("guideText") ?: "ให้ใบหน้าอยู่ในกรอบที่กำหนด",
            instructionText = intent.getStringExtra("instructionText") ?: "ไม่มีปิดตา จมูก ปาก และคาง",
            successText = intent.getStringExtra("successText") ?: "ถือค้างไว้",
            borderColorSuccess = intent.getIntExtra("borderColorSuccess", 0xFF00FF00.toInt()),
            borderColorDefault = intent.getIntExtra("borderColorDefault", 0xFFFF0000.toInt()),
            textColorDefault = intent.getIntExtra("textColorDefault", 0xFFFFFFFF.toInt()),
            textColorSuccess = intent.getIntExtra("textColorSuccess", 0xFF00FF00.toInt()),
            guideFontSize = intent.getFloatExtra("guideFontSize", 24f),
            instructionFontSize = intent.getFloatExtra("instructionFontSize", 20f),
            guideTextColor = intent.getIntExtra("guideTextColor", 0xFFFFFF00.toInt()),
            instructionTextColor = intent.getIntExtra("instructionTextColor", 0x00FFFF.toInt())
        )
    }

    private fun setCameraContent(settings: OverlaySettings) {
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.ui.graphics.Color.Black
                ) {
                    FaceDetectionScreen(settings)
                }
            }
        }
    }
}

/** ✅ Create a data class to hold overlay settings */
data class OverlaySettings(
    val guideText: String,
    val instructionText: String,
    val successText: String,
    val borderColorSuccess: Int,
    val borderColorDefault: Int,
    val textColorDefault: Int,
    val textColorSuccess: Int,
    val guideFontSize: Float,
    val instructionFontSize: Float,
    val guideTextColor: Int,
    val instructionTextColor: Int
)
@Composable
fun FaceDetectionScreen(settings: OverlaySettings) {
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val executor = ContextCompat.getMainExecutor(context)

    val previewView = remember {
        androidx.camera.view.PreviewView(context).apply {
            layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
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
                    preview
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

        // ✅ Overlay with settings
        CameraOverlay(
            guideText = settings.guideText,
            instructionText = settings.instructionText,
            borderColorSuccess = Color(settings.borderColorSuccess),
            borderColorDefault = Color(settings.borderColorDefault),
            guideTextStyle = TextStyle(
                fontSize = settings.guideFontSize.sp,
                fontWeight = FontWeight.Bold,
                color = Color(settings.guideTextColor)
            ),
            instructionTextStyle = TextStyle(
                fontSize = settings.instructionFontSize.sp,
                fontWeight = FontWeight.Medium,
                color = Color(settings.instructionTextColor)
            )
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
    borderWidth: Dp = 8.dp,

    // ✅ Customizable Guide Text
    guideTextStyle: TextStyle = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
    ),
    guideTextAlignment: Alignment = Alignment.TopCenter,
    guideTextPadding: PaddingValues = PaddingValues(top = 16.dp),

    // ✅ Customizable Instruction Text
    instructionTextStyle: TextStyle = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        color = Color.White
    ),
    instructionTextAlignment: Alignment = Alignment.BottomCenter,
    instructionTextPadding: PaddingValues = PaddingValues(bottom = 16.dp)
) {
    val borderColor by animateColorAsState(
        targetValue = if (guideText == "ถือค้างไว้") borderColorSuccess else borderColorDefault,
        animationSpec = tween(durationMillis = 500)
    )

    val textColor by animateColorAsState(
        targetValue = if (guideText == "ถือค้างไว้") Color.Green else Color.White,
        animationSpec = tween(durationMillis = 500)
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val screenWidth = size.width
            val screenHeight = size.height

            val ovalWidth = screenWidth * 0.7f
            val ovalHeight = screenHeight * 0.5f
            val ovalLeft = (screenWidth - ovalWidth) / 2
            val ovalTop = (screenHeight - ovalHeight) / 2

            // ✅ Dark overlay background
            drawRect(
                color = Color.Black.copy(alpha = 0.6f),
                size = size
            )

            // ✅ Clear the oval scanning area
            drawOval(
                color = Color.Transparent,
                topLeft = Offset(ovalLeft, ovalTop),
                size = Size(ovalWidth, ovalHeight),
                blendMode = BlendMode.Clear
            )

            // ✅ Draw the animated border
            drawOval(
                color = borderColor,
                topLeft = Offset(ovalLeft, ovalTop),
                size = Size(ovalWidth, ovalHeight),
                style = Stroke(width = borderWidth.toPx())
            )
        }

        // ✅ Guide Text (Dynamic Color & Alignment)
        Text(
            text = guideText,
            style = guideTextStyle.copy(color = textColor),
            modifier = Modifier
                .align(guideTextAlignment)
                .padding(guideTextPadding)
        )

        // ✅ Instruction Text (Static Position)
        Text(
            text = instructionText,
            style = instructionTextStyle,
            modifier = Modifier
                .align(instructionTextAlignment)
                .padding(instructionTextPadding)
        )
    }
}

