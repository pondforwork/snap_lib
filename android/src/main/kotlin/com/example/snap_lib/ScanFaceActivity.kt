package com.example.snap_lib

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
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
import com.example.snap_lib.ml.ModelFace
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
    private lateinit var model: ModelFace
    private lateinit var overlaySettings: OverlaySettings

    fun sendImageToFlutter(base64Image: String) {
        methodChannel.invokeMethod("imageCaptured", base64Image) // Send Base64 to Flutter
        finish() // Close activity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cameraPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                setCameraContent()
            } else {
                Log.e("CameraX", "Camera permission denied")
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            setCameraContent()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        model = ModelFace.newInstance(this)

        flutterEngine = FlutterEngine(this)
        methodChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "snap_plugin")

        cameraExecutor = Executors.newSingleThreadExecutor()
        flutterEngine = FlutterEngine(this)

        // Initialize overlaySettings with data from intent
        overlaySettings = OverlaySettings(
            guideText = intent.getStringExtra("guideText") ?: "ถ่ายภาพใบหน้า",
            instructionText = intent.getStringExtra("instructionText") ?: "อย่าปิดตา จมูก ปาก หรือคาง",
            successText = intent.getStringExtra("successText") ?: "สำเร็จ",
            borderColorSuccess = intent.getIntExtra("borderColorSuccess", 0xFF00FF00.toInt()),
            borderColorDefault = intent.getIntExtra("borderColorDefault", 0xFFFF0000.toInt()),
            textColorDefault = intent.getIntExtra("textColorDefault", 0xFFFFFFFF.toInt()),
            textColorSuccess = intent.getIntExtra("textColorSuccess", 0xFF00FF00.toInt()),
            guideFontSize = intent.getFloatExtra("guideFontSize", 22f),
            instructionFontSize = intent.getFloatExtra("instructionFontSize", 18f),
            guideTextColor = intent.getIntExtra("guideTextColor", 0xFFFFFF00.toInt()),
            instructionTextColor = intent.getIntExtra("instructionTextColor", 0x00FFFF.toInt())
        )

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

    fun processImageWithTFLite(context: Context, bitmap: Bitmap): Int {
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)

        val byteBuffer = convertBitmapToByteBuffer(bitmap)
        inputFeature0.loadBuffer(byteBuffer)

        val outputs = model.process(inputFeature0)

        val probabilities = outputs.outputFeature0AsTensorBuffer.floatArray

        // หา Class ที่มีความเป็นไปได้สูงสุด
        val detectedClass = probabilities.indices.maxByOrNull { probabilities[it] } ?: -1
        Log.e("detectedClass",detectedClass.toString())

        return detectedClass
    }

    @Composable
    fun FaceDetectionScreen(settings: OverlaySettings) {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        var detectedFace by remember { mutableStateOf(false) }
        var showDialog by remember { mutableStateOf(false) }
        var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
        var classTwoDetectedTime by remember { mutableStateOf<Long?>(null) }
        var isCapturing by remember { mutableStateOf(true) }
        var startCaptureAnimation by remember { mutableStateOf(false) }
        var captureProgress by remember { mutableStateOf(1f) }

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
            if (!isCapturing) {
                imageProxy.close()
                return@setAnalyzer
            }

            val bitmap = imageProxy.toBitmap()
            val rotatedBitmap = rotateBitmap(bitmap, -90f)
            val detectedClass = processImageWithTFLite(context, rotatedBitmap)

            if (detectedClass == 0) {
                val currentTime = System.currentTimeMillis()
                overlaySettings.guideText = "ถือค้างไว้"

                if (classTwoDetectedTime == null) {
                    classTwoDetectedTime = currentTime
                    startCaptureAnimation = true // ✅ Start animation
                }

                if (classTwoDetectedTime != null && currentTime - classTwoDetectedTime!! >= 1500) {
                    capturedBitmap = rotatedBitmap
                    showDialog = true
                    isCapturing = false
                    classTwoDetectedTime = null
                    startCaptureAnimation = false // ✅ Stop animation after capture
                }
            } else {
                overlaySettings.guideText = "วางหน้า"
                classTwoDetectedTime = null
                startCaptureAnimation = false // ✅ Stop animation if face is lost
            }

            detectedFace = (detectedClass == 1)
            imageProxy.close()
        }

        LaunchedEffect(startCaptureAnimation) {
            if (startCaptureAnimation) {
                var progress = 1f
                while (progress >= 0f) {
                    captureProgress = progress
                    progress -= 0.05f
                    kotlinx.coroutines.delay(100)
                }
            }
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
                guideText = overlaySettings.guideText,
                instructionText = overlaySettings.instructionText,
                borderColorSuccess = Color.Green,
                borderColorDefault = Color.Red
            )

            if (startCaptureAnimation) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 1f - captureProgress))
                )
            }

            if (showDialog && capturedBitmap != null) {
                CapturedImageDialog(
                    bitmap = capturedBitmap!!,
                    onRetake = {
                        showDialog = false
                        isCapturing = true
                        overlaySettings.guideText = "วางหน้า"
                        startCaptureAnimation = false // ✅ Reset animation
                    },
                    onConfirm = {
                        showDialog = false
                        val base64Image = convertBitmapToBase64(capturedBitmap!!)
                        (context as ScanFaceActivity).sendImageToFlutter(base64Image)
                    }
                )
            }
        }
    }


    private fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(rotationDegrees) // Rotate the bitmap by the given angle
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }


    data class OverlaySettings(
        var guideText: String,
        var instructionText: String,
        var successText: String,
        var borderColorSuccess: Int,
        var borderColorDefault: Int,
        var textColorDefault: Int,
        var textColorSuccess: Int,
        var guideFontSize: Float,
        var instructionFontSize: Float,
        var guideTextColor: Int,
        var instructionTextColor: Int
    )

    fun convertBitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
    }

    @Composable
    fun CameraOverlay(
        overlaySettings: OverlaySettings
    ) {
        val borderColor by animateColorAsState(
            targetValue = if (overlaySettings.guideText == "ถือค้างไว้")
                Color(overlaySettings.borderColorSuccess)
            else
                Color(overlaySettings.borderColorDefault),
            animationSpec = tween(durationMillis = 500)
        )

        val textColor by animateColorAsState(
            targetValue = if (overlaySettings.guideText == "ถือค้างไว้")
                Color(overlaySettings.textColorSuccess)
            else
                Color(overlaySettings.textColorDefault),
            animationSpec = tween(durationMillis = 500)
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val screenWidth = size.width
                val screenHeight = size.height

                val ovalWidth = screenWidth * 0.7f
                val ovalHeight = screenHeight * 0.5f
                val ovalLeft = (screenWidth - ovalWidth) / 2
                val ovalTop = (screenHeight - ovalHeight) / 2

                // Background with transparent oval
                drawRect(color = Color.Black.copy(alpha = 0.6f), size = size)
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
                    style = Stroke(width = 8.dp.toPx())
                )
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ✅ Guide Text (Top)
                Text(
                    text = overlaySettings.guideText,
                    fontSize = overlaySettings.guideFontSize.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(top = 32.dp)
                )

                Spacer(modifier = Modifier.weight(1f)) // Push instruction text down

                // ✅ Instruction Text (Bottom)
                Text(
                    text = overlaySettings.instructionText,
                    fontSize = overlaySettings.instructionFontSize.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(overlaySettings.instructionTextColor),
                    modifier = Modifier.padding(bottom = 32.dp)
                )
            }
        }
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

    private fun setCameraContent() {
        setContent {
            FaceDetectionScreen(overlaySettings)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (::cameraExecutor.isInitialized) {
            cameraExecutor.shutdown()
        }

        if (::flutterEngine.isInitialized) {
            flutterEngine.destroy()
        }
    }
}

