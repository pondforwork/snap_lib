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
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.os.Build
import android.os.CountDownTimer
import android.util.Log
import android.util.Size
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size as geometrySize
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.snap_lib.ml.ModelFront
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import org.opencv.core.MatOfDouble
import com.example.snap_lib.ImageProcessorPlugin as Processor
import com.example.snap_lib.ImageProcessorPlugin

class NewActivity : AppCompatActivity() {

    // กำหนดตัวแปร Model TFlite
    private lateinit var model: ModelFront;
    private lateinit var cameraExecutor: ExecutorService
    private val CAMERA_REQUEST_CODE = 2001
    private var isPredicting = true
    private var isProcessing = false
    private var lastProcessedTime: Long = 0
    private var isFound = false
    private lateinit var flutterEngine: FlutterEngine
    private lateinit var methodChannel: MethodChannel
    private val CHANNEL = "camera"
    // จับเวลาอยู่หรือไม่ ?
    private var isTiming = false
    // นับภาพที่ Capture จาก 1
    // จัดเก็บ Bitmap ของรูปภาพทั้ง 5
    private val bitmapList: MutableList<Bitmap> = mutableListOf()
    private var sharPestImageIndex = 0

    // Mat สำหรับ Open CV
    private lateinit var mat: Mat
    // Path ของภาพที่ชัดที่สุด ที่จะนำมาใช้
    private var pathFinal = ""
    private val cameraViewModel: CameraViewModel by viewModels()
    private val rectPositionViewModel: RectPositionViewModel by viewModels()

    // การปรับแต่งข้อความและตำแหน่งข้อความ
    private var titleMessage = "ถ่ายภาพหน้าบัตร"
    private var initialGuideText = "กรุณาวางบัตรในกรอบ"
    private var foundMessage = "พบบัตร"
    private var notFoundMessage = "ไม่พบบัตร"

    private lateinit var imageProcessorPlugin: ImageProcessorPlugin

    // ตัวแปรรอรับค่า Base 64 ที่จะส่งคืน
    private var base64Image = "";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the parameter from the intent
        titleMessage = intent.getStringExtra("titleMessage") ?: "ถ่ายภาพหน้าบัตร"
        initialGuideText = intent.getStringExtra("initialMessage") ?: "กรุณาวางบัตรในกรอบ"
        foundMessage = intent.getStringExtra("foundMessage") ?: "พบบัตร ถือค้างไว้"
        notFoundMessage = intent.getStringExtra("notFoundMessage") ?: "ไม่พบบัตร"

        // Request camera permission
        checkAndRequestCameraPermission(this, CAMERA_REQUEST_CODE)

        // สร้าง Model เมื่อเปิดหน้านี้
        model = ModelFront.newInstance(this)
        cameraExecutor = Executors.newSingleThreadExecutor()

        imageProcessorPlugin = ImageProcessorPlugin()


        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween, // Space out elements vertically
                            horizontalAlignment = Alignment.CenterHorizontally // Center elements horizontally
                        ) {
                            // Title แสดงด้านบน
                             Text(
 //                                fontFamily = fontKanit,
                                 text = titleMessage,
                                 color = Color.White,
                                 style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                                 textAlign = TextAlign.Center,
                                 modifier = Modifier
                                     .padding(top = 16.dp)
                                     .wrapContentWidth()
                             )
                            // Camera preview with overlay
                            CameraWithOverlay(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                    // .aspectRatio(4f / 3f), 
                                cameraViewModel = cameraViewModel,
                                rectPositionViewModel = rectPositionViewModel
                            )
                        }
                    }
                }
            }
        }

        // Initialize ImageProcessorPlugin
        // Example call to a method from ImageProcessorPlugin
        // val exampleBitmap: Bitmap = // ... obtain a Bitmap ...
        // val processedMat = imageProcessorPlugin.processImage(
        //     inputMat = imageProcessorPlugin.bitmapToMat(exampleBitmap),
        //     gamma = 1.0,
        //     d = 9,
        //     sigmaColor = 75.0,
        //     sigmaSpace = 75.0,
        //     sharpenStrength = 1.0,
        //     blurKernelWidth = 3.0,
        //     blurKernelHeight = 3.0
        // )
        // ... use processedMat ...
    }

    @Composable
    fun CameraWithOverlay(
        modifier: Modifier = Modifier,
        cameraViewModel: CameraViewModel,
        rectPositionViewModel: RectPositionViewModel
    ) {
        // Animation states
//        val selectedSize = remember { mutableStateOf(0.8f) } // Default rectangle size
//        val animatedRectWidth = animateFloatAsState(
//            targetValue = selectedSize.value,
//            animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing)
//        )

//        val pulseScale = rememberInfiniteTransition().animateFloat(
//            initialValue = 1f,
//            targetValue = 1.1f,
//            animationSpec = infiniteRepeatable(
//                animation = tween(1000, easing = FastOutSlowInEasing),
//                repeatMode = RepeatMode.Reverse
//            )
//        )

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Camera preview centered
            Box(
                modifier = Modifier
                    .align(Alignment.Center) // Center the preview
                    .fillMaxSize()
            ) {

                CameraPreview(modifier = Modifier.fillMaxSize())
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                val creditCardAspectRatio = 3.37f / 2.125f
//                val rectWidth = size.width * animatedRectWidth.value * pulseScale.value
                val rectWidth = size.width * 0.8f // Fixed rectangle size
                val rectHeight = rectWidth / creditCardAspectRatio
                val rectLeft = (size.width - rectWidth) / 2
                val rectTop = (size.height - rectHeight) / 2
                val cornerRadius = 20.dp.toPx()

                drawRect(
                    color = Color.Black.copy(alpha = 0.6f),
                    size = size
                )
                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = Offset(rectLeft, rectTop),
                    size = geometrySize(rectWidth, rectHeight),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    blendMode = BlendMode.Clear
                )
                drawRoundRect(
                    color = Color.Gray,
                    topLeft = Offset(rectLeft, rectTop),
                    size = geometrySize(rectWidth, rectHeight),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    style = Stroke(width = 6f)
                )
            }

            Text(
//                fontFamily = fontKanit,
                text = cameraViewModel.guideText,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 300.dp) // Adjust text position
            )
        }
    }

    @Composable
    fun CameraPreview(modifier: Modifier = Modifier) {
        var bitmapToShow by remember { mutableStateOf<Bitmap?>(null) }
        var isShutter by remember { mutableStateOf(false) }
        var showDialog by remember { mutableStateOf(false) }

        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current

        val timer = object : CountDownTimer(1000, 800) {
            override fun onTick(millisUntilFinished: Long) {
                println("Time remaining: ${millisUntilFinished / 800} seconds")
            }
            override fun onFinish() {
                println("Founded For 1S")
                isShutter = true
            }
        }

        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder()
                        .build()

                    val resolutionSelector1 = ResolutionSelector.Builder()
                        .setResolutionStrategy(
                            ResolutionStrategy(
                                android.util.Size(1080, 1440), // ความละเอียดที่ต้องการ
                                ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER // fallback หากไม่รองรับ
                            )
                        )
                        .build()

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setResolutionSelector(resolutionSelector1)
                        .build()

                    imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->

                        if(isShutter){
                            bitmapToShow = imageProxy.toBitmap()
                            val matrix = Matrix()
                            matrix.postRotate(90f)
                            bitmapToShow = Bitmap.createBitmap(
                                bitmapToShow!!, // Original Bitmap
                                0, 0, // Starting coordinates
                                bitmapToShow!!.width, // Bitmap width
                                bitmapToShow!!.height, // Bitmap height
                                matrix, // The rotation matrix
                                true // Apply smooth transformation
                            )

                            // ถ้าภาพยังไม่ครบ 3 ภาพ และ Dialog ไม่ได้แสดงอยู่
                            if (bitmapList.size < 3){
                                // เพิ่มรูป Bitmap เข้า List จนกว่าจะครบ 3 รูป
                                bitmapList.add(bitmapList.size,bitmapToShow!!)
                                bitmapToJpg(bitmapToShow!!,context,"image${bitmapList.size.toString()}.jpg")
                                if(bitmapList.size == 3){
                                    // ถ้าครบ 3 รูปแล้วให้หารูปที่คมชัดที่สุด จาก Bitmap List
                                    var sharPestImage = findSharpestImage()
                                    println("Sharpest Image Index is: ${sharPestImage.first}, Variance: ${sharPestImage.second}")

                                    // บันทึก Index ของภาพที่ชัดที่สุด ไว้ในตัวแปร
//                                    sharPestImageIndex = sharPestImage.first!!

                                    // เสร็จแล้วแสดงภาพที่ชัดที่สุดออกมา
                                    showDialog = true
                                    isShutter = false
                                    // พักการ Predict
                                    isPredicting = false
                                    // รับ bitmap ภาพที่คมที่สุดเพื่อมา Process
                                    val sharpestBitmapMat = bitmapToMat(bitmapList[sharPestImageIndex])

                                    val contrastValue = imageProcessorPlugin.calculateContrast(sharpestBitmapMat)
                                    val resolutionValue = imageProcessorPlugin.calculateResolution(sharpestBitmapMat)
                                    val snrValue = imageProcessorPlugin.calculateSNR(sharpestBitmapMat)

                                    // ทำการ Preprocessing
                                    val processedMat = imageProcessorPlugin.processImage(sharpestBitmapMat)

                                    base64Image = imageProcessorPlugin.convertMatToBase64(processedMat)

                                }
                            }
                        }
                        else if(!isShutter){
                            // ถ้ามีการสั่งให้จำแนก Class
                            if(isPredicting){
                                // ประมวลภาพถ้ามีการสั่งให้ Predict
                                processImageProxy(imageProxy)
                                // ถ้าเจอ เริ่มจับเวลา
                                if(isFound){
                                    if (!isTiming){
                                        isTiming = true
                                        timer.start()
                                        println("Start Timer")
                                    }
                                }else{
                                    // ถ้าไม่เจอ ยกเลิกการจับเวลา
                                    timer.cancel()
                                    isTiming = false
                                }
                            }
                        }
                        imageProxy.close()
                    }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                    preview.setSurfaceProvider(previewView.surfaceProvider)
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
        )

        // Show Dialog
        if (showDialog && bitmapToShow != null) {
            ShowImageDialog(
                bitmap =  bitmapToShow!!,
                onRetake = {
                    showDialog = false
                    // Clear Bitmap List หลังจากปิด Dialog
                    bitmapList.clear()
                    // กลับมา Predict หลังจากปิด Dialog
                    isPredicting = true
                    //รีเซ็ต GuideText เมื่อปิด Dialog (ถ่ายใหม่)
                    cameraViewModel.updateGuideText(initialGuideText)
                    isFound = false
                },
                onConfirm = {
                    val resultIntent = Intent()
                    if (base64Image.isNotEmpty()) {
                        resultIntent.putExtra("result", base64Image)
                        setResult(RESULT_OK, resultIntent) // ใช้ resultIntent แทน base64Image
                        // Log.w("base64Image", base64Image)
                        finish()
                    } else {
                        finish()
                    }

                }
            )

        }
    }

    // แปลงรูปภาพ Bitmap ให้เป็นไฟล์
    fun bitmapToJpg(bitmapImg: Bitmap, context: Context, fileName: String): File {
        // Wrap the context to work with app-specific directories
        val wrapper = ContextWrapper(context)

        // Get the app's private directory for storing images
        val fileDir = wrapper.getDir("Images", Context.MODE_PRIVATE)
        println("Image Directory")
        println(fileDir)
        // Create a file in the directory with the given name
        val file = File(fileDir, fileName)

        try {
            // Create an output stream to write the bitmap data to the file
            val stream: OutputStream = FileOutputStream(file)
            bitmapImg.compress(Bitmap.CompressFormat.JPEG, 100, stream) // Compress to JPEG with quality 25%
            stream.flush()
            stream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return file
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

    private fun processImageProxy(imageProxy: ImageProxy) {
//        Log.d("NewActivity", "processImageProxy called ss")
        isProcessing = true
        try {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastProcessedTime >= 200) {
//                Log.d("NewActivity", "Processing image")
                val bitmap = imageProxy.toBitmap()

                val rotatedBitmap = rotateBitmap(bitmap, 90f)

                val outputBuffer = predictClasss(rotatedBitmap)

                // ถ้าได้ Output ของการ Predict ออกมา
                if (outputBuffer != null) {
                    val outputArray = outputBuffer.floatArray
                    // println(outputArray.contentToString())
                    val maxIndex = outputArray.indices
                        .filter { outputArray[it] >= 0.8 } // เลือก index ที่ค่า >= 80
                        .maxByOrNull { outputArray[it] }
                        ?: 4 // หากไม่มี index ที่เข้าเงื่อนไข ให้ใช้ค่า default เป็น 4

                    Log.d("NewActivity", "maxIndex: $maxIndex")
                    // ถ้าเป็น Class 0
                    // 0 คือ บัตรประชาชน
                    if (maxIndex == 0 ){
                        cameraViewModel.updateGuideText(foundMessage)
                        isFound = true
                    }else{
                        isFound = false
                        cameraViewModel.updateGuideText(notFoundMessage)
                    }
                } else {
                    Log.d("NewActivity", "Output buffer is null")
                }
            } else {
                Log.d("NewActivity", "Skipping image processing due to time constraint")
            }
        } catch (e: Exception) {
            Log.e("NewActivity", "Error processing image", e)
        }
    }

    private fun findSharpestImage(): Pair<Int?, Double> {
        var sharpestIndex: Int? = null
        var maxVariance = 0.0

        for (i in bitmapList.indices) {
            val bitmap = bitmapList[i]
            val mat = bitmapToMat(bitmap) ?: continue

            if (!mat.empty()) {
                val variance = calculateLaplacianVariance(mat)
                if (variance > maxVariance) {
                    maxVariance = variance
                    sharpestIndex = i
                }
                mat.release()
            }
        }
        return Pair(sharpestIndex, maxVariance)
    }

    private fun bitmapToMat(bitmap: Bitmap): Mat {
        val mat = Mat() // Create an empty Mat
        Utils.bitmapToMat(bitmap, mat) // Convert Bitmap to Mat
        return mat
    }

    private fun calculateLaplacianVariance(mat: Mat): Double {
        val laplacian = Mat()
        Imgproc.Laplacian(mat, laplacian, mat.depth())

        val mean = MatOfDouble()
        val stddev = MatOfDouble()

        // Correct call to Core.meanStdDev
        Core.meanStdDev(laplacian, mean, stddev)

        val variance = stddev[0, 0][0] * stddev[0, 0][0] // Variance = (StdDev)^2
        laplacian.release()

        return variance
    }


    private fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(rotationDegrees) // Rotate the bitmap by the given angle
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun predictClasss(imageBytes: Bitmap): TensorBuffer? {
        try {
            // Resize the image to the required input size for the model (224x224)
            val height = 224
            val width = 224
            val resizedBitmap = Bitmap.createScaledBitmap(imageBytes, width, height, true)
            // println("Image resized to $width x $height.")

            // Prepare a ByteBuffer to hold image data in the required format (Float32)
            val imageData = ByteBuffer.allocateDirect(4 * height * width * 3) // 3 for RGB channels
            imageData.order(ByteOrder.nativeOrder())

            // Convert image pixels to normalized RGB values and fill the ByteBuffer
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val pixel = resizedBitmap.getPixel(x, y)

                    // Extract RGB values and normalize to [0, 1]
                    val r = (pixel shr 16 and 0xFF) / 255.0f
                    val g = (pixel shr 8 and 0xFF) / 255.0f
                    val b = (pixel and 0xFF) / 255.0f

                    imageData.putFloat(r)
                    imageData.putFloat(g)
                    imageData.putFloat(b)
                }
            }

            // Create input tensor buffer with the required shape
            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
            inputFeature0.loadBuffer(imageData)

            // Run inference using the already loaded model
            val outputs = model.process(inputFeature0)

            // Extract the output tensor buffer
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer

            // Return the output feature
            return outputFeature0
        } catch (e: Exception) {
            println("Error processing image: ${e.message}")
            e.printStackTrace()
            return null // Return null if an error occurs
        }
    }

    @Composable
    fun ShowImageDialog(
        bitmap: Bitmap,
        onRetake: () -> Unit, // Callback for "ถ่ายใหม่"
        onConfirm: () -> Unit // Callback for "ยืนยัน"
    ) {
        Dialog(onDismissRequest = { /* Prevent dismiss by clicking outside */ }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f)) // Dim background
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier

                        .wrapContentHeight()
                        .padding(8.dp),
                    shape = RoundedCornerShape(16.dp), // Rounded corners for a modern look
                    color = Color.White, // Dialog background
                    shadowElevation = 12.dp // Subtle shadow for emphasis
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Title Text
                        Text(
//                            fontFamily = fontKanit,
                            text = "ยืนยันข้อมูล",
                            color = Color(0xFF2D3892), // Stylish blue title
                            fontSize = 22.sp, // Larger font size for prominence
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Subtitle
                        Text(
//                            fontFamily = fontKanit,
                            text = "กรุณาตรวจสอบความชัดเจนของภาพบัตร",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Display the captured image
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Captured Image",
                            modifier = Modifier
                                .fillMaxWidth() // Wider image
                                .height(300.dp) // Adjusted height for layout
                                .clip(RoundedCornerShape(12.dp)) // Rounded corners for the image
                                .padding(8.dp) // Padding around the image
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Button row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly // Evenly distribute buttons
                        ) {
                            // ปุ่มถ่ายใหม่
                            // Retake Button
                            Button(
                                onClick = onRetake,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .border(2.dp, Color.Gray, RoundedCornerShape(24.dp))
                            ) {
                                Text(
//                                    fontFamily = fontKanit,
                                    text = "ถ่ายใหม่",
                                    color = Color.Black,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Confirm Button
                            Button(
                                onClick = onConfirm,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D3892)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .border(2.dp, Color(0xFF2D3892), RoundedCornerShape(24.dp)) // Border matches button color
                            ) {
                                Text(
//                                    fontFamily = fontKanit,
                                    text = "ยืนยัน",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

class CameraViewModel : ViewModel() {
    // Initial Guide Text
    var guideText by mutableStateOf("กรุณาวางบัตรในกรอบ")
        private set

    var brightnessValueText by mutableStateOf("0")
        private set

    var glareValueText by mutableStateOf("0")
        private set
    var snrValueText by mutableStateOf("0")
        private set

    // Function to update the guide text
    fun updateGuideText(newText: String) {
        guideText = newText
    }

    fun updateBrightnessValueText(newValue: String) {
        brightnessValueText = newValue
    }
    fun updateSnrValueText(newValue: String) {
        snrValueText = newValue
    }

    fun updateGlareValueText(newValue: String) {
        glareValueText = newValue
    }
}

class RectPositionViewModel : ViewModel() {
    private val _rectPosition = MutableLiveData<Rect>()
    fun updateRectPosition(left: Float, top: Float, right: Float, bottom: Float) {
        _rectPosition.value = Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
    }
}



