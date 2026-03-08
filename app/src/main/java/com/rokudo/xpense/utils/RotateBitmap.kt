package com.rokudo.xpense.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import java.io.IOException

object RotateBitmap {
    private const val TAG = "RotateBitmap"

    private fun rotateImage(img: Bitmap, degree: Int): Bitmap {
        val matrix = Matrix().apply { postRotate(degree.toFloat()) }
        val rotated = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
        img.recycle()
        return rotated
    }

    @JvmStatic
    @Throws(IOException::class)
    fun HandleSamplingAndRotationBitmap(context: Context, selectedImage: Uri): Bitmap? {
        val maxHeight = 1024
        val maxWidth = 1024

        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(selectedImage)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }

        options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
        options.inJustDecodeBounds = false

        var img: Bitmap? = context.contentResolver.openInputStream(selectedImage)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }
        img = img?.let { rotateImageIfRequired(it, selectedImage, context) }
        return img
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
            val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
            inSampleSize = minOf(heightRatio, widthRatio)
            val totalPixels = (width * height).toFloat()
            val totalReqPixelsCap = (reqWidth * reqHeight * 2).toFloat()
            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++
            }
        }
        return inSampleSize
    }

    private fun rotateImageIfRequired(img: Bitmap, selectedImage: Uri, context: Context): Bitmap {
        return try {
            val input = context.contentResolver.openInputStream(selectedImage) ?: return img
            val ei = ExifInterface(input)
            when (ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270)
                else -> img
            }
        } catch (e: NullPointerException) {
            Log.e(TAG, "rotateImageIfRequired: Could not read file. ${e.message}")
            img
        }
    }
}

