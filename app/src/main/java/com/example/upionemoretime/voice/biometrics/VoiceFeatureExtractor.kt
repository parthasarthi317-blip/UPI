package com.example.upionemoretime.voice.biometrics

import kotlin.math.*

/**
 * Optimized FBank Feature Extractor for ECAPA-TDNN.
 * Aligned with SpeechBrain/Kaldi-style preprocessing.
 */
class VoiceFeatureExtractor {

    private val sampleRate = 16000
    private val hopLength = 160 // 10ms
    private val winLength = 400 // 25ms
    private val nFFT = 512      // Next power of 2 for 400
    private val nMels = 80
    val requiredFrames = 360    // THE MODEL MUST HAVE EXACTLY THIS

    // Pre-calculate Hamming window
    private val hammingWindow = FloatArray(winLength) { i ->
        (0.54 - 0.46 * cos(2.0 * PI * i / (winLength - 1))).toFloat()
    }

    // Pre-calculate Mel Filterbank weights
    private val melFilters: Array<FloatArray> by lazy { createMelFilters() }

    private fun createMelFilters(): Array<FloatArray> {
        val filters = Array(nMels) { FloatArray(nFFT / 2 + 1) }
        val minMel = hzToMel(0f)
        val maxMel = hzToMel(sampleRate / 2f)
        
        val melPoints = FloatArray(nMels + 2) { i ->
            melToHz(minMel + i * (maxMel - minMel) / (nMels + 1))
        }
        
        val binPoints = IntArray(nMels + 2) { i ->
            floor((nFFT + 1) * melPoints[i] / sampleRate).toInt()
        }

        for (m in 1..nMels) {
            val left = binPoints[m - 1]
            val center = binPoints[m]
            val right = binPoints[m + 1]

            for (k in left until center) {
                if (k < filters[m - 1].size) {
                    filters[m - 1][k] = (k - left).toFloat() / (center - left)
                }
            }
            for (k in center until right) {
                if (k < filters[m - 1].size) {
                    filters[m - 1][k] = (right - k).toFloat() / (right - center)
                }
            }
        }
        return filters
    }

    private fun hzToMel(hz: Float): Float = 2595f * log10(1f + hz / 700f)
    private fun melToHz(mel: Float): Float = 700f * (10f.pow(mel / 2595f) - 1f)

    fun extractFeatures(rawAudio: FloatArray): FloatArray {
        if (rawAudio.isEmpty()) return FloatArray(requiredFrames * nMels)

        // 1. DC Offset Removal (Mean Subtraction)
        val meanValue = rawAudio.average().toFloat()
        val audio = FloatArray(rawAudio.size) { i -> rawAudio[i] - meanValue }

        // 2. Simple VAD (Voice Activity Detection)
        // Discards silence to prevent it from corrupting the speaker embedding
        val activeAudio = applySimpleVAD(audio)

        // 3. Pre-emphasis
        val preEmphasized = FloatArray(activeAudio.size)
        preEmphasized[0] = activeAudio[0]
        for (i in 1 until activeAudio.size) {
            preEmphasized[i] = activeAudio[i] - 0.97f * activeAudio[i - 1]
        }

        // 4. Calculate actual frames
        val actualFrames = ((preEmphasized.size - winLength) / hopLength) + 1
        val features = FloatArray(requiredFrames * nMels)
        
        val framesToProcess = min(actualFrames, requiredFrames)
        val fftReal = FloatArray(nFFT)
        val fftImag = FloatArray(nFFT)

        for (f in 0 until framesToProcess) {
            val start = f * hopLength
            
            for (i in 0 until nFFT) {
                if (i < winLength && (start + i) < preEmphasized.size) {
                    fftReal[i] = preEmphasized[start + i] * hammingWindow[i]
                } else {
                    fftReal[i] = 0f
                }
                fftImag[i] = 0f
            }

            performFFT(fftReal, fftImag)

            val powerSpectrum = FloatArray(nFFT / 2 + 1)
            for (i in 0 until (nFFT / 2 + 1)) {
                powerSpectrum[i] = (fftReal[i] * fftReal[i] + fftImag[i] * fftImag[i]) / nFFT
            }

            for (m in 0 until nMels) {
                var melEnergy = 0f
                for (k in 0 until (nFFT / 2 + 1)) {
                    melEnergy += powerSpectrum[k] * melFilters[m][k]
                }
                
                // 5. Log10 scaling - Standard for Kaldi/Common ECAPA models
                // Use a small epsilon to avoid log(0)
                features[f * nMels + m] = log10(max(melEnergy, 1e-10f))
            }
        }

        // 6. Padding Strategy: Reflective Padding (More natural for the model)
        if (framesToProcess in 1 until requiredFrames) {
            var forward = false
            var sourceFrame = framesToProcess - 1
            for (f in framesToProcess until requiredFrames) {
                if (sourceFrame <= 0) forward = true
                if (sourceFrame >= framesToProcess - 1) forward = false
                
                if (forward) sourceFrame++ else sourceFrame--
                
                System.arraycopy(features, sourceFrame * nMels, features, f * nMels, nMels)
            }
        }

        // 7. Global CMVN (Cepstral Mean and Variance Normalization)
        for (m in 0 until nMels) {
            var sum = 0f
            var sumSq = 0f
            for (f in 0 until requiredFrames) {
                val value = features[f * nMels + m]
                sum += value
                sumSq += value * value
            }
            val mean = sum / requiredFrames
            val variance = (sumSq / requiredFrames) - (mean * mean)
            val stdDev = sqrt(max(variance, 1e-6f))
            
            for (f in 0 until requiredFrames) {
                features[f * nMels + m] = (features[f * nMels + m] - mean) / stdDev
            }
        }
        
        return features
    }

    /**
     * Improved Voice Activity Detection (VAD).
     * Detects and removes leading/trailing silence and keeps only the speech regions.
     * Uses a 200ms safety margin to ensure word onsets/offsets aren't clipped.
     */
    private fun applySimpleVAD(audio: FloatArray): FloatArray {
        val frameSize = 160 // 10ms at 16kHz
        val totalFrames = audio.size / frameSize
        if (totalFrames < 10) return audio

        // Threshold for energy (sum of squares). 0.0002f is robust against light breathing.
        val threshold = 0.0002f
        val marginFrames = 20 // 200ms (20 frames * 10ms) safety margin

        val energies = FloatArray(totalFrames)
        for (i in 0 until totalFrames) {
            val start = i * frameSize
            var sumSq = 0f
            for (j in 0 until frameSize) {
                val s = audio[start + j]
                sumSq += s * s
            }
            energies[i] = sumSq / frameSize
        }

        var firstActive = -1
        var lastActive = -1

        // Use a small smoothing window (5 frames) to find the speech boundaries.
        // This prevents quiet consonants or short pauses from being treated as silence.
        for (i in 0 until totalFrames) {
            var isSpeech = false
            for (j in -2..2) {
                val idx = i + j
                if (idx in 0 until totalFrames && energies[idx] > threshold) {
                    isSpeech = true
                    break
                }
            }
            
            if (isSpeech) {
                if (firstActive == -1) firstActive = i
                lastActive = i
            }
        }

        // If no speech detected, return the original (the model will handle it)
        if (firstActive == -1) return audio

        // Apply the 200ms safety margin to the detected speech boundaries
        val startFrame = max(0, firstActive - marginFrames)
        val endFrame = min(totalFrames - 1, lastActive + marginFrames)

        val startSample = startFrame * frameSize
        val endSample = min((endFrame + 1) * frameSize, audio.size)

        return audio.sliceArray(startSample until endSample)
    }

    private fun performFFT(real: FloatArray, imag: FloatArray) {
        val n = real.size
        var j = 0
        for (i in 0 until n - 1) {
            if (i < j) {
                val tempR = real[i]; real[i] = real[j]; real[j] = tempR
                val tempI = imag[i]; imag[i] = imag[j]; imag[j] = tempI
            }
            var m = n shr 1
            while (m >= 1 && j >= m) {
                j -= m
                m = m shr 1
            }
            j += m
        }
        var m = 1
        while (m < n) {
            val step = m shl 1
            val arg = -PI / m
            var wR = 1.0; var wI = 0.0
            val uR = cos(arg); val uI = sin(arg)
            for (k in 0 until m) {
                for (i in k until n step step) {
                    val target = i + m
                    val tR = (wR * real[target] - wI * imag[target]).toFloat()
                    val tI = (wR * imag[target] + wI * real[target]).toFloat()
                    real[target] = real[i] - tR
                    imag[target] = imag[i] - tI
                    real[i] += tR
                    imag[i] += tI
                }
                val nextWR = wR * uR - wI * uI
                wI = wR * uI + wI * uR
                wR = nextWR
            }
            m = step
        }
    }

    fun getFrameCount(audioSize: Int): Int = requiredFrames
}
