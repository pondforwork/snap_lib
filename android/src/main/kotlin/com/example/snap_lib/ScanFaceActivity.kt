package com.example.snap_lib

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
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
import androidx.core.content.ContextCompat
import com.example.snap_lib.ml.ModelFrontNew
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanFaceActivity : ComponentActivity() {
    private lateinit var flutterEngine: FlutterEngine
    private lateinit var methodChannel: MethodChannel
    private val CHANNEL = "snap_plugin"
    private lateinit var cameraExecutor: ExecutorService

    private var overlaySettings = OverlaySettings(
        guideText = "ให้ใบหน้าอยู่ในกรอบที่กำหนด",
        instructionText = "ไม่มีปิดตา จมูก ปาก และคาง",
        successText = "ถือค้างไว้",
        borderColorSuccess = 0xFF00FF00.toInt(),
        borderColorDefault = 0xFFFF0000.toInt(),
        textColorDefault = 0xFFFFFFFF.toInt(),
        textColorSuccess = 0xFF00FF00.toInt(),
        guideFontSize = 22f,
        instructionFontSize = 18f,
        guideTextColor = 0xFFFFFF00.toInt(),
        instructionTextColor = 0x00FFFF.toInt()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cameraPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                setCameraContent() // ✅ Start Camera if permission is granted
            } else {
                Log.e("CameraX", "Camera permission denied")
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            setCameraContent()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        flutterEngine = FlutterEngine(this)
        methodChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "snap_plugin")

        cameraExecutor = Executors.newSingleThreadExecutor()
        // ✅ Initialize flutterEngine before use
        flutterEngine = FlutterEngine(this)

        methodChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "snap_plugin")
        methodChannel.setMethodCallHandler { call, result ->
            when (call.method) {
                "updateOverlaySettings" -> {
                    overlaySettings = OverlaySettings(
                        guideText = call.argument<String>("guideText") ?: overlaySettings.guideText,
                        instructionText = call.argument<String>("instructionText") ?: overlaySettings.instructionText,
                        successText = call.argument<String>("successText") ?: overlaySettings.successText,
                        borderColorSuccess = call.argument<Int>("borderColorSuccess") ?: overlaySettings.borderColorSuccess,
                        borderColorDefault = call.argument<Int>("borderColorDefault") ?: overlaySettings.borderColorDefault,
                        textColorDefault = call.argument<Int>("textColorDefault") ?: overlaySettings.textColorDefault,
                        textColorSuccess = call.argument<Int>("textColorSuccess") ?: overlaySettings.textColorSuccess,
                        guideFontSize = call.argument<Float>("guideFontSize") ?: overlaySettings.guideFontSize,
                        instructionFontSize = call.argument<Float>("instructionFontSize") ?: overlaySettings.instructionFontSize,
                        guideTextColor = call.argument<Int>("guideTextColor") ?: overlaySettings.guideTextColor,
                        instructionTextColor = call.argument<Int>("instructionTextColor") ?: overlaySettings.instructionTextColor
                    )
                    result.success("Overlay settings updated")
                }
                else -> result.notImplemented()
            }
        }


    }



    private fun setCameraContent() {
        setContent {
            FaceDetectionScreen(overlaySettings)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // ✅ Check before shutting down cameraExecutor
        if (::cameraExecutor.isInitialized) {
            cameraExecutor.shutdown()
        }

        // ✅ Check before destroying flutterEngine
        if (::flutterEngine.isInitialized) {
            flutterEngine.destroy()
        }
    }

}

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
    var detectedFace by remember { mutableStateOf(false) }
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    val executor = ContextCompat.getMainExecutor(context)

    val previewView = remember {
        androidx.camera.view.PreviewView(context).apply {
            layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    val imageAnalyzer = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()

    imageAnalyzer.setAnalyzer(executor) { imageProxy ->
        val bitmap = imageProxy.toBitmap()
        val detected = processImageWithTFLite(context, bitmap)
        detectedFace = detected
        imageProxy.close()
    }


    LaunchedEffect(Unit) {
        try {
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalyzer.setAnalyzer(executor) { imageProxy ->
                    val bitmap = imageProxy.toBitmap()
                    val detected = processImageWithTFLite(context.applicationContext, bitmap)
                    detectedFace = detected
                    imageProxy.close()
                }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    context as ComponentActivity,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview,
                    imageAnalyzer
                )
            }, executor)
        } catch (e: Exception) {
            Log.e("CameraX", "Failed to initialize CameraX", e)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

        CameraOverlay(
            guideText = if (detectedFace) settings.successText else settings.guideText,
            instructionText = settings.instructionText
        )
    }
}

@Composable
fun CameraOverlay(
    guideText: String,
    instructionText: String,
    borderColorSuccess: Color = Color.Green,
    borderColorDefault: Color = Color.Red,
    borderWidth: Dp = 8.dp
) {
    val borderColor by animateColorAsState(
        targetValue = if (guideText == "ถือค้างไว้") borderColorSuccess else borderColorDefault,
        animationSpec = tween(durationMillis = 500)
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center // ✅ Ensures text appears at the center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val screenWidth = size.width
            val screenHeight = size.height

            val ovalWidth = screenWidth * 0.7f
            val ovalHeight = screenHeight * 0.5f
            val ovalLeft = (screenWidth - ovalWidth) / 2
            val ovalTop = (screenHeight - ovalHeight) / 2

            drawRect(color = Color.Black.copy(alpha = 0.6f), size = size)
            drawOval(color = Color.Transparent, topLeft = Offset(ovalLeft, ovalTop), size = Size(ovalWidth, ovalHeight), blendMode = BlendMode.Clear)
            drawOval(color = borderColor, topLeft = Offset(ovalLeft, ovalTop), size = Size(ovalWidth, ovalHeight), style = Stroke(width = borderWidth.toPx()))
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween, // ✅ Makes sure text is positioned correctly
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // **✅ Guide Text (Top)**
            Text(
                text = guideText,
                style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White),
                modifier = Modifier.padding(top = 32.dp)
            )

            Spacer(modifier = Modifier.weight(1f)) // **Push instruction text down**

            // **✅ Instruction Text (Bottom)**
            Text(
                text = instructionText,
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Normal, color = Color.White),
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }
    }
}


fun processImageWithTFLite(context: Context, bitmap: Bitmap): Boolean {
    val appContext = context.applicationContext // ✅ Ensures safe context usage
    val model = ModelFrontNew.newInstance(appContext)

    val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
    val byteBuffer = convertBitmapToByteBuffer(bitmap)
    inputFeature0.loadBuffer(byteBuffer)

    val outputs = model.process(inputFeature0)
    model.close()

    return outputs.outputFeature0AsTensorBuffer.floatArray[0] > 0.7
}

fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
    val width = 224
    val height = 224
    val resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)

    val byteBuffer = ByteBuffer.allocateDirect(4 * width * height * 3) // 3 channels (RGB)
    byteBuffer.order(ByteOrder.nativeOrder())

    for (y in 0 until height) {
        for (x in 0 until width) {
            val pixel = resizedBitmap.getPixel(x, y)

            val r = (pixel shr 16 and 0xFF) / 255.0f
            val g = (pixel shr 8 and 0xFF) / 255.0f
            val b = (pixel and 0xFF) / 255.0f

            byteBuffer.putFloat(r)
            byteBuffer.putFloat(g)
            byteBuffer.putFloat(b)
        }
    }

    return byteBuffer
}
