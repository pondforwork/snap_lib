package com.example.image_processor_plugin

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream

object FileUtils {
    fun saveBitmap(context: Context, bitmap: Bitmap, fileName: String): String {
        val file = File(context.filesDir, "$fileName.jpg")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
        return file.absolutePath
    }
}
