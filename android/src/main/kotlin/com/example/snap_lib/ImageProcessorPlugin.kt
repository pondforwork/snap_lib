package com.example.image_processor_plugin

import android.graphics.Bitmap
import android.graphics.Matrix
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.android.Utils

object ImageProcessingUtils {

    fun processImage(bitmap: Bitmap): Bitmap {
        val rotatedBitmap = rotateBitmap(bitmap, 90f)
        return cropToCreditCardAspectRatio(rotatedBitmap)
    }

    fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(rotationDegrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun cropToCreditCardAspectRatio(
        bitmap: Bitmap,
        aspectRatio: Float = 3.37f / 2.125f,
        cropWidthRatio: Float = 0.7f // ค่าเริ่มต้น 70% ของความกว้างของภาพ
    ): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        // คำนวณความกว้างของภาพที่ต้องการครอบตัด
        val rectWidth = width * cropWidthRatio
        val rectHeight = rectWidth / aspectRatio

        // คำนวณตำแหน่งเริ่มต้นสำหรับการครอบตัด
        val rectLeft = (width - rectWidth) / 2
        val rectTop = (height - rectHeight) / 2

        // ตรวจสอบให้แน่ใจว่าไม่เกินขอบของภาพ
        val finalWidth = rectWidth.toInt().coerceAtMost(width)
        val finalHeight = rectHeight.toInt().coerceAtMost(height)
        val finalLeft = rectLeft.toInt().coerceAtLeast(0)
        val finalTop = rectTop.toInt().coerceAtLeast(0)

        return Bitmap.createBitmap(bitmap, finalLeft, finalTop, finalWidth, finalHeight)
    }


    fun bitmapToMat(bitmap: Bitmap): Mat {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)
        return mat
    }

    fun calculateBrightness(mat: Mat): Double {
        val gray = Mat()
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY)
        val mean = Core.mean(gray)
        gray.release()
        return mean.`val`[0]
    }

    fun calculateGlare(mat: Mat,glareAreaRule:Double = 500.0): Double {
        val gray = Mat()
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY)
        val binary = Mat()
        Imgproc.threshold(gray, binary, 230.0, 255.0, Imgproc.THRESH_BINARY)
        val contours = ArrayList<MatOfPoint>()
        Imgproc.findContours(binary, contours, Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)
        var glareArea = 0.0
        for (contour in contours) {
            val area = Imgproc.contourArea(contour)
            if (area > glareAreaRule) {
                glareArea += area
            }
        }
        val totalArea = mat.width() * mat.height()
        return (glareArea / totalArea) * 100
    }

    fun calculateSNR(mat: Mat): Double {
        val grayMat = Mat()
        Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_BGR2GRAY)
        val meanStdDev = MatOfDouble()
        val stdDev = MatOfDouble()
        Core.meanStdDev(grayMat, meanStdDev, stdDev)
        grayMat.release()
        val mean = meanStdDev.toArray().firstOrNull() ?: 0.0
        val std = stdDev.toArray().firstOrNull() ?: 1.0
        return mean / std
    }
}
