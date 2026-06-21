package com.example.upionemoretime.voice.biometrics

import android.content.Context
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import ai.onnxruntime.TensorInfo
import android.util.Log
import java.nio.FloatBuffer

class OnnxEmbeddingEngine(private val context: Context) {

    private var ortEnv: OrtEnvironment = OrtEnvironment.getEnvironment()
    private var ortSession: OrtSession? = null
    private val featureExtractor = VoiceFeatureExtractor()

    init {
        loadModel()
    }

    private fun loadModel() {
        try {
            val modelBytes = context.assets.open("ecapa_tdnn.onnx").readBytes()
            ortSession = ortEnv.createSession(modelBytes)
            Log.e("BIOMETRIC", "ONNX: Model loaded successfully")
            
            ortSession?.let { session ->
                session.inputInfo.forEach { (name, info) ->
                    val tensorInfo = info.info as? TensorInfo
                    Log.e("BIOMETRIC", "ONNX Model Input: '$name', Shape=${tensorInfo?.shape?.contentToString()}")
                }
            }
        } catch (e: Exception) {
            Log.e("BIOMETRIC", "ONNX: Error loading model: ${e.message}")
        }
    }

    fun generateEmbedding(audioData: FloatArray): FloatArray? {
        val session = ortSession ?: return null

        return try {
            if (audioData.isEmpty()) return null

            // 1. Convert raw audio to Mel Spectrogram features
            val features = featureExtractor.extractFeatures(audioData)
            val frameCount = featureExtractor.getFrameCount(audioData.size)
            
            if (features.isEmpty() || frameCount == 0) {
                Log.e("BIOMETRIC", "ONNX: Feature extraction failed")
                return null
            }

            // 2. Prepare shape [1, Frames, 80]
            val inputName = session.inputNames.firstOrNull() ?: "feature"
            val shape = longArrayOf(1, frameCount.toLong(), 80)

            Log.e("BIOMETRIC", "ONNX: Inference on '$inputName' with shape [1, $frameCount, 80]")

            val floatBuffer = FloatBuffer.wrap(features)
            val inputTensor = OnnxTensor.createTensor(ortEnv, floatBuffer, shape)
            
            val inputs = mapOf(inputName to inputTensor)
            val result = session.run(inputs)
            
            val outputTensor = result?.get(0) as OnnxTensor
            val outputBuffer = outputTensor.floatBuffer
            val embedding = FloatArray(outputBuffer.remaining())
            outputBuffer.get(embedding)
            
            inputTensor.close()
            result.close()
            
            Log.e("BIOMETRIC", "ONNX: Success! Embedding size: ${embedding.size}")
            embedding
        } catch (e: Exception) {
            Log.e("BIOMETRIC", "ONNX Inference Error: ${e.message}")
            null
        }
    }

    fun calculateCosineSimilarity(vec1: FloatArray, vec2: FloatArray): Float {
        var dotProduct = 0.0f
        var norm1 = 0.0f
        var norm2 = 0.0f
        for (i in vec1.indices) {
            dotProduct += vec1[i] * vec2[i]
            norm1 += vec1[i] * vec1[i]
            norm2 += vec2[i] * vec2[i]
        }
        val similarity = dotProduct / (Math.sqrt(norm1.toDouble()).toFloat() * Math.sqrt(norm2.toDouble()).toFloat())
        Log.e("BIOMETRIC", "Similarity: $similarity")
        return similarity
    }

    fun close() {
        ortSession?.close()
        ortEnv.close()
    }
}
