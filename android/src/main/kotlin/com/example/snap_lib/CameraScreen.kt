package com.example.snap_lib


import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun CameraScreen() {
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current

    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(modifier = Modifier.fillMaxSize(), lifecycleOwner = lifecycleOwner)

        CameraOverlay(
            modifier = Modifier.fillMaxSize(),
            guideText = "ให้ใบหน้าอยู่ในกรอบที่กำหนด",
            instructionText = "ไม่มีปิดตา จมูก ปาก และคาง",
            guideTextStyle = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.Blue),
            instructionTextStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.Red),
            guideTextAlignment = Alignment.TopCenter,
            instructionTextAlignment = Alignment.BottomCenter
        )


    }
}
