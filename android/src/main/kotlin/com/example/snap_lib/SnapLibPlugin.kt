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
            "startFaceSnap" -> {
                startCameraOverlay(call, result)
            }
            else -> result.notImplemented()
        }
    }


    private fun handleSnap(call: MethodCall, result: MethodChannel.Result, snapMode: String) {
        val titleMessage = call.argument<String>("titleMessage") ?: "Scanning ${snapMode.capitalize()} Card"
        val initialMessage = call.argument<String>("initialMessage") ?: "Please position your card"
        val foundMessage = call.argument<String>("foundMessage") ?: "Card detected"
        val notFoundMessage = call.argument<String>("notFoundMessage") ?: "No card found"

        val titleFontSize = call.argument<Number>("titleFontSize")?.toInt() ?: 20
        val guideMessageFontSize = call.argument<Number>("guideMessageFontSize")?.toInt() ?: 20

        val isDetectNoise = call.argument<Boolean>("isDetectNoise") ?: true
        val isDetectBrightness = call.argument<Boolean>("isDetectBrightness") ?: true
        val isDetectGlare = call.argument<Boolean>("isDetectGlare") ?: true

        val maxNoiseValue = call.argument<Double>("maxNoiseValue") ?: 3.0
        val maxBrightnessValue = call.argument<Double>("maxBrightnessValue") ?: 200.0
        val minBrightnessValue = call.argument<Double>("minBrightnessValue") ?: 80.0
        val maxGlarePercent = call.argument<Double>("maxGlarePercent") ?: 1.0

        val warningMessage = call.argument<String>("warningMessage") ?: "กรุณาปรับแสงให้เหมาะสม"
        val warningNoise = call.argument<String>("warningNoise") ?: "🔹 ลด Noise ในภาพ"
        val warningBrightnessOver = call.argument<String>("warningBrightnessOver") ?: "🔹 ลดความสว่าง"
        val warningBrightnessLower = call.argument<String>("warningBrightnessLower") ?: "🔹 เพิ่มความสว่าง"
        val warningGlare = call.argument<String>("warningGlare") ?: "🔹 ลดแสงสะท้อน"

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

            putExtra("titleFontSize", titleFontSize)
            putExtra("guideMessageFontSize", guideMessageFontSize)

            putExtra("isDetectNoise", isDetectNoise)
            putExtra("isDetectBrightness", isDetectBrightness)
            putExtra("isDetectGlare", isDetectGlare)
            putExtra("maxNoiseValue", maxNoiseValue)
            putExtra("maxBrightnessValue", maxBrightnessValue)
            putExtra("minBrightnessValue", minBrightnessValue)
            putExtra("maxGlarePercent", maxGlarePercent)

            putExtra("warningMessage", warningMessage)
            putExtra("warningNoise", warningNoise)
            putExtra("warningBrightnessOver", warningBrightnessOver)
            putExtra("warningBrightnessLower", warningBrightnessLower)
            putExtra("warningGlare", warningGlare)

            putExtra("dialogBackgroundColor", (call.argument<Number>("dialogBackgroundColor") ?: 0xFFFFFFFF).toInt())
            putExtra("dialogTitleColor", (call.argument<Number>("dialogTitleColor") ?: 0xFF2D3892).toInt())
            putExtra("dialogButtonConfirmColor", (call.argument<Number>("dialogButtonConfirmColor") ?: 0xFF2D3892).toInt())
            putExtra("dialogButtonRetakeColor", (call.argument<Number>("dialogButtonRetakeColor") ?: 0xFFFFFFFF).toInt())
            putExtra("dialogButtonTextColor", (call.argument<Number>("dialogButtonTextColor") ?: 0xFF000000).toInt())
            putExtra("dialogAlignment", call.argument<String>("dialogAlignment") ?: "center")
            putExtra("dialogTitle", call.argument<String>("dialogTitle") ?: "ยืนยันข้อมูล")
            putExtra("dialogTitleFontSize", (call.argument<Number>("dialogTitleFontSize") ?: 22).toInt())
            putExtra("dialogTitleAlignment", call.argument<String>("dialogTitleAlignment") ?: "center")
            putExtra("dialogExtraMessage", call.argument<String>("dialogExtraMessage") ?: "ตรวจสอบให้แน่ใจว่ารูปภาพสามารถอ่านได้ชัดเจน")
            putExtra("dialogExtraMessageColor", (call.argument<Number>("dialogExtraMessageColor") ?: 0xFF000000).toInt())
            putExtra("dialogExtraMessageFontSize", (call.argument<Number>("dialogExtraMessageFontSize") ?: 14).toInt())
            putExtra("dialogExtraMessageAlignment", call.argument<String>("dialogExtraMessageAlignment") ?: "center")
            putExtra("dialogBorderRadius", (call.argument<Number>("dialogBorderRadius") ?: 16).toInt())
            putExtra("dialogButtonHeight", (call.argument<Number>("dialogButtonHeight") ?: 48).toInt())

            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)
        result.success("Snap Started for $snapMode card")
    }
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

            // ✅ เพิ่มการส่งค่า borderWidth
            putExtra("borderWidth", (call.argument<Number>("borderWidth") ?: 5).toInt())

            putExtra("dialogBackgroundColor", (call.argument<Number>("dialogBackgroundColor") ?: 0xFFFFFFFF).toInt())
            putExtra("dialogTitleColor", (call.argument<Number>("dialogTitleColor") ?: 0xFF2D3892).toInt())
            putExtra("dialogButtonConfirmColor", (call.argument<Number>("dialogButtonConfirmColor") ?: 0xFF2D3892).toInt())
            putExtra("dialogButtonRetakeColor", (call.argument<Number>("dialogButtonRetakeColor") ?: 0xFFFFFFFF).toInt())
            putExtra("dialogButtonTextColor", (call.argument<Number>("dialogButtonTextColor") ?: 0xFF000000).toInt())
            putExtra("dialogAlignment", call.argument<String>("dialogAlignment") ?: "center")
            putExtra("dialogTitle", call.argument<String>("dialogTitle") ?: "ยืนยันข้อมูล")
            putExtra("dialogTitleFontSize", (call.argument<Number>("dialogTitleFontSize") ?: 22).toInt())
            putExtra("dialogTitleAlignment", call.argument<String>("dialogTitleAlignment") ?: "center")
            putExtra("dialogExtraMessage", call.argument<String>("dialogExtraMessage") ?: "ตรวจสอบให้แน่ใจว่ารูปภาพสามารถอ่านได้ชัดเจน")
            putExtra("dialogExtraMessageColor", (call.argument<Number>("dialogExtraMessageColor") ?: 0xFF000000).toInt())
            putExtra("dialogExtraMessageFontSize", (call.argument<Number>("dialogExtraMessageFontSize") ?: 14).toInt())
            putExtra("dialogExtraMessageAlignment", call.argument<String>("dialogExtraMessageAlignment") ?: "center")
            putExtra("dialogBorderRadius", (call.argument<Number>("dialogBorderRadius") ?: 16).toInt())
            putExtra("dialogButtonHeight", (call.argument<Number>("dialogButtonHeight") ?: 48).toInt())
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
