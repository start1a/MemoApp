package com.start3a.memoji

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.IOException


// 이미지
class ImageTransform {

    companion object {
        fun getOrientationOfImage(imagePath: String): Int {
            val exif = try {
                ExifInterface(File(imagePath).absolutePath)
            } catch (e: IOException) {
                e.printStackTrace()
                return -1
            }
            val orientation =
                exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1)
            if (orientation != -1) {
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> return 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> return 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> return 270
                }
            }
            return 0
        }

        @Throws(Exception::class)
        fun getRotatedBitmap(bitmap: Bitmap?, degrees: Int): Bitmap? {
            if (bitmap == null) return null
            if (degrees == 0) return bitmap
            val m = Matrix()
            m.setRotate(
                degrees.toFloat(),
                bitmap.width.toFloat() / 2,
                bitmap.height.toFloat() / 2
            )
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
        }
    }
}