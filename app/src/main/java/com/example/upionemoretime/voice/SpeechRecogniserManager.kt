package com.example.upionemoretime.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

class SpeechRecognitionManager(
    private val context: Context
) {

    private var speechRecognizer: SpeechRecognizer? = null

    fun startListening(
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("Speech Recognition Not Available")
            return
        }

        speechRecognizer?.destroy()

        speechRecognizer =
            SpeechRecognizer.createSpeechRecognizer(context)

        speechRecognizer?.setRecognitionListener(

            object : RecognitionListener {

                override fun onResults(results: Bundle?) {

                    val matches =
                        results?.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        )

                    val text = matches?.firstOrNull()

                    if (!text.isNullOrEmpty()) {
                        onResult(text)
                    } else {
                        onError("No Speech Detected")
                    }
                }

                override fun onError(error: Int) {

                    val errorMessage = when (error) {


                        SpeechRecognizer.ERROR_AUDIO ->
                            "ERROR_AUDIO"

                        SpeechRecognizer.ERROR_CLIENT ->
                            "ERROR_CLIENT"

                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                            "ERROR_PERMISSION"

                        SpeechRecognizer.ERROR_NETWORK ->
                            "ERROR_NETWORK"

                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                            "ERROR_TIMEOUT"

                        SpeechRecognizer.ERROR_NO_MATCH ->
                            "ERROR_NO_MATCH"

                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
                            "ERROR_BUSY"

                        SpeechRecognizer.ERROR_SERVER ->
                            "ERROR_SERVER"

                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                            "ERROR_SPEECH_TIMEOUT"

                        else ->
                            "UNKNOWN_ERROR_$error"
                    }

                    onError(errorMessage)
                }

                override fun onReadyForSpeech(params: Bundle?) {}

                override fun onRmsChanged(rmsdB: Float) {}

                override fun onBeginningOfSpeech() {}

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {}

                override fun onPartialResults(partialResults: Bundle?) {}

                override fun onEvent(
                    eventType: Int,
                    params: Bundle?
                ) {}
            }
        )

        val intent = Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        ).apply {

            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )

            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                "en-US"
            )

            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "en-US"
            )

            putExtra(
                RecognizerIntent.EXTRA_MAX_RESULTS,
                5
            )

            putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                true
            )

            putExtra(
                RecognizerIntent.EXTRA_PREFER_OFFLINE,
                false
            )

            putExtra(
                RecognizerIntent.EXTRA_CALLING_PACKAGE,
                context.packageName
            )
        }

        intent.putExtra(
            RecognizerIntent.EXTRA_PREFER_OFFLINE,
            false
        )
        speechRecognizer?.startListening(intent)
    }

    fun destroy() {
        speechRecognizer?.destroy()
    }
}