package com.example.upionemoretime.voice.biometrics

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.log10
import kotlin.math.sqrt

class AudioCaptureService {

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var recordingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat).coerceAtLeast(3200)

    @SuppressLint("MissingPermission")
    fun startCapturing(onDataCaptured: (FloatArray) -> Unit) {
        if (isRecording) {
            Log.w("BIOMETRIC", "AudioCapture: Already recording, ignoring start request")
            return
        }

        release() 

        Log.e("BIOMETRIC", "AudioCapture: STARTING (Source: MIC, Rate: $sampleRate, Buffer: $bufferSize)")
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC, 
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e("BIOMETRIC", "AudioCapture: Initialization FAILED - State not Initialized")
                onDataCaptured(FloatArray(0))
                return
            }

            audioRecord?.startRecording()
            if (audioRecord?.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
                Log.e("BIOMETRIC", "AudioCapture: Failed to enter RECORDING state (state: ${audioRecord?.recordingState})")
                onDataCaptured(FloatArray(0))
                return
            }
            Log.e("BIOMETRIC", "AudioCapture: SUCCESS - Microphone is now ACTIVE")
        } catch (e: Exception) {
            Log.e("BIOMETRIC", "AudioCapture: Exception during start: ${e.message}")
            onDataCaptured(FloatArray(0))
            return
        }

        isRecording = true

        recordingJob = scope.launch {
            val audioBuffer = ShortArray(bufferSize)
            val chunks = mutableListOf<ShortArray>()
            var totalRead = 0

            try {
                while (isRecording) {
                    val read = audioRecord?.read(audioBuffer, 0, bufferSize) ?: 0
                    if (read > 0) {
                        chunks.add(audioBuffer.copyOf(read))
                        totalRead += read
                        
                        // Volume logging (RMS)
                        var sum = 0.0
                        for (i in 0 until read) {
                            sum += audioBuffer[i] * audioBuffer[i]
                        }
                        val rms = sqrt(sum / read)
                        val db = 20 * log10(rms.coerceAtLeast(1.0))
                        
                        if (chunks.size % 10 == 0) {
                            Log.d("BIOMETRIC", "AudioCapture: Listening... (Samples: $totalRead, Volume: ${db.toInt()}dB)")
                        }
                    } else if (read < 0) {
                        Log.e("BIOMETRIC", "AudioCapture: Read error code: $read")
                        break
                    } else {
                        Thread.sleep(20)
                    }
                }
            } catch (e: Exception) {
                Log.e("BIOMETRIC", "AudioCapture: Loop exception: ${e.message}")
            } finally {
                Log.e("BIOMETRIC", "AudioCapture: STOPPED. Final count: $totalRead samples.")
                val floatData = convertToFloat(chunks, totalRead)
                onDataCaptured(floatData)
            }
        }
    }

    private fun convertToFloat(chunks: List<ShortArray>, totalSize: Int): FloatArray {
        if (totalSize <= 0) return FloatArray(0)
        val floatData = FloatArray(totalSize)
        var offset = 0
        for (chunk in chunks) {
            for (i in chunk.indices) {
                floatData[offset++] = chunk[i] / 32768.0f
            }
        }
        return floatData
    }

    fun stopCapturing() {
        if (!isRecording) return
        Log.e("BIOMETRIC", "AudioCapture: Stop requested")
        isRecording = false
        try {
            audioRecord?.stop()
        } catch (e: Exception) {
            Log.e("BIOMETRIC", "AudioCapture: Stop exception: ${e.message}")
        }
    }

    fun release() {
        stopCapturing()
        try {
            audioRecord?.release()
        } catch (e: Exception) {}
        audioRecord = null
    }
}
