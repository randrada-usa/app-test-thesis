package com.example.fruitgrade.ml

import android.graphics.Bitmap
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ImagePreprocessor {
    companion object {
        const val INPUT_SIZE = 224
        const val BATCH_SIZE = 1
        const val CHANNELS = 3
        const val FLOAT_SIZE = 4
    }

    fun preprocess(bitmap: Bitmap): ByteBuffer {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
        val inputBuffer = ByteBuffer.allocateDirect(
            BATCH_SIZE * INPUT_SIZE * INPUT_SIZE * CHANNELS * FLOAT_SIZE
        )
        inputBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        scaledBitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

        for (pixel in pixels) {
            val r = ((pixel shr 16) and 0xFF).toFloat()
            val g = ((pixel shr 8) and 0xFF).toFloat()
            val b = (pixel and 0xFF).toFloat()
            inputBuffer.putFloat(r)
            inputBuffer.putFloat(g)
            inputBuffer.putFloat(b)
        }

        scaledBitmap.recycle()
        return inputBuffer
    }
}
