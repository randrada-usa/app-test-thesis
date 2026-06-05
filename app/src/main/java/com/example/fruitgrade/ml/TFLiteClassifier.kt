package com.example.fruitgrade.ml

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class TFLiteClassifier(context: Context, private val modelName: String) {
    private val interpreter: Interpreter
    private val labels = listOf("overripe", "ripe", "rotten", "unripe")

    init {
        val model = loadModelFile(context, modelName)
        val options = Interpreter.Options().apply {
            numThreads = 4
        }
        interpreter = Interpreter(model, options)
    }

    private fun loadModelFile(context: Context, modelName: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelName)
        FileInputStream(fileDescriptor.fileDescriptor).use { inputStream ->
            val channel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength
            return channel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        }
    }

    fun classify(imageBuffer: ByteBuffer): Pair<String, Float> {
        val output = Array(1) { FloatArray(labels.size) }
        interpreter.run(imageBuffer, output)
        val probabilities = output[0]
        val maxIndex = probabilities.indices.maxBy { probabilities[it] }
        val label = labels[maxIndex]
        val confidence = probabilities[maxIndex]
        return label to confidence
    }

    fun close() {
        interpreter.close()
    }
}
