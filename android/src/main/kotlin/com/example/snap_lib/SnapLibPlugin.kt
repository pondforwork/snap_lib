package com.example.snap_lib

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.io.ByteArrayOutputStream

/** SnapLibPlugin */
class SnapLibPlugin : FlutterPlugin, MethodChannel.MethodCallHandler {
  private lateinit var channel: MethodChannel
  private lateinit var context: Context
  private lateinit var imageProcessor: ImageProcessorPlugin

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "snap_plugin")
    channel.setMethodCallHandler(this)
    context = flutterPluginBinding.applicationContext

    imageProcessor = ImageProcessorPlugin()
    imageProcessor.onAttachedToEngine(flutterPluginBinding)
  }

  override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
    when (call.method) {
      "processImage" -> {
        imageProcessor.onMethodCall(call, result)
      }
      "startFrontSnap" -> {
        val parameter = call.argument<String>("titleMessage")
        if (parameter != null) {
          startFrontSnap(parameter)
          result.success("Parameter received")
        } else {
          result.error("INVALID_ARGUMENT", "Parameter is null", null)
        }
      }

      else -> result.notImplemented()
    }
  }
  private fun startFrontSnap(parameter1: String) {
    val intent = Intent(context, NewActivity::class.java)
    intent.putExtra("titleMessage", parameter1)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
  }


  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
    imageProcessor.onDetachedFromEngine(binding)  // ✅ Detach ImageProcessorPlugin
  }
}
