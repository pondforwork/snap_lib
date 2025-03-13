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
                    val d = call.argument<Int>("d") ?: 9
                    val sigmaColor = call.argument<Double>("sigmaColor") ?: 75.0
                    val sigmaSpace = call.argument<Double>("sigmaSpace") ?: 75.0
                    val sharpenStrength = call.argument<Double>("sharpenStrength") ?: 1.0
                    val blurKernelSize = call.argument<Double>("blurKernelSize") ?: 3.0
                    val applyGamma = call.argument<Boolean>("applyGamma") ?: true
                    val reduceNoise = call.argument<Boolean>("reduceNoise") ?: true
                    val enhanceSharpen = call.argument<Boolean>("enhanceSharpen") ?: true
                    val returnBase64 = call.argument<Boolean>("returnBase64") ?: true

                    if (imageBytes != null) {
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        val mat = bitmapToMat(bitmap)


                        val processedMat = processImage(
                            mat, gamma, d, sigmaColor, sigmaSpace,
                            sharpenStrength, blurKernelSize,
                            applyGamma, reduceNoise, enhanceSharpen
                        )

                        val output = if (returnBase64) convertMatToBase64(processedMat) else convertMatToByteArray(processedMat)
                        result.success(output)
                    } else {
                        result.error("INVALID_ARGUMENT", "Missing or invalid image data", null)
                    }
                }

                "processFontCard" -> {
                    val imageBytes = call.argument<ByteArray>("image")
                    val snr = call.argument<Double>("snr") ?: 0.0
                    val contrast = call.argument<Double>("contrast") ?: 0.0
                    val brightness = call.argument<Double>("brightness") ?: 0.0
                    val glarePercent = call.argument<Double>("glarePercent") ?: 0.0
                    val resolution = call.argument<String>("resolution") ?: "0x0"
                    val gamma = call.argument<Double>("gamma") ?: 1.0
                    val d = call.argument<Int>("d") ?: 9
                    val sigmaColor = call.argument<Double>("sigmaColor") ?: 75.0
                    val sigmaSpace = call.argument<Double>("sigmaSpace") ?: 75.0
                    val sharpenStrength = call.argument<Double>("sharpenStrength") ?: 1.0
                    val blurKernelSize = call.argument<Double>("blurKernelSize") ?: 3.0
                    val returnBase64 = call.argument<Boolean>("returnBase64") ?: true
                    val useGammaCorrection = call.argument<Boolean>("useGammaCorrection") ?: true
                    val useBilateralFilter = call.argument<Boolean>("useBilateralFilter") ?: true
                    val useSharpening = call.argument<Boolean>("useSharpening") ?: true

                    if (imageBytes != null) {
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        val mat = bitmapToMat(bitmap)

                        val processedMat = processImageFontCard(
                            snr = snr, contrast = contrast, brightness = brightness, glarePercent = glarePercent,
                            resolution = resolution, inputMat = mat, gamma = gamma,
                            d = d, sigmaColor = sigmaColor, sigmaSpace = sigmaSpace,
                            useSharpening = useSharpening, useBilateralFilter = useBilateralFilter, useGammaCorrection = useGammaCorrection,
                            sharpenStrength = sharpenStrength, blurKernelSize = blurKernelSize
                        )

                        val output = if (returnBase64) convertMatToBase64(processedMat) else convertMatToByteArray(processedMat)
                        result.success(output)
                    } else {
                        result.error("INVALID_ARGUMENT", "Missing or invalid image data", null)
                    }
                }

                "processBackCard" -> {
                    val imageBytes = call.argument<ByteArray>("image")
                    val snr = call.argument<Double>("snr") ?: 0.0
                    val contrast = call.argument<Double>("contrast") ?: 0.0
                    val brightness = call.argument<Double>("brightness") ?: 0.0
                    val glarePercent = call.argument<Double>("glarePercent") ?: 1.0
                    val resolution = call.argument<String>("resolution") ?: "0x0"
                    val gamma = call.argument<Double>("gamma") ?: 1.8
                    val d = call.argument<Int>("d") ?: 9
                    val sigmaColor = call.argument<Double>("sigmaColor") ?: 75.0
                    val sigmaSpace = call.argument<Double>("sigmaSpace") ?: 75.0
                    val useSharpening = call.argument<Boolean>("useSharpening") ?: true
                    val useBilateralFilter = call.argument<Boolean>("useBilateralFilter") ?: true
                    val sharpenStrength = call.argument<Double>("sharpenStrength") ?: 1.0
                    val blurKernelSize = call.argument<Double>("blurKernelSize") ?: 3.0
                    val returnBase64 = call.argument<Boolean>("returnBase64") ?: true

                    if (imageBytes != null) {
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        val mat = bitmapToMat(bitmap)

                        val processedMat = processImageBackCard(
                            snr = snr, contrast = contrast, brightness = brightness, glarePercent = glarePercent,
                            resolution = resolution, inputMat = mat, gamma = gamma,
                            d = d, sigmaColor = sigmaColor, sigmaSpace = sigmaSpace,
                            useSharpening = useSharpening, useBilateralFilter = useBilateralFilter,
                            sharpenStrength = sharpenStrength, blurKernelSize = blurKernelSize
                        )

                        val output = if (returnBase64) convertMatToBase64(processedMat) else convertMatToByteArray(processedMat)
                        result.success(output)
                    } else {
                        result.error("INVALID_ARGUMENT", "Missing or invalid image data", null)
                    }
                }

                "isImageQualityAcceptable" -> {
                    val snr = call.argument<Double>("snr") ?: 0.0
                    val contrast = call.argument<Double>("contrast") ?: 0.0
                    val brightness = call.argument<Double>("brightness") ?: 0.0
                    val glarePercent = call.argument<Double>("glarePercent") ?: 0.0
                    val resolution = call.argument<String>("resolution") ?: "0x0"

                    val isAcceptable = isImageQualityAcceptable(
                        snr, contrast, brightness, glarePercent, resolution
                    )
                    result.success(isAcceptable)
                }
                "convertMatToBase64" -> {
                    val imageBytes = call.argument<ByteArray>("image")
                    if (imageBytes != null) {
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        val mat = bitmapToMat(bitmap)

                        val base64String = convertMatToBase64(mat)
                        result.success(base64String) // Send Base64 String to Flutter
                    } else {
                        result.error("INVALID_ARGUMENT", "Missing or invalid image data", null)
                    }
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
                "calculateBrightness" -> {
                    val imageBytes = call.argument<ByteArray>("image")
                    if (imageBytes != null) {
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        val mat = bitmapToMat(bitmap)

                        val brightness = calculateBrightness(mat)
                        result.success(brightness) // Return brightness to Flutter
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
                "applyGammaCorrection" -> {
                    val imageBytes = call.argument<ByteArray>("image")
                    val gamma = call.argument<Double>("gamma") ?: 1.0
                    val returnBase64 = call.argument<Boolean>("returnBase64") ?: true

                    if (imageBytes != null) {
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        val mat = bitmapToMat(bitmap)

                        val processedMat = applyGammaCorrection(mat, gamma)

                        val output = if (returnBase64) {
                            convertMatToBase64(processedMat)
                        } else {
                            convertMatToByteArray(processedMat)
                        }

                        result.success(output)
                    } else {
                        result.error("INVALID_ARGUMENT", "Missing or invalid image data", null)
                    }
                }
                "enhanceSharpen" -> {
                    val imageBytes = call.argument<ByteArray>("image")
                    val strength = call.argument<Double>("strength") ?: 1.5
                    val kernelSize = call.argument<Double>("kernelSize") ?: 3.0
                    val returnBase64 = call.argument<Boolean>("returnBase64") ?: true

                    if (imageBytes != null) {
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        val mat = bitmapToMat(bitmap)

                        val processedMat = enhanceSharpenUnsharpMask(mat, strength, kernelSize)

                        val output = if (returnBase64) convertMatToBase64(processedMat) else convertMatToByteArray(processedMat)
                        result.success(output)
                    } else {
                        result.error("INVALID_ARGUMENT", "Missing or invalid image data", null)
                    }
                }
                "reduceNoise" -> {
                    val imageBytes = call.argument<ByteArray>("image")
                    val d = call.argument<Int>("d") ?: 9
                    val sigmaColor = call.argument<Double>("sigmaColor") ?: 75.0
                    val sigmaSpace = call.argument<Double>("sigmaSpace") ?: 75.0
                    val returnBase64 = call.argument<Boolean>("returnBase64") ?: true

                    if (imageBytes != null) {
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        val mat = bitmapToMat(bitmap)

                        val processedMat = reduceNoiseWithBilateral(mat, d, sigmaColor, sigmaSpace)

                        val output = if (returnBase64) convertMatToBase64(processedMat) else convertMatToByteArray(processedMat)
                        result.success(output)
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
    private fun isImageQualityAcceptable(
        snr: Double,
        contrast: Double,
        brightness: Double,
        glarePercent: Double,
        resolution: String,
        minResolution: Int = 500,
        snrThreshold: Double = 3.0,
        contrastThreshold: Double = 50.0,
        maxBrightness: Int = 200,
        maxGlarePercent: Double = 1.0
    ): Boolean {
        try {
            require(minResolution > 0) { "minResolution must be greater than 0." }
            require(snrThreshold > 0) { "snrThreshold must be greater than 0." }
            require(contrastThreshold > 0) { "contrastThreshold must be greater than 0." }
            require(maxBrightness in 1..255) { "maxBrightness must be between 1 and 255." }
            require(maxGlarePercent in 0.0..100.0) { "maxGlarePercent must be between 0 and 100." }
            require(glarePercent > 0) { "glarePercent must be greater than 0." }
            val (width, height) = resolution.split("x").map { it.toIntOrNull() ?: 0 }

            if (width < minResolution || height < minResolution) {
                throw IllegalArgumentException("Image resolution is too low ($resolution). Minimum required is ${minResolution}x${minResolution}.")
            }

            if (snr < snrThreshold || contrast < contrastThreshold) {
                println("Image quality is medium (SNR: $snr, Contrast: $contrast). Needs preprocessing.")
                return false
            }

            if (brightness > maxBrightness || glarePercent > maxGlarePercent) {
                println("Image quality is low due to brightness ($brightness) or glare ($glarePercent%). Needs preprocessing.")
                return false
            }

            println("Image quality is sufficient (SNR: $snr, Contrast: $contrast). Skipping preprocessing.")
            return true

        } catch (e: Exception) {
            println("Error validating image quality: ${e.message}")
            throw IllegalArgumentException("Invalid input: ${e.message}")
        }
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


    fun processImage(

        inputMat: Mat,
        gamma: Double = 1.0,
        d: Int = 9,
        sigmaColor: Double = 75.0,
        sigmaSpace: Double = 75.0,
        sharpenStrength: Double = 1.0,
        blurKernelSize: Double = 3.0,
        applyGamma: Boolean = true,
        reduceNoise: Boolean = true,
        enhanceSharpen: Boolean = true
    ): Mat {
        require(gamma > 0) { "Gamma must be greater than 0." }
        require(d > 0) { "Bilateral filter parameter d must be greater than 0." }
        require(sigmaColor > 0) { "sigmaColor must be greater than 0." }
        require(sigmaSpace > 0) { "sigmaSpace must be greater than 0." }
        require(sharpenStrength in 0.1..3.0) { "Sharpen strength must be between 0.1 and 3.0." }
        require(blurKernelSize >= 3.0 && blurKernelSize % 2 == 1.0) { "Blur kernel size must be an odd number and at least 3." }

        var processedMat = inputMat.clone()

        when (processedMat.type()) {
            CvType.CV_8UC4 -> Imgproc.cvtColor(processedMat, processedMat, Imgproc.COLOR_RGBA2BGR)
            CvType.CV_8UC1 -> {}
            else -> println("⚠️ Unexpected Mat type: ${processedMat.type()}")
        }

        if (applyGamma) processedMat = applyGammaCorrection(processedMat, gamma)
        if (reduceNoise) processedMat = reduceNoiseWithBilateral(processedMat, d, sigmaColor, sigmaSpace)
        if (enhanceSharpen) processedMat = enhanceSharpenUnsharpMask(processedMat, sharpenStrength, blurKernelSize)

        if (processedMat.type() == CvType.CV_8UC3) Imgproc.cvtColor(processedMat, processedMat, Imgproc.COLOR_BGR2RGBA)

        println("✅ Preprocessing completed.")
        return processedMat
    }


    private fun processImageFontCard(
        snr: Double, contrast: Double, brightness: Double, glarePercent: Double, resolution: String,
        inputMat: Mat,
        gamma: Double = 1.0,
        d: Int = 9,
        sigmaColor: Double = 75.0,
        sigmaSpace: Double = 75.0,
        useSharpening: Boolean = true,
        useBilateralFilter: Boolean = true,
        useGammaCorrection: Boolean = true,
        sharpenStrength: Double = 1.0,
        blurKernelSize: Double = 3.0,
        minResolution: Int = 500,
        snrThreshold: Double = 3.0,
        contrastThreshold: Double = 50.0,
        maxBrightness: Int = 200,
        maxGlarePercent: Double = 1.0
    ): Mat {
        return processImageCard(
            "Font Card", snr, contrast, brightness, glarePercent, resolution, inputMat,
            gamma, d, sigmaColor, sigmaSpace, useSharpening, useBilateralFilter, useGammaCorrection,
            sharpenStrength, blurKernelSize, minResolution, snrThreshold, contrastThreshold, maxBrightness, maxGlarePercent
        )
    }

    private fun processImageBackCard(
        snr: Double, contrast: Double, brightness: Double, glarePercent: Double, resolution: String,
        inputMat: Mat,
        gamma: Double = 1.8,
        d: Int = 9,
        sigmaColor: Double = 75.0,
        sigmaSpace: Double = 75.0,
        useSharpening: Boolean = true,
        useBilateralFilter: Boolean = true,
        sharpenStrength: Double = 1.0,
        blurKernelSize: Double = 3.0,
        minResolution: Int = 500,
        snrThreshold: Double = 3.0,
        contrastThreshold: Double = 50.0,
        maxBrightness: Int = 200,
        maxGlarePercent: Double = 1.0
    ): Mat {
        return processImageCard(
            "Back Card", snr, contrast, brightness, glarePercent, resolution, inputMat,
            gamma, d, sigmaColor, sigmaSpace, useSharpening, useBilateralFilter, gamma != 1.0,
            sharpenStrength, blurKernelSize, minResolution, snrThreshold, contrastThreshold, maxBrightness, maxGlarePercent
        )
    }

    private fun processImageCard(
        cardType: String,
        snr: Double, contrast: Double, brightness: Double, glarePercent: Double, resolution: String,
        inputMat: Mat,
        gamma: Double,
        d: Int,
        sigmaColor: Double,
        sigmaSpace: Double,
        useSharpening: Boolean,
        useBilateralFilter: Boolean,
        useGammaCorrection: Boolean,
        sharpenStrength: Double,
        blurKernelSize: Double,
        minResolution: Int,
        snrThreshold: Double,
        contrastThreshold: Double,
        maxBrightness: Int,
        maxGlarePercent: Double
    ): Mat {
        println("Processing $cardType - Checking Image Quality (SNR: $snr, Contrast: $contrast, Brightness: $brightness, Glare: $glarePercent, Resolution: $resolution)")

        val isQualityGood = isImageQualityAcceptable(snr, contrast, brightness, glarePercent, resolution, minResolution, snrThreshold, contrastThreshold, maxBrightness, maxGlarePercent)

        val processedMat = if (!isQualityGood) {
            println("$cardType Image Quality is Low - Applying Preprocessing...")
            var tempMat = inputMat.clone()

            when (tempMat.type()) {
                CvType.CV_8UC4 -> Imgproc.cvtColor(tempMat, tempMat, Imgproc.COLOR_RGBA2BGR)
                CvType.CV_8UC1 -> println("Grayscale image detected, no need for conversion")
                else -> println("Unexpected Mat type: ${tempMat.type()}")
            }

            if (useGammaCorrection) tempMat = applyGammaCorrection(tempMat, gamma)
            if (useBilateralFilter) tempMat = reduceNoiseWithBilateral(tempMat, d, sigmaColor, sigmaSpace)
            if (useSharpening) tempMat = enhanceSharpenUnsharpMask(tempMat, sharpenStrength, blurKernelSize)

            if (tempMat.type() == CvType.CV_8UC3) {
                Imgproc.cvtColor(tempMat, tempMat, Imgproc.COLOR_BGR2RGBA)
            }

            println("$cardType Processing Completed.")
            tempMat
        } else {
            println("$cardType Image Quality is Good - Skipping Processing.")
            inputMat
        }

        return processedMat
    }



    fun convertMatToBase64(mat: Mat): String {

        val processedBitmap = matToBitmap(mat)
        val outputStream = ByteArrayOutputStream()
        processedBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()

        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

     fun calculateBrightness(mat: Mat): Double {
        // Convert to grayscale for brightness analysis
        val grayMat = Mat()
        Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_BGR2GRAY)

        // Compute the mean brightness
        val meanScalar = org.opencv.core.Core.mean(grayMat)
        grayMat.release() // Free memory

        return meanScalar.`val`[0] // Return brightness value
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

        val tempMat = mat.clone()

        when (tempMat.type()) {
            CvType.CV_8UC4 -> {
                Imgproc.cvtColor(tempMat, tempMat, Imgproc.COLOR_RGBA2BGR) // Convert to BGR
            }
            CvType.CV_8UC1 -> {
                println("Grayscale image detected, no need for conversion")
            }
            else -> {
                println("Unexpected Mat type: ${tempMat.type()}")
            }
        }

        println("Applying Bilateral Filter with d=$d, sigmaColor=$sigmaColor, sigmaSpace=$sigmaSpace")

        val output = Mat()
        Imgproc.bilateralFilter(tempMat, output, d, sigmaColor, sigmaSpace)

        // Convert back to RGBA to maintain correct colors
        if (output.type() == CvType.CV_8UC3) {
            Imgproc.cvtColor(output, output, Imgproc.COLOR_BGR2RGBA)
        }

        return output
    }



    fun enhanceSharpenUnsharpMask(
        mat: Mat,
        strength: Double = 1.0,
        kernelSize: Double = 3.0  // Single parameter for square kernel
    ): Mat {
        require(strength in 0.1..3.0) { "Sharpening strength must be between 0.1 and 3.0" }
        require(kernelSize >= 3.0 && kernelSize % 2 == 1.0) { "Kernel size must be an odd number and at least 3" }

        println("Applying Unsharp Mask Sharpening with strength=$strength, kernelSize=${kernelSize}x${kernelSize}")

        val blurred = Mat()
        val blurKernel = Size(kernelSize, kernelSize) // ✅ Ensure square kernel
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
     fun calculateGlare(mat: Mat, threshold: Double = 230.0, minGlareArea: Double = 500.0): Double {
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


    private fun convertMatToBase64(imageBytes: ByteArray): String {
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        val mat = bitmapToMat(bitmap)
        val processedBitmap = matToBitmap(mat)

        val outputStream = ByteArrayOutputStream()
        processedBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }





    private fun matToByteArray(mat: Mat): ByteArray {
        val bitmap = matToBitmap(mat)
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }
    private fun convertMatToByteArray(mat: Mat): ByteArray {
        val processedBitmap = matToBitmap(mat) // Convert Mat to Bitmap
        val outputStream = ByteArrayOutputStream()

        // Compress to PNG format
        processedBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)

        return outputStream.toByteArray() // Return as ByteArray (Uint8List in Flutter)
    }

}
