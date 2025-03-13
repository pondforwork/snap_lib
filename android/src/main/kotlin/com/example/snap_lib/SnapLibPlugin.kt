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

//    private fun handleSnap(call: MethodCall, result: MethodChannel.Result, snapMode: String) {
//        val titleMessage = call.argument<String>("titleMessage") ?: "Scanning ${snapMode.capitalize()} Card"
//        val initialMessage = call.argument<String>("initialMessage") ?: "Please position your card"
//        val foundMessage = call.argument<String>("foundMessage") ?: "Card detected"
//        val notFoundMessage = call.argument<String>("notFoundMessage") ?: "No card found"
//
//        val isDetectNoise = call.argument<Boolean>("isDetectNoise") ?: true
//        val isDetectBrightness = call.argument<Boolean>("isDetectBrightness") ?: true
//        val isDetectGlare = call.argument<Boolean>("isDetectGlare") ?: true
//
//        val maxNoiseValue = call.argument<Double>("maxNoiseValue") ?: 3.0
//        val maxBrightnessValue = call.argument<Double>("maxBrightnessValue") ?: 200.0
//        val minBrightnessValue = call.argument<Double>("minBrightnessValue") ?: 80.0
//        val maxGlarePercent = call.argument<Double>("maxGlarePercent") ?: 1.0
//
//        val warningMessage = call.argument<String>("warningMessage") ?: "กรุณาปรับแสงให้เหมาะสม"
//        val warningNoise = call.argument<String>("warningNoise") ?: "🔹 ลด Noise ในภาพ"
//        val warningBrightnessOver = call.argument<String>("warningBrightnessOver") ?: "🔹 ลดความสว่าง"
//        val warningBrightnessLower = call.argument<String>("warningBrightnessLower") ?: "🔹 เพิ่มความสว่าง"
//        val warningGlare = call.argument<String>("warningGlare") ?: "🔹 ลดแสงสะท้อน"
//
//        val intent = if (snapMode == "front") {
//            Intent(context, ScanFrontCardActivity::class.java)
//        } else {
//            Intent(context, ScanBackCardActivity::class.java)
//        }
//
//        intent.apply {
//            putExtra("titleMessage", titleMessage)
//            putExtra("initialMessage", initialMessage)
//            putExtra("foundMessage", foundMessage)
//            putExtra("notFoundMessage", notFoundMessage)
//            putExtra("snapMode", snapMode)
//
//            // ✅ ส่งค่าการตั้งค่าตรวจจับ
//            putExtra("isDetectNoise", isDetectNoise)
//            putExtra("isDetectBrightness", isDetectBrightness)
//            putExtra("isDetectGlare", isDetectGlare)
//
//            putExtra("maxNoiseValue", maxNoiseValue)
//            putExtra("maxBrightnessValue", maxBrightnessValue)
//            putExtra("minBrightnessValue", minBrightnessValue)
//            putExtra("maxGlarePercent", maxGlarePercent)
//
//            // ✅ ส่งค่าข้อความแจ้งเตือนที่กำหนดเอง
//            putExtra("warningMessage", warningMessage)
//            putExtra("warningNoise", warningNoise)
//            putExtra("warningBrightnessOver", warningBrightnessOver)
//            putExtra("warningBrightnessLower", warningBrightnessLower)
//            putExtra("warningGlare", warningGlare)
////dialog
//            // ✅ Pass user-defined custom dialog properties
//            putExtra("dialogBackgroundColor", call.argument<Int>("dialogBackgroundColor") ?: Color.WHITE)
//            putExtra("dialogTitleColor", call.argument<Int>("dialogTitleColor") ?: Color(0xFF2D3892))
//            putExtra("dialogSubtitleColor", call.argument<Int>("dialogSubtitleColor") ?: Color.GRAY)
//            putExtra("dialogButtonConfirmColor", call.argument<Int>("dialogButtonConfirmColor") ?: Color(0xFF2D3892))
//            putExtra("dialogButtonRetakeColor", call.argument<Int>("dialogButtonRetakeColor") ?: Color.WHITE)
//            putExtra("dialogButtonTextColor", call.argument<Int>("dialogButtonTextColor") ?: Color.WHITE)
//            putExtra("dialogAlignment", call.argument<String>("dialogAlignment") ?: "center")
//
//            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        }
//
//        context.startActivity(intent)
//        result.success("Snap Started for $snapMode card")
//    }
private fun handleSnap(call: MethodCall, result: MethodChannel.Result, snapMode: String) {
    val titleMessage = call.argument<String>("titleMessage") ?: "Scanning ${snapMode.capitalize()} Card"
    val initialMessage = call.argument<String>("initialMessage") ?: "Please position your card"
    val foundMessage = call.argument<String>("foundMessage") ?: "Card detected"
    val notFoundMessage = call.argument<String>("notFoundMessage") ?: "No card found"

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

    // ✅ Handle Dialog Settings (Fixed)
    val dialogSettings = mapOf(
        "dialogBackgroundColor" to (call.argument<Number>("dialogBackgroundColor")?.toInt() ?: 0xFFFFFFFF.toInt()),
        "dialogTitleColor" to (call.argument<Number>("dialogTitleColor")?.toInt() ?: 0xFF2D3892.toInt()),
        "dialogSubtitleColor" to (call.argument<Number>("dialogSubtitleColor")?.toInt() ?: 0xFF888888.toInt()),
        "dialogButtonConfirmColor" to (call.argument<Number>("dialogButtonConfirmColor")?.toInt() ?: 0xFF2D3892.toInt()),
        "dialogButtonRetakeColor" to (call.argument<Number>("dialogButtonRetakeColor")?.toInt() ?: 0xFFFFFFFF.toInt()),
        "dialogButtonTextColor" to (call.argument<Number>("dialogButtonTextColor")?.toInt() ?: 0xFF000000.toInt()),
        "dialogAlignment" to (call.argument<String>("dialogAlignment") ?: "center"),
        "dialogTitle" to (call.argument<String>("dialogTitle") ?: "ยืนยันข้อมูล"),
        "dialogTitleFontSize" to (call.argument<Number>("dialogTitleFontSize")?.toInt() ?: 22),
        "dialogTitleAlignment" to (call.argument<String>("dialogTitleAlignment") ?: "center"),
        "dialogSubtitle" to (call.argument<String>("dialogSubtitle") ?: "กรุณาตรวจสอบความชัดเจนของภาพบัตร"),
        "dialogSubtitleFontSize" to (call.argument<Number>("dialogSubtitleFontSize")?.toInt() ?: 14),
        "dialogSubtitleAlignment" to (call.argument<String>("dialogSubtitleAlignment") ?: "center"),
        "dialogExtraMessage" to (call.argument<String>("dialogExtraMessage") ?: "ตรวจสอบให้แน่ใจว่ารูปภาพสามารถอ่านได้ชัดเจน"),
        "dialogExtraMessageColor" to (call.argument<Number>("dialogExtraMessageColor")?.toInt() ?: 0xFF000000.toInt()),
        "dialogExtraMessageFontSize" to (call.argument<Number>("dialogExtraMessageFontSize")?.toInt() ?: 14),
        "dialogExtraMessageAlignment" to (call.argument<String>("dialogExtraMessageAlignment") ?: "center"),
        "dialogBorderRadius" to (call.argument<Number>("dialogBorderRadius")?.toInt() ?: 16),
        "dialogButtonHeight" to (call.argument<Number>("dialogButtonHeight")?.toInt() ?: 48)
    )

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

        // ✅ Add detection settings
        putExtra("isDetectNoise", isDetectNoise)
        putExtra("isDetectBrightness", isDetectBrightness)
        putExtra("isDetectGlare", isDetectGlare)
        putExtra("maxNoiseValue", maxNoiseValue)
        putExtra("maxBrightnessValue", maxBrightnessValue)
        putExtra("minBrightnessValue", minBrightnessValue)
        putExtra("maxGlarePercent", maxGlarePercent)

        // ✅ Add warning messages
        putExtra("warningMessage", warningMessage)
        putExtra("warningNoise", warningNoise)
        putExtra("warningBrightnessOver", warningBrightnessOver)
        putExtra("warningBrightnessLower", warningBrightnessLower)
        putExtra("warningGlare", warningGlare)

        // ✅ Pass dialog settings dynamically
        dialogSettings.forEach { (key, value) ->
            when (value) {
                is String -> putExtra(key, value)
                is Int -> putExtra(key, value)
            }
        }

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
