package com.example.upionemoretime.voice.biometrics

import kotlin.math.*

/**
 * Robust Mel-Spectrogram Feature Extractor for ECAPA-TDNN
 * Performs Pre-emphasis, Windowing, FFT, Mel-Filtering, and Log-scaling.
 * Enforces a FIXED frame count of 360 (approx 3.6s) as required by the ONNX model.
 */
class VoiceFeatureExtractor {

    private val sampleRate = 16000
    private val hopLength = 160 // 10ms
    private val winLength = 400 // 25ms
    private val nFFT = 512      // Next power of 2 for 400
    private val nMels = 80
    val requiredFrames = 360    // THE MODEL MUST HAVE EXACTLY THIS

    // Pre-calculate Hamming window to save CPU cycles during extraction
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

    fun extractFeatures(audio: FloatArray): FloatArray {
        if (audio.isEmpty()) return FloatArray(requiredFrames * nMels)

        // 1. Pre-emphasis (Standard voice DSP to boost high frequencies)
        val preEmphasized = FloatArray(audio.size)
        preEmphasized[0] = audio[0]
        for (i in 1 until audio.size) {
            preEmphasized[i] = audio[i] - 0.97f * audio[i - 1]
        }

        // 2. Calculate actual frames captured
        val actualFrames = ((preEmphasized.size - winLength) / hopLength) + 1
        
        // 3. Initialize feature array with FIXED size [360 * 80]
        // We fill with -10f (representing log10(1e-10)) so that empty frames 
        // are treated as silence, not as high-energy signals.
        val features = FloatArray(requiredFrames * nMels) { -10f }
        
        // 4. Process frames
        val framesToProcess = min(actualFrames, requiredFrames)
        
        val fftReal = FloatArray(nFFT)
        val fftImag = FloatArray(nFFT)

        for (f in 0 until framesToProcess) {
            val start = f * hopLength
            
            // a. Windowing (Hamming) and Zero-padding to nFFT
            for (i in 0 until nFFT) {
                if (i < winLength && (start + i) < preEmphasized.size) {
                    fftReal[i] = preEmphasized[start + i] * hammingWindow[i]
                } else {
                    fftReal[i] = 0f
                }
                fftImag[i] = 0f
            }

            // b. Perform FFT (Radix-2)
            performFFT(fftReal, fftImag)

            // c. Compute Power Spectrum
            val powerSpectrum = FloatArray(nFFT / 2 + 1)
            for (i in 0 until (nFFT / 2 + 1)) {
                // Magnitude squared
                powerSpectrum[i] = (fftReal[i] * fftReal[i] + fftImag[i] * fftImag[i]) / nFFT
            }

            // d. Apply Mel Filterbank
            for (m in 0 until nMels) {
                var melEnergy = 0f
                for (k in 0 until (nFFT / 2 + 1)) {
                    melEnergy += powerSpectrum[k] * melFilters[m][k]
                }
                
                // e. Log scaling (using log10 for stability)
                // We use max(energy, 1e-10) to avoid log(0)
                features[f * nMels + m] = log10(max(melEnergy, 1e-10f))
            }
        }
        
        return features
    }

    /**
     * Simple Radix-2 FFT implementation
     */
    private fun performFFT(real: FloatArray, imag: FloatArray) {
        val n = real.size
        // Bit-reversal permutation
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

        // Butterfly computations
        var m = 1
        while (m < n) {
            val step = m shl 1
            val arg = -PI / m
            var wR = 1.0
            var wI = 0.0
            val uR = cos(arg)
            val uI = sin(arg)
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
