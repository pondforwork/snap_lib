package com.example.snap_lib

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import org.opencv.android.OpenCVLoader
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.android.Utils
import java.io.ByteArrayOutputStream

/** SnapLibPlugin */
class SnapLibPlugin : FlutterPlugin, MethodCallHandler {
  private lateinit var channel: MethodChannel

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "image_processor_plugin")
    channel.setMethodCallHandler(this)

    // ✅ Initialize OpenCV
    if (!OpenCVLoader.initDebug()) {
      println("OpenCV initialization failed.")
    } else {
      println("OpenCV initialized successfully.")
    }
  }

  override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
    when (call.method) {
      "processImage" -> {
        val imageBytes = call.argument<ByteArray>("image")
        if (imageBytes != null) {
          val processedImage = processImage(imageBytes)
          result.success(processedImage)
        } else {
          result.error("ERROR", "Invalid image input", null)
        }
      }

      "convertMatToBase64" -> {
        val imageBytes = call.argument<ByteArray>("image")
        if (imageBytes != null) {
          val base64String = convertMatToBase64(imageBytes)
          result.success(base64String)
        } else {
          result.error("ERROR", "Invalid image input", null)
        }
      }

      else -> result.notImplemented()
    }
  }

  private fun processImage(imageBytes: ByteArray): ByteArray {
    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    val mat = bitmapToMat(bitmap)

    // Apply some OpenCV processing
    val processedMat = Mat()
    Imgproc.cvtColor(mat, processedMat, Imgproc.COLOR_BGR2GRAY)

    return matToByteArray(processedMat)
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

  private fun matToByteArray(mat: Mat): ByteArray {
    val bitmap = matToBitmap(mat)
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    return outputStream.toByteArray()
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }
}
