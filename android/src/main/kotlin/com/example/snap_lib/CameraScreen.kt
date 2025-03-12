package com.example.snap_lib


import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CameraScreen() {
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera preview now receives `lifecycleOwner`
        CameraPreview(modifier = Modifier.fillMaxSize(), lifecycleOwner = lifecycleOwner)

        // Overlay UI
        CameraOverlay(
            modifier = Modifier.fillMaxSize(),
            guideText = "ให้ใบหน้าอยู่ในกรอบที่กำหนด"
        )
    }
}
