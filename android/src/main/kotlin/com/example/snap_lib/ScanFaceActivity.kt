package com.example.snap_lib

import android.Manifest
import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.text.style.TextAlign
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
    private var dialogSettings = ScanFaceActivity.DialogSettings()

    fun sendImageToFlutter(base64Image: String) {
        methodChannel.invokeMethod("imageCaptured", base64Image) // Send Base64 to Flutter
        finish() // Close activity
    }
    data class DialogSettings(
        val dialogBackgroundColor: Int = 0xFFFFFFFF.toInt(),
        val dialogTitleColor: Int = 0xFF2D3892.toInt(),
        val dialogButtonConfirmColor: Int = 0xFF2D3892.toInt(),
        val dialogButtonRetakeColor: Int = 0xFFFFFFFF.toInt(),
        val dialogButtonTextColor: Int = 0xFF000000.toInt(),
        val dialogAlignment: String = "center",
        val dialogTitle: String = "ยืนยันข้อมูล",
        val dialogTitleFontSize: Int = 22,
        val dialogTitleAlignment: String = "center",
        val dialogExtraMessage: String = "ตรวจสอบให้แน่ใจว่ารูปภาพสามารถอ่านได้ชัดเจน",
        val dialogExtraMessageColor: Int = 0xFF000000.toInt(),
        val dialogExtraMessageFontSize: Int = 14,
        val dialogExtraMessageAlignment: String = "center",
        val dialogBorderRadius: Int = 16,
        val dialogButtonHeight: Int = 48
    )
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
            instructionTextColor = intent.getIntExtra("instructionTextColor", 0x00FFFF.toInt()),
            borderWidth = intent.getIntExtra("borderWidth", 0xFFFFFFFF.toInt()),


        )

//        dialog
        dialogSettings = ScanFaceActivity.DialogSettings(
            dialogBackgroundColor = intent.getIntExtra("dialogBackgroundColor", 0xFFFFFFFF.toInt()),
            dialogTitleColor = intent.getIntExtra("dialogTitleColor", 0xFF2D3892.toInt()),
            dialogButtonConfirmColor = intent.getIntExtra(
                "dialogButtonConfirmColor",
                0xFF2D3892.toInt()
            ),
            dialogButtonRetakeColor = intent.getIntExtra(
                "dialogButtonRetakeColor",
                0xFFFFFFFF.toInt()
            ),
            dialogButtonTextColor = intent.getIntExtra("dialogButtonTextColor", 0xFF000000.toInt()),
            dialogAlignment = intent.getStringExtra("dialogAlignment") ?: "center",
            dialogTitle = intent.getStringExtra("dialogTitle") ?: "ยืนยันข้อมูล",
            dialogTitleFontSize = intent.getIntExtra("dialogTitleFontSize", 22),
            dialogTitleAlignment = intent.getStringExtra("dialogTitleAlignment") ?: "center",
            dialogExtraMessage = intent.getStringExtra("dialogExtraMessage")
                ?: "ตรวจสอบให้แน่ใจว่ารูปภาพสามารถอ่านได้ชัดเจน",
            dialogExtraMessageColor = intent.getIntExtra(
                "dialogExtraMessageColor",
                0xFF000000.toInt()
            ),
            dialogExtraMessageFontSize = intent.getIntExtra("dialogExtraMessageFontSize", 14),
            dialogExtraMessageAlignment = intent.getStringExtra("dialogExtraMessageAlignment")
                ?: "center",
            dialogBorderRadius = intent.getIntExtra("dialogBorderRadius", 16),
            dialogButtonHeight = intent.getIntExtra("dialogButtonHeight", 48)
        )


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
                captureProgress = 1f  // Reset progress to full opacity
                while (captureProgress > 0f) {
                    captureProgress -= 0.05f
                    kotlinx.coroutines.delay(100)
                }
                startCaptureAnimation = false // Ensure animation stops correctly
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
                overlaySettings = overlaySettings
            )

            if (showDialog && capturedBitmap != null) {
                ShowImageDialog(
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
                    },


                    // ✅ Apply dialog settings dynamically
                    dialogBackgroundColor = Color(dialogSettings.dialogBackgroundColor),
                    dialogTitleColor = Color(dialogSettings.dialogTitleColor),
                    dialogAlignment = when (dialogSettings.dialogAlignment) {
                        "top" -> Alignment.TopCenter
                        "bottom" -> Alignment.BottomCenter
                        else -> Alignment.Center
                    },

                    // ✅ Apply text settings
                    title = dialogSettings.dialogTitle,
                    titleFontSize = dialogSettings.dialogTitleFontSize,
                    titleAlignment = when (dialogSettings.dialogTitleAlignment) {
                        "left" -> TextAlign.Left
                        "right" -> TextAlign.Right
                        else -> TextAlign.Center
                    },



                    // ✅ Apply extra message settings
                    extraMessage = dialogSettings.dialogExtraMessage,
                    extraMessageColor = Color(dialogSettings.dialogExtraMessageColor),
                    extraMessageFontSize = dialogSettings.dialogExtraMessageFontSize,
                    extraMessageAlignment = when (dialogSettings.dialogExtraMessageAlignment) {
                        "left" -> TextAlign.Left
                        "right" -> TextAlign.Right
                        else -> TextAlign.Center
                    },

                    borderRadius = dialogSettings.dialogBorderRadius.dp,
                    buttonHeight = dialogSettings.dialogButtonHeight.dp
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
        var instructionTextColor: Int,
        var borderWidth: Int,
        var guideTextPosition: String = "Top", // "Top", "Center", "Bottom"
        var instructionTextPosition: String = "Bottom" // "Top", "Center", "Bottom"
    )

    fun convertBitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
    }
    @Composable
    fun CameraOverlay(
        modifier: Modifier = Modifier,
        overlaySettings: OverlaySettings
    ) {
        val guideText = overlaySettings.guideText.ifEmpty { "ให้ใบหน้าอยู่ในกรอบที่กำหนด" }
        val instructionText = overlaySettings.instructionText.ifEmpty { "ไม่มีปิดตา จมูก ปาก และคาง" }
        val successText = overlaySettings.successText.ifEmpty { "ถือค้างไว้" }

        val borderColorSuccess = Color(overlaySettings.borderColorSuccess.takeIf { it != 0 } ?: Color.Green.toArgb())
        val borderColorDefault = Color(overlaySettings.borderColorDefault.takeIf { it != 0 } ?: Color.Red.toArgb())

        val textColorDefault = Color(overlaySettings.textColorDefault.takeIf { it != 0 } ?: Color.White.toArgb())
        val textColorSuccess = Color(overlaySettings.textColorSuccess.takeIf { it != 0 } ?: Color.Green.toArgb())

        val guideFontSize = overlaySettings.guideFontSize.takeIf { it > 0 } ?: 22f
        val instructionFontSize = overlaySettings.instructionFontSize.takeIf { it > 0 } ?: 18f

        val guideTextColor = Color(overlaySettings.guideTextColor.takeIf { it != 0 } ?: Color.Yellow.toArgb())
        val instructionTextColor = Color(overlaySettings.instructionTextColor.takeIf { it != 0 } ?: Color.Cyan.toArgb())

        val borderWidth = overlaySettings.guideFontSize.takeIf { it > 0 } ?: 22f

        val guideTextAlignment = when (overlaySettings.guideTextPosition) {
            "Center" -> Alignment.Center
            "Bottom" -> Alignment.BottomCenter
            else -> Alignment.TopCenter
        }

        val instructionTextAlignment = when (overlaySettings.instructionTextPosition) {
            "Top" -> Alignment.TopCenter
            "Center" -> Alignment.Center
            else -> Alignment.BottomCenter
        }

        val borderColor by animateColorAsState(
            targetValue = if (guideText == successText) borderColorSuccess else borderColorDefault,
            animationSpec = tween(durationMillis = 500)
        )

        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val screenWidth = size.width
                val screenHeight = size.height

                val ovalWidth = screenWidth * 0.7f
                val ovalHeight = screenHeight * 0.5f
                val ovalLeft = (screenWidth - ovalWidth) / 2
                val ovalTop = (screenHeight - ovalHeight) / 2

                // Dark background
                drawRect(color = Color.Black.copy(alpha = 0.6f), size = size)

                // Transparent oval
                drawOval(
                    color = Color.Transparent,
                    topLeft = Offset(ovalLeft, ovalTop),
                    size = Size(ovalWidth, ovalHeight),
                    blendMode = BlendMode.Clear
                )

                // Border
                drawOval(
                    color = borderColor,
                    topLeft = Offset(ovalLeft, ovalTop),
                    size = Size(ovalWidth, ovalHeight),
                    style = Stroke(width = borderWidth)
                )
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    contentAlignment = guideTextAlignment
                ) {
                    Text(
                        text = guideText,
                        fontSize = guideFontSize.sp,
                        fontWeight = FontWeight.Bold,
                        color = guideTextColor
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    contentAlignment = instructionTextAlignment
                ) {
                    Text(
                        text = instructionText,
                        fontSize = instructionFontSize.sp,
                        fontWeight = FontWeight.Medium,
                        color = instructionTextColor
                    )
                }
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
    fun ShowImageDialog(
        bitmap: Bitmap,
        onRetake: () -> Unit,
        onConfirm: () -> Unit,

        // ✅ Dialog Customization
        dialogBackgroundColor: Color = Color.White,
        dialogTitleColor: Color = Color(0xFF2D3892),
        dialogSubtitleColor: Color = Color.Gray,
        dialogAlignment: Alignment = Alignment.Center,
        // ✅ Title Customization
        title: String = "ยืนยันข้อมูล",
        titleFontSize: Int = 22,
        titleAlignment: TextAlign = TextAlign.Center,


        // ✅ Extra Message Customization
        extraMessage: String = "ตรวจสอบให้แน่ใจว่ารูปภาพสามารถอ่านได้ชัดเจน",
        extraMessageColor: Color = Color.Red,
        extraMessageFontSize: Int = 14,
        extraMessageAlignment: TextAlign = TextAlign.Center,

        // ✅ Dialog Shape & Buttons
        borderRadius: Dp = 16.dp,
        buttonHeight: Dp = 48.dp,

        // ✅ Retake Button Customization (ถ่ายใหม่)
        retakeButtonText: String = "ถ่ายใหม่",
        retakeButtonTextColor: Color = Color.Black,
        retakeButtonBackgroundColor: Color = Color.White,
        retakeButtonBorderColor: Color = Color.Gray,
        retakeButtonBorderWidth: Dp = 2.dp,

        // ✅ Confirm Button Customization (ยืนยัน)
        confirmButtonText: String = "ยืนยัน",
        confirmButtonTextColor: Color = Color.White,
        confirmButtonBackgroundColor: Color = Color(0xFF2D3892),
        confirmButtonBorderColor: Color = Color(0xFF2D3892),
        confirmButtonBorderWidth: Dp = 2.dp
    ) {
        Dialog(onDismissRequest = { /* Prevent dismiss by clicking outside */ }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f)) // ✅ Dim background
                    .padding(8.dp),
                contentAlignment = dialogAlignment
            ) {
                Surface(
                    shape = RoundedCornerShape(borderRadius),
                    color = dialogBackgroundColor,
                    shadowElevation = 12.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // ✅ Title
                        Text(
                            text = title,
                            color = dialogTitleColor,
                            fontSize = titleFontSize.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = titleAlignment,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )



                        // ✅ Extra Message (Warnings)
                        Text(
                            text = extraMessage,
                            color = extraMessageColor,
                            fontSize = extraMessageFontSize.sp,
                            textAlign = extraMessageAlignment,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // ✅ Captured Image Display
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Captured Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .padding(8.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // ✅ Button Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // 🔄 **Retake Button (ถ่ายใหม่)**
                            Button(
                                onClick = onRetake,
                                colors = ButtonDefaults.buttonColors(containerColor = retakeButtonBackgroundColor),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(buttonHeight)
                                    .border(retakeButtonBorderWidth, retakeButtonBorderColor, RoundedCornerShape(24.dp))
                            ) {
                                Text(
                                    text = retakeButtonText,
                                    color = retakeButtonTextColor,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // ✅ **Confirm Button (ยืนยัน)**
                            Button(
                                onClick = onConfirm,
                                colors = ButtonDefaults.buttonColors(containerColor = confirmButtonBackgroundColor),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(buttonHeight)
                                    .border(confirmButtonBorderWidth, confirmButtonBorderColor, RoundedCornerShape(24.dp))
                            ) {
                                Text(
                                    text = confirmButtonText,
                                    color = confirmButtonTextColor,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
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

