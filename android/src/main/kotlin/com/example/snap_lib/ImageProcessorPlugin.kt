package com.example.snap_lib

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Build
import android.util.Base64
import androidx.annotation.RequiresApi
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import org.opencv.android.OpenCVLoader
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.android.Utils
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.pow
import kotlinx.coroutines.*

class ImageProcessorPlugin : FlutterPlugin, MethodChannel.MethodCallHandler {
    private lateinit var channel: MethodChannel

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "image_processor_plugin")
        channel.setMethodCallHandler(this)

        if (!OpenCVLoader.initDebug()) {
            println("OpenCV initialization failed")
        } else {
            println("OpenCV initialized successfully")
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        println("ImageProcessorPlugin has been detached from Flutter Engine.")
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "processImage" -> {
                val imageBytes = call.argument<ByteArray>("image")
                if (imageBytes != null) {
                    GlobalScope.launch(Dispatchers.IO) {
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        val mat = bitmapToMat(bitmap)
                        val snr = calculateSNR(mat)
                        val contrast = calculateContrast(mat)
                        val resolution = calculateResolution(mat)
                        val processedImage = checkAndPreprocessImage(snr, contrast, resolution, mat)
                        val processedBitmap = matToBitmap(processedImage)
                        val outputStream = ByteArrayOutputStream()
                        processedBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                        val byteArray = outputStream.toByteArray()
                        withContext(Dispatchers.Main) {
                            result.success(byteArray)
                        }
                    }
                } else {
                    result.error("ERROR", "Invalid image input", null)
                }
            }

            "calculateBrightness" -> {
                val imageBytes = call.argument<ByteArray>("image")
                if (imageBytes != null) {
                    GlobalScope.launch(Dispatchers.IO) {
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        val mat = bitmapToMat(bitmap)
                        val brightness = calculateBrightness(mat)
                        withContext(Dispatchers.Main) {
                            result.success(brightness)
                        }
                    }
                } else {
                    result.error("ERROR", "Invalid image input", null)
                }
            }

            "calculateGlare" -> {
                val imageBytes = call.argument<ByteArray>("image")
                val maxGlareThreshold = call.argument<Double>("maxGlareThreshold") ?: 255.0
                if (imageBytes != null) {
                    GlobalScope.launch(Dispatchers.IO) {
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        val mat = bitmapToMat(bitmap)
                        val glare = calculateGlare(mat, maxGlareThreshold = maxGlareThreshold)
                        withContext(Dispatchers.Main) {
                            result.success(glare)
                        }
                    }
                } else {
                    result.error("ERROR", "Invalid image input", null)
                }
            }

            "calculateSNR" -> {
                val imageBytes = call.argument<ByteArray>("image")
                if (imageBytes != null) {
                    GlobalScope.launch(Dispatchers.IO) {
                        val mat = bitmapToMat(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size))
                        val snr = calculateSNR(mat)
                        withContext(Dispatchers.Main) {
                            result.success(snr)
                        }
                    }
                } else {
                    result.error("ERROR", "Invalid image input", null)
                }
            }

            "applyGammaCorrection" -> {
                val imageBytes = call.argument<ByteArray>("image")
                val gamma = call.argument<Double>("gamma") ?: 1.8
                if (imageBytes != null) {
                    GlobalScope.launch(Dispatchers.IO) {
                        val mat = bitmapToMat(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size))
                        val correctedImage = applyGammaCorrection(mat, gamma)
                        withContext(Dispatchers.Main) {
                            result.success(correctedImage)
                        }
                    }
                } else {
                    result.error("ERROR", "Invalid image input", null)
                }
            }

            "reduceNoise" -> {
                val imageBytes = call.argument<ByteArray>("image")
                val d = call.argument<Int>("d") ?: 9
                val sigmaColor = call.argument<Double>("sigmaColor") ?: 75.0
                val sigmaSpace = call.argument<Double>("sigmaSpace") ?: 75.0
                if (imageBytes != null) {
                    GlobalScope.launch(Dispatchers.IO) {
                        val mat = bitmapToMat(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size))
                        val noiseReducedImage = reduceNoiseWithBilateral(mat, d, sigmaColor, sigmaSpace)
                        withContext(Dispatchers.Main) {
                            result.success(noiseReducedImage)
                        }
                    }
                } else {
                    result.error("ERROR", "Invalid image input", null)
                }
            }

            "enhanceSharpen" -> {
                val imageBytes = call.argument<ByteArray>("image")
                val strength = call.argument<Double>("strength") ?: 1.5
                val blurKernelWidth = call.argument<Double>("blurKernelWidth") ?: 5.0
                val blurKernelHeight = call.argument<Double>("blurKernelHeight") ?: 5.0
                if (imageBytes != null) {
                    GlobalScope.launch(Dispatchers.IO) {
                        val mat = bitmapToMat(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size))
                        val sharpenedImage = enhanceSharpenUnsharpMask(mat, strength, Size(blurKernelWidth, blurKernelHeight))
                        withContext(Dispatchers.Main) {
                            result.success(sharpenedImage)
                        }
                    }
                } else {
                    result.error("ERROR", "Invalid image input", null)
                }
            }

            "convertMatToBase64" -> {
                val imageBytes = call.argument<ByteArray>("image")
                if (imageBytes != null) {
                    GlobalScope.launch(Dispatchers.IO) {
                        val base64String = convertMatToBase64(imageBytes)
                        withContext(Dispatchers.Main) {
                            result.success(base64String)
                        }
                    }
                } else {
                    result.error("ERROR", "Invalid image input", null)
                }
            }

            "convertMatToFile" -> {
                val imageBytes = call.argument<ByteArray>("image")
                val filePath = call.argument<String>("filePath") ?: "/storage/emulated/0/Download/opencv_image.jpg"
                if (imageBytes != null) {
                    GlobalScope.launch(Dispatchers.IO) {
                        val savedFilePath = convertMatToFile(imageBytes, filePath)
                        withContext(Dispatchers.Main) {
                            result.success(savedFilePath)
                        }
                    }
                } else {
                    result.error("ERROR", "Invalid image input", null)
                }
            }

            else -> result.notImplemented()
        }
    }

    // Convert Mat to Base64
    private fun convertMatToBase64(imageBytes: ByteArray): String {
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        val mat = bitmapToMat(bitmap)
        val processedBitmap = matToBitmap(mat)

        val outputStream = ByteArrayOutputStream()
        processedBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    // Convert Mat to File and save
    private fun convertMatToFile(imageBytes: ByteArray, filePath: String): String {
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        val mat = bitmapToMat(bitmap)
        val processedBitmap = matToBitmap(mat)

        val file = File(filePath)
        val fileOutputStream = FileOutputStream(file)
        processedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
        fileOutputStream.flush()
        fileOutputStream.close()
        return file.absolutePath
    }


    private fun matToBitmap(mat: Mat): Bitmap {
        val bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, bitmap)
        return bitmap
    }
    fun checkAndPreprocessImage(
        snr: Double,
        contrast: Double,
        resolution: String,
        inputMat: Mat,
        minResolution: Int = 500,  // Default minimum resolution for OCR
        snrThreshold: Double = 10.0,  // Default minimum SNR threshold
        contrastThreshold: Double = 50.0,  // Default minimum contrast threshold
        minAllowedSNR: Double = 1.0,  // Absolute minimum SNR allowed
        minAllowedContrast: Double = 10.0,  // Absolute minimum contrast allowed
        minAllowedResolution: Int = 100  // Absolute minimum resolution allowed
    ): Mat {
        val (width, height) = resolution.split("x").map { it.toInt() }

        if (snr < minAllowedSNR || contrast < minAllowedContrast || width < minAllowedResolution || height < minAllowedResolution) {
            println("Image quality is critically low (SNR: $snr, Contrast: $contrast, Resolution: $resolution). Preprocessing is not possible.")
            return inputMat
        }

        if (width < minResolution || height < minResolution) {
            println("Image resolution is too low ($resolution). Skipping preprocessing.")
            return inputMat
        }

        return if (snr < snrThreshold || contrast < contrastThreshold) {
            println("Image quality is medium (SNR: $snr, Contrast: $contrast). Applying preprocessing...")

            var processedMat = inputMat.clone()

            when (processedMat.type()) {
                CvType.CV_8UC4 -> {
                    println("Converting from RGBA to BGR")
                    Imgproc.cvtColor(processedMat, processedMat, Imgproc.COLOR_RGBA2BGR)
                }
                CvType.CV_8UC1 -> println("Grayscale image detected, no need to convert")
                else -> println("Unexpected Mat type: ${processedMat.type()}")
            }

            processedMat = applyGammaCorrection(processedMat, gamma = 1.2)
            processedMat = reduceNoiseWithBilateral(processedMat)
            processedMat = enhanceSharpenUnsharpMask(processedMat)

            if (processedMat.type() == CvType.CV_8UC3) {
                println("Converting from BGR to RGBA")
                Imgproc.cvtColor(processedMat, processedMat, Imgproc.COLOR_BGR2RGBA)
            }

            println("Preprocessing completed.")
            processedMat
        } else {
            println("Image quality is sufficient (SNR: $snr, Contrast: $contrast). Skipping preprocessing.")
            inputMat
        }
    }

    fun calculateBrightness(mat: Mat): Double {
        val gray = Mat()
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY)
        val mean = Core.mean(gray)
        gray.release()
        return mean.`val`[0]
    }

    private fun calculateResolution(mat: Mat): String {
        return "${mat.cols()}x${mat.rows()}"
    }


    fun calculateGlare(
        mat: Mat,
        glareAreaRule: Double = 500.0,
        thresholdValue: Double = 230.0,
        maxGlareThreshold: Double
    ): Double {
        try {
            if (maxGlareThreshold > 255.0) {
                println("Error: maxGlareThreshold ($maxGlareThreshold) exceeds the allowed limit (255.0). Function will not proceed.")
                return -1.0
            }

            val gray = Mat()
            Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY)

            val binary = Mat()
            Imgproc.threshold(gray, binary, thresholdValue, maxGlareThreshold, Imgproc.THRESH_BINARY)

            val contours = ArrayList<MatOfPoint>()
            Imgproc.findContours(binary, contours, Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

            var glareArea = 0.0
            for (contour in contours) {
                val area = Imgproc.contourArea(contour)
                if (area > glareAreaRule) {
                    glareArea += area
                }
            }

            val totalArea = mat.width() * mat.height()
            gray.release()
            binary.release()

            val glarePercentage = (glareArea / totalArea) * 100

            return glarePercentage.coerceAtMost(maxGlareThreshold)

        } catch (e: Exception) {
            println("Exception in calculateGlare: ${e.message}")
            return -1.0
        }
    }



    fun calculateSNR(mat: Mat): Double {
        val grayMat = Mat()
        Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_BGR2GRAY)
        val meanStdDev = MatOfDouble()
        val stdDev = MatOfDouble()
        Core.meanStdDev(grayMat, meanStdDev, stdDev)
        grayMat.release()
        val mean = meanStdDev.toArray().firstOrNull() ?: 0.0
        val std = stdDev.toArray().firstOrNull() ?: 1.0
        return mean / std
    }

    fun applyGammaCorrection(image: Mat, gamma: Double = 1.8): Mat {
        try {
            // ✅ Strict validation: gamma must be between 0.1 and 5.0
            if (gamma < 0.1 || gamma > 5.0) {
                println("Error: Gamma ($gamma) is out of range (0.1 - 5.0). Skipping function.")
                return Mat() // Return an empty Mat to indicate an invalid operation
            }

            // Calculate the inverse of gamma
            val invGamma = 1.0 / gamma

            // Create a lookup table (LUT) for gamma correction
            val lut = Mat(1, 256, CvType.CV_8U)
            for (i in 0..255) {
                lut.put(0, i, ((i / 255.0).pow(invGamma) * 255).toInt().toDouble())
            }

            // Apply gamma correction using LUT
            val correctedImage = Mat()
            Core.LUT(image, lut, correctedImage)

            return correctedImage

        } catch (e: Exception) {
            println("Exception in applyGammaCorrection: ${e.message}")
            return Mat() // Return an empty Mat to indicate an error
        }
    }
    fun reduceNoiseWithBilateral(
        mat: Mat,
        d: Int = 9,
        sigmaColor: Double = 75.0,
        sigmaSpace: Double = 75.0
    ): Mat {
        try {
            // ✅ Strict Validation: Ensure parameters are within acceptable ranges
            if (d <= 0 || sigmaColor < 1.0 || sigmaSpace < 1.0) {
                println("Error: Invalid parameters (d: $d, sigmaColor: $sigmaColor, sigmaSpace: $sigmaSpace). Skipping function.")
                return Mat() // Return an empty Mat to indicate an invalid operation
            }

            val output = Mat()
            Imgproc.bilateralFilter(mat, output, d, sigmaColor, sigmaSpace)
            return output

        } catch (e: Exception) {
            println("Exception in reduceNoiseWithBilateral: ${e.message}")
            return Mat() // Return an empty Mat in case of an error
        }
    }
    fun enhanceSharpenUnsharpMask(
        mat: Mat,
        strength: Double = 1.5,
        blurKernel: Size = Size(5.0, 5.0),
        minStrength: Double = 0.1,
        maxStrength: Double = 5.0,
        minKernelSize: Double = 1.0
    ): Mat {
        try {
            // ✅ Strict Validation: Ensure parameters are within valid ranges
            if (strength < minStrength || strength > maxStrength || blurKernel.width < minKernelSize || blurKernel.height < minKernelSize) {
                println("Error: Invalid parameters (strength: $strength, blurKernel: ${blurKernel.width}x${blurKernel.height}). Skipping function.")
                return Mat() // Return an empty Mat to indicate an invalid operation
            }

            val blurred = Mat()
            Imgproc.GaussianBlur(mat, blurred, blurKernel, 0.0)
            val sharpened = Mat()
            Core.addWeighted(mat, 1.0 + strength, blurred, -strength, 0.0, sharpened)

            return sharpened

        } catch (e: Exception) {
            println("Exception in enhanceSharpenUnsharpMask: ${e.message}")
            return Mat() // Return an empty Mat in case of an error
        }
    }
    fun bitmapToMat(bitmap: Bitmap): Mat {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)
        return mat
    }

    private fun calculateContrast(mat: Mat): Double {
        val gray = Mat()
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY)
        val mean = Core.mean(gray)
        val stdDev = MatOfDouble()
        Core.meanStdDev(gray, MatOfDouble(), stdDev)
        gray.release()
        return stdDev.toArray().firstOrNull() ?: 0.0
    }

}
