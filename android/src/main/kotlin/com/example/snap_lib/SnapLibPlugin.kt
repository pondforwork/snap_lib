package com.example.snap_lib

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

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
                handleSnap(call, result, "front")
            }
            "startBackSnap" -> {
                handleSnap(call, result, "back")
            }
            "startCameraOverlay" -> {
                startCameraOverlay(call, result)
            }
            else -> result.notImplemented()
        }
    }

    /** Handles starting the Snap Activity */
    private fun handleSnap(call: MethodCall, result: MethodChannel.Result, snapMode: String) {
        val titleMessage = call.argument<String>("titleMessage") ?: "Scanning ${snapMode.capitalize()} Card"
        val initialMessage = call.argument<String>("initialMessage") ?: "Please position your card"
        val foundMessage = call.argument<String>("foundMessage") ?: "Card detected"
        val notFoundMessage = call.argument<String>("notFoundMessage") ?: "No card found"

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
        result.success("Snap Started for $snapMode card")
    }

    /** Handles starting the Camera Overlay */
    private fun startCameraOverlay(call: MethodCall, result: MethodChannel.Result) {
        val intent = Intent(context, ScanFaceActivity::class.java).apply {
            putExtra("guideText", call.argument<String>("guideText") ?: "ให้ใบหน้าอยู่ในกรอบที่กำหนด")
            putExtra("instructionText", call.argument<String>("instructionText") ?: "ไม่มีปิดตา จมูก ปาก และคาง")
            putExtra("successText", call.argument<String>("successText") ?: "ถือค้างไว้")

            // ✅ Convert Long to Int for colors
            putExtra("borderColorSuccess", (call.argument<Number>("borderColorSuccess") ?: 0xFF00FF00).toInt())
            putExtra("borderColorDefault", (call.argument<Number>("borderColorDefault") ?: 0xFFFF0000).toInt())
            putExtra("textColorDefault", (call.argument<Number>("textColorDefault") ?: 0xFFFFFFFF).toInt())
            putExtra("textColorSuccess", (call.argument<Number>("textColorSuccess") ?: 0xFF00FF00).toInt())

            // ✅ Convert Double to Float for font sizes
            putExtra("guideFontSize", (call.argument<Number>("guideFontSize") ?: 24.0).toFloat())
            putExtra("instructionFontSize", (call.argument<Number>("instructionFontSize") ?: 20.0).toFloat())

            // ✅ Convert Long to Int for text colors
            putExtra("guideTextColor", (call.argument<Number>("guideTextColor") ?: 0xFFFFFF00).toInt())
            putExtra("instructionTextColor", (call.argument<Number>("instructionTextColor") ?: 0x00FFFF).toInt())

            // ✅ Handle Enum String for FaceSnapMode
            putExtra("faceSnapMode", call.argument<String>("faceSnapMode") ?: "normal")
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        result.success("Camera Overlay Started")
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        imageProcessor.onDetachedFromEngine(binding)
    }
}
