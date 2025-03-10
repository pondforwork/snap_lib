package com.example.snap_lib

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.annotation.NonNull
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
                val keepColor = call.argument<Boolean>("keepColor") ?: true

                if (imageBytes != null) {
                    GlobalScope.launch(Dispatchers.IO) {
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        var mat = bitmapToMat(bitmap)

                        if (keepColor && mat.channels() == 1) {
                            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_GRAY2BGR)
                        }

                        val processedImage = preprocessImage(mat, keepColor)
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

            "convertMatToBase64" -> {
                val imageBytes = call.argument<ByteArray>("image")
                val keepColor = call.argument<Boolean>("keepColor") ?: true

                if (imageBytes != null) {
                    GlobalScope.launch(Dispatchers.IO) {
                        val base64String = convertMatToBase64(imageBytes, keepColor)
                        withContext(Dispatchers.Main) {
                            result.success(base64String)
                        }
                    }
                } else {
                    result.error("ERROR", "Invalid image input", null)
                }
            }

            else -> result.notImplemented()
        }
    }

    /** ✅ Convert Mat to Base64 with color preservation */
    private fun convertMatToBase64(imageBytes: ByteArray, keepColor: Boolean): String {
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        var mat = bitmapToMat(bitmap)

        if (keepColor && mat.channels() == 1) {
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_GRAY2BGR)
        }

        val processedBitmap = matToBitmap(mat)

        val outputStream = ByteArrayOutputStream()
        processedBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    /** ✅ Ensure preprocessing retains color if needed */
    private fun preprocessImage(inputMat: Mat, keepColor: Boolean): Mat {
        var processedMat = inputMat.clone()

        if (keepColor && processedMat.channels() == 1) {
            Imgproc.cvtColor(processedMat, processedMat, Imgproc.COLOR_GRAY2BGR)
        }

        processedMat = applyGammaCorrection(processedMat, 1.2)
        processedMat = reduceNoiseWithBilateral(processedMat)
        processedMat = enhanceSharpenUnsharpMask(processedMat)

        if (keepColor && processedMat.channels() == 1) {
            Imgproc.cvtColor(processedMat, processedMat, Imgproc.COLOR_GRAY2BGR)
        }

        return processedMat
    }

    /** ✅ Convert Bitmap to Mat */
    private fun bitmapToMat(bitmap: Bitmap): Mat {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)
        return mat
    }

    /** ✅ Convert Mat to Bitmap */
    private fun matToBitmap(mat: Mat): Bitmap {
        val bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, bitmap)
        return bitmap
    }

    /** ✅ Gamma Correction */
    private fun applyGammaCorrection(image: Mat, gamma: Double): Mat {
        val invGamma = 1.0 / gamma
        val lut = Mat(1, 256, CvType.CV_8U)

        for (i in 0..255) {
            lut.put(0, i, ((i / 255.0).pow(invGamma) * 255).toInt().toDouble())
        }

        val correctedImage = Mat()
        Core.LUT(image, lut, correctedImage)

        return correctedImage
    }

    /** ✅ Noise Reduction */
    private fun reduceNoiseWithBilateral(mat: Mat, d: Int = 9, sigmaColor: Double = 75.0, sigmaSpace: Double = 75.0): Mat {
        val output = Mat()
        Imgproc.bilateralFilter(mat, output, d, sigmaColor, sigmaSpace)
        return output
    }

    /** ✅ Image Sharpening */
    private fun enhanceSharpenUnsharpMask(mat: Mat, strength: Double = 1.5, blurKernel: Size = Size(5.0, 5.0)): Mat {
        val blurred = Mat()
        Imgproc.GaussianBlur(mat, blurred, blurKernel, 0.0)
        val sharpened = Mat()
        Core.addWeighted(mat, 1.0 + strength, blurred, -strength, 0.0, sharpened)
        return sharpened
    }
}
