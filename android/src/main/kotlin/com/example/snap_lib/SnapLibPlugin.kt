package com.example.snap_lib

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.core.snap
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
            val titleMessage = call.argument<String>("titleMessage") ?: "Scanning Front Card"
            val initialMessage = call.argument<String>("initialMessage") ?: "Please position your card"
            val foundMessage = call.argument<String>("foundMessage") ?: "Card detected"
            val notFoundMessage = call.argument<String>("notFoundMessage") ?: "No card found"
            val snapMode = call.argument<String>("snapMode") ?: "front"
            startSnap(titleMessage, initialMessage, foundMessage, notFoundMessage, snapMode)
            result.success("Snap Started")
        }

      "openScanFace" -> {
        openScanFace()
        result.success("ScanFaceActivity started")
      }


      else -> result.notImplemented()
    }
  }

  private fun startFrontSnap(titleMessage: String , initialMessage: String , foundMessage: String, notFoundMessage: String, snapMode : String) {

    if(snapMode == "front"){
      val intent = Intent(context, ScanFrontCardActivity::class.java)
      intent.putExtra("titleMessage", titleMessage)
      intent.putExtra("initialMessage", initialMessage)
      intent.putExtra("foundMessage", foundMessage)
      intent.putExtra("notFoundMessage", notFoundMessage)
      intent.putExtra("snapMode", snapMode)
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(intent)
    } else {
      val intent = Intent(context, ScanBackCardActivity::class.java)
      intent.putExtra("titleMessage", titleMessage)
      intent.putExtra("initialMessage", initialMessage)
      intent.putExtra("foundMessage", foundMessage)
      intent.putExtra("notFoundMessage", notFoundMessage)
      intent.putExtra("snapMode", snapMode)
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(intent)
    }
  }

    private fun startSnap(titleMessage: String, initialMessage: String, foundMessage: String, notFoundMessage: String, snapMode: String) {
        val intent = if (snapMode == "front") {
            Intent(context, ScanFrontCardActivity::class.java)
        } else {
            Intent(context, ScanBackCardActivity::class.java)
        }

        intent.apply {
            putExtra("titleMessage", titleMessage)
            putExtra("initialMessage", initialMessage)
            putExtra("foundMessage", foundMessage)
            putExtra("notFoundMessage", notFoundMessage)
            putExtra("snapMode", snapMode)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun openScanFace() {
        val intent = Intent(context, ScanFaceActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        imageProcessor.onDetachedFromEngine(binding)
    }



}
