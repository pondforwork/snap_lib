package com.example.snap_lib
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import java.io.ByteArrayOutputStream


import kotlin.math.pow

class ImageProcessorPlugin : FlutterPlugin, MethodChannel.MethodCallHandler {
    private lateinit var channel: MethodChannel

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "image_processor_plugin")
        channel.setMethodCallHandler(this)

        if (!OpenCVLoader.initDebug()) {
            Log.e("SnapLib", "OpenCV initialization failed")
        } else {
            Log.i("SnapLib", "OpenCV initialized successfully")
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        try {
            when (call.method) {
                "processImage" -> {
                    val imageBytes = call.argument<ByteArray>("image")
                    val gamma = call.argument<Double>("gamma") ?: 1.0
                    val useBilateralFilter = call.argument<Boolean>("useBilateralFilter") ?: true
                    val d = call.argument<Int>("d") ?: 9
                    val sigmaColor = call.argument<Double>("sigmaColor") ?: 75.0
                    val sigmaSpace = call.argument<Double>("sigmaSpace") ?: 75.0
                    val useSharpening = call.argument<Boolean>("useSharpening") ?: true
                    val sharpenStrength = call.argument<Double>("sharpenStrength") ?: 1.0
                    val blurKernelWidth = call.argument<Double>("blurKernelWidth") ?: 3.0
                    val blurKernelHeight = call.argument<Double>("blurKernelHeight") ?: 3.0

                    if (imageBytes != null) {
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        val mat = bitmapToMat(bitmap)
                        val processedMat = processImage(mat, gamma, useBilateralFilter, d, sigmaColor, sigmaSpace, useSharpening, sharpenStrength, blurKernelWidth, blurKernelHeight)
                        val processedBitmap = matToBitmap(processedMat)
                        val outputStream = ByteArrayOutputStream()
                        val byteArray = outputStream.toByteArray()
                        result.success(byteArray)
                    } else {
                        result.error("INVALID_ARGUMENT", "Missing or invalid image data", null)
                    }
                }

                "isImageQualityAcceptable" -> {
                    val snr = call.argument<Double>("snr") ?: 0.0
                    val contrast = call.argument<Double>("contrast") ?: 0.0
                    val resolution = call.argument<String>("resolution") ?: "0x0"
                    val isAcceptable = isImageQualityAcceptable(snr, contrast, resolution)
                    result.success(isAcceptable)
                }

                "calculateContrast" -> {
                    val imageBytes = call.argument<ByteArray>("image")
                    if (imageBytes != null) {
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        val mat = bitmapToMat(bitmap)
                        result.success(calculateContrast(mat))
                    } else {
                        result.error("INVALID_ARGUMENT", "Missing or invalid image data", null)
                    }
                }

                "calculateSNR" -> {
                    val imageBytes = call.argument<ByteArray>("image")
                    if (imageBytes != null) {
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        val mat = bitmapToMat(bitmap)
                        result.success(calculateSNR(mat))
                    } else {
                        result.error("INVALID_ARGUMENT", "Missing or invalid image data", null)
                    }
                }

                "calculateResolution" -> {
                    val imageBytes = call.argument<ByteArray>("image")
                    if (imageBytes != null) {
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        val mat = bitmapToMat(bitmap)
                        result.success(calculateResolution(mat))
                    } else {
                        result.error("INVALID_ARGUMENT", "Missing or invalid image data", null)
                    }
                }

                "calculateGlare" -> {
                    val imageBytes = call.argument<ByteArray>("image")
                    val threshold = call.argument<Double>("threshold") ?: 230.0
                    val minGlareArea = call.argument<Double>("minGlareArea") ?: 500.0
                    if (imageBytes != null) {
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        val mat = bitmapToMat(bitmap)
                        result.success(calculateGlare(mat, threshold, minGlareArea))
                    } else {
                        result.error("INVALID_ARGUMENT", "Missing or invalid image data", null)
                    }
                }
                "processFontCard" -> {
                    val imageBytes = call.argument<ByteArray>("image")
                    val snr = call.argument<Double>("snr") ?: 0.0
                    val contrast = call.argument<Double>("contrast") ?: 0.0
                    val resolution = call.argument<String>("resolution") ?: "0x0"
                    val gamma = call.argument<Double>("gamma") ?: 1.0
                    val useBilateralFilter = call.argument<Boolean>("useBilateralFilter") ?: true
                    val d = call.argument<Int>("d") ?: 9
                    val sigmaColor = call.argument<Double>("sigmaColor") ?: 75.0
                    val sigmaSpace = call.argument<Double>("sigmaSpace") ?: 75.0
                    val useSharpening = call.argument<Boolean>("useSharpening") ?: true
                    val sharpenStrength = call.argument<Double>("sharpenStrength") ?: 1.0
                    val blurKernelWidth = call.argument<Double>("blurKernelWidth") ?: 3.0
                    val blurKernelHeight = call.argument<Double>("blurKernelHeight") ?: 3.0

                    if (imageBytes != null) {
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        val mat = bitmapToMat(bitmap)

                        val base64String = processImageFontCard(
                            snr, contrast, resolution, mat, gamma,
                            useBilateralFilter, d, sigmaColor, sigmaSpace,
                            useSharpening, sharpenStrength, blurKernelWidth, blurKernelHeight
                        )

                            result.success(base64String)
                    } else {
                            result.error("INVALID_ARGUMENT", "Missing or invalid image data", null)
                    }
                }

                "processBackCard" -> {
                    val imageBytes = call.argument<ByteArray>("image")
                    val snr = call.argument<Double>("snr") ?: 0.0
                    val contrast = call.argument<Double>("contrast") ?: 0.0
                    val resolution = call.argument<String>("resolution") ?: "0x0"
                    val gamma = call.argument<Double>("gamma") ?: 1.8
                    val useBilateralFilter = call.argument<Boolean>("useBilateralFilter") ?: true
                    val d = call.argument<Int>("d") ?: 9
                    val sigmaColor = call.argument<Double>("sigmaColor") ?: 75.0
                    val sigmaSpace = call.argument<Double>("sigmaSpace") ?: 75.0
                    val useSharpening = call.argument<Boolean>("useSharpening") ?: true
                    val sharpenStrength = call.argument<Double>("sharpenStrength") ?: 1.0
                    val blurKernelWidth = call.argument<Double>("blurKernelWidth") ?: 3.0
                    val blurKernelHeight = call.argument<Double>("blurKernelHeight") ?: 3.0

                    if (imageBytes != null) {
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        val mat = bitmapToMat(bitmap)

                        val base64String = processImageBackCard(
                            snr, contrast, resolution, mat, gamma,
                            useBilateralFilter, d, sigmaColor, sigmaSpace,
                            useSharpening, sharpenStrength, blurKernelWidth, blurKernelHeight
                        )

                            result.success(base64String)
                    } else {
                            result.error("INVALID_ARGUMENT", "Missing or invalid image data", null)
                    }
                }

                else -> result.notImplemented()
            }
        } catch (e: Exception) {
            result.error("PROCESSING_ERROR", "Error processing image: ${e.message}", null)
        }
    }


    private fun isImageQualityAcceptable(snr: Double, contrast: Double, resolution: String): Boolean {
        val (width, height) = resolution.split("x").map { it.toInt() }
        val minResolution = 500  // Minimum acceptable resolution for OCR
        val snrThreshold = 10.0  // Minimum SNR threshold
        val contrastThreshold = 50.0  // Minimum contrast threshold

        if (width < minResolution || height < minResolution) {
            throw IllegalArgumentException("Image resolution is too low ($resolution). Skipping preprocessing.")
        }

        if (snr < snrThreshold || contrast < contrastThreshold) {
            println("Image quality is medium (SNR: $snr, Contrast: $contrast). Needs preprocessing.")
            return false // Indicates preprocessing is needed
        }

        println("Image quality is sufficient (SNR: $snr, Contrast: $contrast). Skipping preprocessing.")
        return true // No preprocessing needed
    }
    private fun bitmapToMat(bitmap: Bitmap): Mat {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)
        return mat
    }
    private fun matToBitmap(mat: Mat): Bitmap {
        val bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, bitmap)
        return bitmap
    }
    private fun processImage(
        inputMat: Mat,
        gamma: Double = 1.0, // Default gamma correction value (1.0 = No Change)
        useBilateralFilter: Boolean = true, // Enable/Disable Bilateral Filter
        d: Int = 9, // Diameter of Bilateral Filter
        sigmaColor: Double = 75.0, // Sigma Color for Bilateral Filter
        sigmaSpace: Double = 75.0, // Sigma Space for Bilateral Filter
        useSharpening: Boolean = true, // Enable/Disable Sharpening
        sharpenStrength: Double = 1.0, // Strength of Unsharp Mask
        blurKernelWidth: Double = 3.0, // Width of Gaussian Blur Kernel
        blurKernelHeight: Double = 3.0 // Height of Gaussian Blur Kernel
    ): Mat {
        println(
            "Applying preprocessing with parameters: " +
                    "gamma=$gamma, bilateralFilter=$useBilateralFilter, " +
                    "sharpening=$useSharpening, d=$d, sigmaColor=$sigmaColor, sigmaSpace=$sigmaSpace, " +
                    "sharpenStrength=$sharpenStrength, blurKernel=${blurKernelWidth}x$blurKernelHeight"
        )

        // Clone the original Mat to avoid modifying it directly
        var processedMat = inputMat.clone()

        // Ensure the input is in the correct color format
        when (processedMat.type()) {
            CvType.CV_8UC4 -> {
                println("Converting from RGBA to BGR")
                Imgproc.cvtColor(processedMat, processedMat, Imgproc.COLOR_RGBA2BGR)
            }
            CvType.CV_8UC1 -> {
                println("Grayscale image, no need to convert")
            }
            else -> {
                println("Unexpected Mat type: ${processedMat.type()}")
            }
        }

        // ✅ Apply optional preprocessing steps
        if (gamma != 1.0) {
            processedMat = applyGammaCorrection(processedMat, gamma)
        }

        if (useBilateralFilter) {
            processedMat = reduceNoiseWithBilateral(processedMat, d, sigmaColor, sigmaSpace)
        }

        if (useSharpening) {
            processedMat = enhanceSharpenUnsharpMask(
                processedMat, sharpenStrength, Size(blurKernelWidth, blurKernelHeight)
            )
        }

        // Convert back to RGBA if needed
        if (processedMat.type() == CvType.CV_8UC3) {
            println("Converting from BGR to RGBA")
            Imgproc.cvtColor(processedMat, processedMat, Imgproc.COLOR_BGR2RGBA)
        }

        println("Preprocessing completed.")
        return processedMat
    }

    private fun processImageFontCard(
        snr: Double, contrast: Double, resolution: String,
        inputMat: Mat,
        gamma: Double = 1.0, // Default gamma correction value (1.0 = No Change)
        useBilateralFilter: Boolean = true, // Enable/Disable Bilateral Filter
        d: Int = 9, // Diameter of Bilateral Filter
        sigmaColor: Double = 75.0, // Sigma Color for Bilateral Filter
        sigmaSpace: Double = 75.0, // Sigma Space for Bilateral Filter
        useSharpening: Boolean = true, // Enable/Disable Sharpening
        sharpenStrength: Double = 1.0, // Strength of Unsharp Mask
        blurKernelWidth: Double = 3.0, // Width of Gaussian Blur Kernel
        blurKernelHeight: Double = 3.0 // Height of Gaussian Blur Kernel
    ): String {
        println("Processing Font Card - Checking Image Quality (SNR: $snr, Contrast: $contrast, Resolution: $resolution)")

        // **Step 1: Check Image Quality**
        val isQualityGood = isImageQualityAcceptable(snr, contrast, resolution)

        // **Step 2: Apply Preprocessing Only if Quality is Bad**
        val processedMat = if (!isQualityGood) {
            println("Font Card Image Quality is Low - Applying Preprocessing...")

            // Clone to avoid modifying the original Mat
            var tempMat = inputMat.clone()

            // Ensure correct color format
            when (tempMat.type()) {
                CvType.CV_8UC4 -> {
                    println("Converting from RGBA to BGR")
                    Imgproc.cvtColor(tempMat, tempMat, Imgproc.COLOR_RGBA2BGR)
                }
                CvType.CV_8UC1 -> {
                    println("Grayscale image detected, no need for conversion")
                }
                else -> {
                    println("Unexpected Mat type: ${tempMat.type()}")
                }
            }

            // **Apply Processing**
            if (gamma != 1.0) {
                tempMat = applyGammaCorrection(tempMat, gamma)
            }
            if (useBilateralFilter) {
                tempMat = reduceNoiseWithBilateral(tempMat, d, sigmaColor, sigmaSpace)
            }
            if (useSharpening) {
                tempMat = enhanceSharpenUnsharpMask(tempMat, sharpenStrength, Size(blurKernelWidth, blurKernelHeight))
            }

            // Convert back to RGBA if needed
            if (tempMat.type() == CvType.CV_8UC3) {
                println("Converting from BGR to RGBA")
                Imgproc.cvtColor(tempMat, tempMat, Imgproc.COLOR_BGR2RGBA)
            }

            println("Font Card Processing Completed.")
            tempMat
        } else {
            println("Font Card Image Quality is Good - Skipping Processing.")
            inputMat
        }

        // **Step 3: Convert Processed Mat to Base64**
        return convertMatToBase64(processedMat)
    }
    private fun processImageBackCard(
        snr: Double, contrast: Double, resolution: String,
        inputMat: Mat,
        gamma: Double = 1.0, // Default gamma correction value (1.0 = No Change)
        useBilateralFilter: Boolean = true, // Enable/Disable Bilateral Filter
        d: Int = 9, // Diameter of Bilateral Filter
        sigmaColor: Double = 75.0, // Sigma Color for Bilateral Filter
        sigmaSpace: Double = 75.0, // Sigma Space for Bilateral Filter
        useSharpening: Boolean = true, // Enable/Disable Sharpening
        sharpenStrength: Double = 1.0, // Strength of Unsharp Mask
        blurKernelWidth: Double = 3.0, // Width of Gaussian Blur Kernel
        blurKernelHeight: Double = 3.0 // Height of Gaussian Blur Kernel
    ): String {
        println("Processing Font Card - Checking Image Quality (SNR: $snr, Contrast: $contrast, Resolution: $resolution)")

        // **Step 1: Check Image Quality**
        val isQualityGood = isImageQualityAcceptable(snr, contrast, resolution)

        // **Step 2: Apply Preprocessing Only if Quality is Bad**
        val processedMat = if (!isQualityGood) {
            println("Font Card Image Quality is Low - Applying Preprocessing...")

            // Clone to avoid modifying the original Mat
            var tempMat = inputMat.clone()

            // Ensure correct color format
            when (tempMat.type()) {
                CvType.CV_8UC4 -> {
                    println("Converting from RGBA to BGR")
                    Imgproc.cvtColor(tempMat, tempMat, Imgproc.COLOR_RGBA2BGR)
                }
                CvType.CV_8UC1 -> {
                    println("Grayscale image detected, no need for conversion")
                }
                else -> {
                    println("Unexpected Mat type: ${tempMat.type()}")
                }
            }

            // **Apply Processing**
            if (gamma != 1.0) {
                tempMat = applyGammaCorrection(tempMat, gamma)
            }
            if (useBilateralFilter) {
                tempMat = reduceNoiseWithBilateral(tempMat, d, sigmaColor, sigmaSpace)
            }
            if (useSharpening) {
                tempMat = enhanceSharpenUnsharpMask(tempMat, sharpenStrength, Size(blurKernelWidth, blurKernelHeight))
            }

            // Convert back to RGBA if needed
            if (tempMat.type() == CvType.CV_8UC3) {
                println("Converting from BGR to RGBA")
                Imgproc.cvtColor(tempMat, tempMat, Imgproc.COLOR_BGR2RGBA)
            }

            println("Font Card Processing Completed.")
            tempMat
        } else {
            println("Font Card Image Quality is Good - Skipping Processing.")
            inputMat
        }

        // **Step 3: Convert Processed Mat to Base64**
        return convertMatToBase64(processedMat)
    }

    private fun convertMatToBase64(mat: Mat): String {
        val processedBitmap = matToBitmap(mat)
        val outputStream = ByteArrayOutputStream()
        processedBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()

        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }


    fun applyGammaCorrection(image: Mat, gamma: Double = 1.0): Mat {
        require(gamma in 1.0..5.0) { "Gamma must be between 1.0 and 5.0" }

        val invGamma = 1.0 / gamma
        val lut = Mat(1, 256, CvType.CV_8U)

        for (i in 0..255) {
            lut.put(0, i, ((i / 255.0).pow(invGamma) * 255).toInt().toDouble())
        }

        val correctedImage = Mat()
        Core.LUT(image, lut, correctedImage)
        return correctedImage
    }

    fun reduceNoiseWithBilateral(
        mat: Mat,
        d: Int = 9,
        sigmaColor: Double = 75.0,
        sigmaSpace: Double = 75.0
    ): Mat {
        require(d >= 1) { "d (diameter) must be at least 1" }
        require(sigmaColor >= 10) { "sigmaColor must be at least 10" }
        require(sigmaSpace >= 10) { "sigmaSpace must be at least 10" }

        println("Applying Bilateral Filter with d=$d, sigmaColor=$sigmaColor, sigmaSpace=$sigmaSpace")

        val output = Mat()
        Imgproc.bilateralFilter(mat, output, d, sigmaColor, sigmaSpace)
        return output
    }


    fun enhanceSharpenUnsharpMask(
        mat: Mat,
        strength: Double = 1.0,
        blurKernel: Size = Size(3.0, 3.0)
    ): Mat {
        require(strength in 0.1..3.0) { "Sharpening strength must be between 0.1 and 3.0" }
        require(blurKernel.width >= 3.0 && blurKernel.height >= 3.0) { "Blur kernel size must be at least 3x3" }

        println("Applying Unsharp Mask Sharpening with strength=$strength, blurKernel=${blurKernel.width}x${blurKernel.height}")

        val blurred = Mat()
        Imgproc.GaussianBlur(mat, blurred, blurKernel, 0.0)

        val sharpened = Mat()
        Core.addWeighted(mat, 1.0 + strength, blurred, -strength, 0.0, sharpened)
        return sharpened
    }

    fun calculateContrast(mat: Mat): Double {
        val grayMat = Mat()
        Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_BGR2GRAY)
        val minMaxLoc = Core.minMaxLoc(grayMat)
        grayMat.release()
        return minMaxLoc.maxVal - minMaxLoc.minVal
    }
    fun calculateSNR(mat: Mat): Double {
        val grayMat = Mat()
        Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_BGR2GRAY)

        val meanStdDev = MatOfDouble()
        val stdDev = MatOfDouble()
        Core.meanStdDev(grayMat, meanStdDev, stdDev)

        grayMat.release()

        val mean = meanStdDev.toArray().firstOrNull() ?: 0.0
        val std = stdDev.toArray().firstOrNull() ?: 1.0 // Avoid division by zero
        return mean / std
    }
    fun calculateResolution(mat: Mat): String {
        return "${mat.cols()}x${mat.rows()}"
    }
    private fun calculateGlare(mat: Mat, threshold: Double = 230.0, minGlareArea: Double = 500.0): Double {
        require(threshold in 200.0..255.0) { "Threshold must be between 200 and 255" }
        require(minGlareArea >= 100) { "minGlareArea must be at least 100 to avoid noise" }

        // Convert to grayscale if necessary
        var gray = Mat()
        when (mat.type()) {
            CvType.CV_8UC4 -> Imgproc.cvtColor(mat, gray, Imgproc.COLOR_RGBA2GRAY)
            CvType.CV_8UC3 -> Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY)
            CvType.CV_8UC1 -> gray = mat.clone() // Already grayscale
            else -> throw IllegalArgumentException("Unsupported image format: ${mat.type()}")
        }

        // Apply threshold to detect bright regions (potential glare)
        val binary = Mat()
        Imgproc.threshold(gray, binary, threshold, 255.0, Imgproc.THRESH_BINARY)

        // Find contours of bright regions
        val contours = ArrayList<MatOfPoint>()
        Imgproc.findContours(binary, contours, Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        // Calculate total glare area
        var glareArea = 0.0
        for (contour in contours) {
            val area = Imgproc.contourArea(contour)
            if (area > minGlareArea) { // Ignore small reflections/noise
                glareArea += area
            }
        }

        // Calculate total image area
        val totalArea = (mat.width() * mat.height()).toDouble()
        val glarePercentage = if (totalArea > 0) (glareArea / totalArea) * 100 else 0.0

        // Cleanup Mats
        gray.release()
        binary.release()

        return glarePercentage
    }

}
