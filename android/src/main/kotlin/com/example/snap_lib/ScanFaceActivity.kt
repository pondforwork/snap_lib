package com.example.snap_lib

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

class ScanFaceActivity : ComponentActivity(), FlutterPlugin, MethodChannel.MethodCallHandler {
    private lateinit var channel: MethodChannel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val lifecycleOwner = LocalLifecycleOwner.current
            var guideText by remember { mutableStateOf("ให้ใบหน้าอยู่ในกรอบที่กำหนด") }
            var instructionText by remember { mutableStateOf("ไม่มีปิดตา จมูก ปาก และคาง") }
            var guideTextStyle by remember { mutableStateOf(TextStyle(fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.Blue)) }
            var instructionTextStyle by remember { mutableStateOf(TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.Red)) }

            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CameraPreview(modifier = Modifier.fillMaxSize(), lifecycleOwner = lifecycleOwner)

                        CameraOverlay(
                            modifier = Modifier.fillMaxSize(),
                            guideText = guideText,
                            instructionText = instructionText,
                            guideTextStyle = guideTextStyle,
                            instructionTextStyle = instructionTextStyle,
                            guideTextAlignment = Alignment.TopCenter,
                            instructionTextAlignment = Alignment.BottomCenter
                        )
                    }
                }
            }
        }
    }

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(binding.binaryMessenger, "snap_plugin")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "openScanFace" -> {
                openScanFace()
                result.success(null)
            }
            "updateGuideText" -> {
                val newText = call.argument<String>("text") ?: "ให้ใบหน้าอยู่ในกรอบที่กำหนด"
                result.success(null)
            }
            "updateInstructionText" -> {
                val newText = call.argument<String>("text") ?: "ไม่มีปิดตา จมูก ปาก และคาง"
                result.success(null)
            }
            else -> result.notImplemented()
        }
    }

    private fun openScanFace() {
        val intent = Intent(this, ScanFaceActivity::class.java)
        startActivity(intent)
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }
}
