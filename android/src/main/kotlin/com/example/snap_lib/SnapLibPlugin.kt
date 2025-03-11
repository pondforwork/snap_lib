package com.example.snap_lib

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

/** SnapLibPlugin */
class SnapLibPlugin : FlutterPlugin, MethodChannel.MethodCallHandler {
  private lateinit var channel: MethodChannel
  private lateinit var context: Context
  private lateinit var imageProcessor: ImageProcessorPlugin  // ✅ Reference to ImageProcessorPlugin

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "image_processor_plugin")
    channel.setMethodCallHandler(this)
    context = flutterPluginBinding.applicationContext

    // Initialize ImageProcessorPlugin
    imageProcessor = ImageProcessorPlugin()
    imageProcessor.onAttachedToEngine(flutterPluginBinding)
  }

  override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
    when (call.method) {
      "processImage" -> {
        imageProcessor.onMethodCall(call, result)
      }

      "startFrontSnap" -> {
        startFrontSnap(result)
      }

      else -> result.notImplemented()
    }
  }

  private fun startFrontSnap(result: MethodChannel.Result) {
    try {
      val intent = Intent(context, NewActivity::class.java)
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(intent)
      result.success("NewActivity started successfully")
    } catch (e: Exception) {
      result.error("START_ERROR", "Failed to start NewActivity: ${e.localizedMessage}", null)
    }
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
    imageProcessor.onDetachedFromEngine(binding)  // ✅ Detach ImageProcessorPlugin
  }
}
