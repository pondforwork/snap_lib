package com.example.snap_lib

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.*
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun CameraOverlay(
    modifier: Modifier = Modifier,
    guideText: String = "ให้ใบหน้าอยู่ในกรอบที่กำหนด",
    instructionText: String = "ไม่มีปิดตา จมูก ปาก และคาง",
    successText: String = "ถือค้างไว้",
    borderColorSuccess: Color = Color.Green,
    borderColorDefault: Color = Color.Red,
    textColorDefault: Color = Color.White,
    textColorSuccess: Color = Color.Green,
    borderWidth: Float = 8f
) {
    // Dynamic border color and text color based on guideText
    val borderColor by animateColorAsState(
        targetValue = if (guideText == successText) borderColorSuccess else borderColorDefault,
        animationSpec = tween(durationMillis = 500)
    )
    val textColor by animateColorAsState(
        targetValue = if (guideText == successText) textColorSuccess else textColorDefault,
        animationSpec = tween(durationMillis = 500)
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Transparent overlay with oval cutout
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val ovalWidth = canvasWidth * 0.7f
            val ovalHeight = canvasHeight * 0.5f
            val ovalLeft = (canvasWidth - ovalWidth) / 2
            val ovalTop = (canvasHeight - ovalHeight) / 2

            // Background outside the oval
            drawRect(
                color = Color.Black.copy(alpha = 0.6f),
                size = size
            )

            // Clear the oval area
            drawOval(
                color = Color.Transparent,
                topLeft = Offset(ovalLeft, ovalTop),
                size = Size(ovalWidth, ovalHeight),
                blendMode = BlendMode.Clear
            )

            // Oval border
            drawOval(
                color = borderColor,
                topLeft = Offset(ovalLeft, ovalTop),
                size = Size(ovalWidth, ovalHeight),
                style = Stroke(width = borderWidth.dp.toPx())
            )
        }

        // Guide Text
        BasicText(
            text = guideText,
            style = TextStyle(
                color = textColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        )

        // Instruction Text
        BasicText(
            text = instructionText,
            style = TextStyle(
                color = Color.White,
                fontSize = 16.sp
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}
