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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
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
    private val CHANNEL = "snap_plugin"
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var methodChannel: MethodChannel
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
    fun sendImageToFlutter(base64Image: String) {
        methodChannel.invokeMethod("imageCaptured", base64Image) // Send Base64 to Flutter
        finish() // Close activity
    }
    fun convertBitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
    }

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

fun convertBitmapToBase64(bitmap: Bitmap): String {
    val byteArrayOutputStream = java.io.ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
}



@Composable
fun FaceDetectionScreen(settings: OverlaySettings) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var detectedFace by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var classTwoDetectedTime by remember { mutableStateOf<Long?>(null) }
    var isCapturing by remember { mutableStateOf(true) } // ✅ Control camera capturing

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

    val imageAnalyzer = remember {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
    }

    imageAnalyzer.setAnalyzer(executor) { imageProxy ->
        if (!isCapturing) { // ✅ Skip processing if capturing is paused
            imageProxy.close()
            return@setAnalyzer
        }

        val bitmap = imageProxy.toBitmap()
        val detectedClass = processImageWithTFLite(context, bitmap)

        if (detectedClass == 1) {
            val currentTime = System.currentTimeMillis()

            // ✅ Start counting if class 2 is detected
            if (classTwoDetectedTime == null) {
                classTwoDetectedTime = currentTime
            }

            // ✅ If class 2 has been detected for 2 seconds, capture image
            if (classTwoDetectedTime != null && currentTime - classTwoDetectedTime!! >= 2000) {
                capturedBitmap = bitmap
                showDialog = true
                isCapturing = false // ✅ Stop capturing
                classTwoDetectedTime = null // Reset timer
            }
        } else {
            classTwoDetectedTime = null // Reset timer if class changes
        }

        detectedFace = (detectedClass == 1)
        imageProxy.close()
    }

    LaunchedEffect(Unit) {
        try {
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
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

    // ✅ Show Dialog when class 2 is detected for 2 seconds
    if (showDialog && capturedBitmap != null) {
        CapturedImageDialog(
            bitmap = capturedBitmap!!,
            onRetake = {
                showDialog = false
                isCapturing = true // ✅ Resume capturing
            },
            onConfirm = {
                showDialog = false
                val base64Image = convertBitmapToBase64(capturedBitmap!!)
                (context as ScanFaceActivity).sendImageToFlutter(base64Image) // ✅ Send image to Flutter
            }
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

fun processImageWithTFLite(context: Context, bitmap: Bitmap): Int {
    val model = ModelFrontNew.newInstance(context) // ✅ Ensure context is passed correctly
    val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)

    val byteBuffer = convertBitmapToByteBuffer(bitmap) // ✅ Convert bitmap to tensor buffer
    inputFeature0.loadBuffer(byteBuffer)

    val outputs = model.process(inputFeature0)
    model.close()

    val probabilities = outputs.outputFeature0AsTensorBuffer.floatArray

    // ✅ Find the highest probability class
    val detectedClass = probabilities.indices.maxByOrNull { probabilities[it] } ?: -1

    // ✅ Print the detected class and probabilities
    Log.d("TFLite", "Detected Class: $detectedClass")
    Log.d("TFLite", "Probabilities: ${probabilities.contentToString()}")

    return detectedClass // ✅ Return detected class
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
@Composable
fun CapturedImageDialog(
    bitmap: Bitmap,
    onRetake: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .wrapContentHeight()
                    .padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ยืนยันข้อมูล",
                        color = Color(0xFF2D3892),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Captured Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = onRetake,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .border(2.dp, Color.Gray, RoundedCornerShape(24.dp))
                        ) {
                            Text(text = "ถ่ายใหม่", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(
                            onClick = onConfirm,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D3892)),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .border(2.dp, Color(0xFF2D3892), RoundedCornerShape(24.dp))
                        ) {
                            Text(text = "ยืนยัน", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
