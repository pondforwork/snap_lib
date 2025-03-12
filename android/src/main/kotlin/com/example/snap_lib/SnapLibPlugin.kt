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
        // Parameter ที่ Parse เข้ามา
        val parameter = call.argument<String>("titleMessage")
        val initialMessage = call.argument<String>("initialMessage")
        val foundMessage = call.argument<String>("foundMessage")
        val notFoundMessage = call.argument<String>("notFoundMessage")
        val snapMode = call.argument<String>("snapMode")

        if (parameter != null && initialMessage !=null && foundMessage !=null && notFoundMessage !=null && snapMode!=null) {
          startFrontSnap(parameter,initialMessage,foundMessage,notFoundMessage,snapMode)
          result.success("Parameter received")
        } else {
          result.error("INVALID_ARGUMENT", "Parameter is null", null)
        }
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


  private fun openScanFace() {
    val intent = Intent(context, ScanFaceActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
  }



  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
    imageProcessor.onDetachedFromEngine(binding)  // ✅ Detach ImageProcessorPlugin
  }
}
