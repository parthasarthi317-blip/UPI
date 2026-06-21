package com.example.upionemoretime.voice.biometrics

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class VoiceBiometricManager(context: Context) {

    private val vault = BiometricVault(context)
    private val audioCapture = AudioCaptureService()
    private val onnxEngine = OnnxEmbeddingEngine(context)

    private val _isEnrolling = MutableStateFlow(false)
    val isEnrolling: StateFlow<Boolean> = _isEnrolling

    private val enrollmentSamples = mutableListOf<FloatArray>()
    private val enrollmentCountRequired = 3

    fun isUserEnrolled(): Boolean = vault.isEnrolled()

    fun startEnrollment() {
        _isEnrolling.value = true
        enrollmentSamples.clear()
    }

    fun captureEnrollmentSample(onComplete: (FloatArray?) -> Unit) {
        audioCapture.startCapturing { audioData ->
            Log.e("BIOMETRIC", "VoiceBiometricManager: Received ${audioData.size} audio samples")
            val embedding = onnxEngine.generateEmbedding(audioData)
            onComplete(embedding)
        }
    }

    fun addEnrollmentSample(embedding: FloatArray): Int {
        enrollmentSamples.add(embedding)
        if (enrollmentSamples.size >= enrollmentCountRequired) {
            finalizeEnrollment()
        }
        return enrollmentSamples.size
    }

    private fun finalizeEnrollment() {
        if (enrollmentSamples.isEmpty()) return

        // Calculate centroid (average) of embeddings
        val dim = enrollmentSamples[0].size
        val centroid = FloatArray(dim)
        for (sample in enrollmentSamples) {
            for (i in 0 until dim) {
                centroid[i] += sample[i]
            }
        }
        for (i in 0 until dim) {
            centroid[i] /= enrollmentSamples.size.toFloat()
        }

        vault.storeMasterEmbedding(centroid)
        _isEnrolling.value = false
    }

    fun verifySpeaker(onResult: (FloatArray?) -> Unit) {
        audioCapture.startCapturing { audioData ->
            Log.e("BIOMETRIC", "VoiceBiometricManager: Received ${audioData.size} audio samples for verification")
            val currentEmbedding = onnxEngine.generateEmbedding(audioData)
            onResult(currentEmbedding)
        }
    }

    fun getSimilarity(embedding1: FloatArray, embedding2: FloatArray): Float {
        return onnxEngine.calculateCosineSimilarity(embedding1, embedding2)
    }

    fun getMasterEmbedding(): FloatArray? = vault.getMasterEmbedding()

    fun clearEnrollment() {
        vault.clear()
        enrollmentSamples.clear()
        _isEnrolling.value = false
        Log.e("BIOMETRIC", "User enrollment cleared.")
    }

    fun stopCapture() {
        audioCapture.stopCapturing()
    }

    fun destroy() {
        audioCapture.release()
        onnxEngine.close()
    }
}
