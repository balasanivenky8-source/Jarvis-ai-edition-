package com.jarvis.assistant

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

class VoiceService(
    private val context: Context
) {

    private var recognizer:
        SpeechRecognizer? = null


    fun startListening(
        onResult: (String) -> Unit
    ) {

        if (
            !SpeechRecognizer
                .isRecognitionAvailable(context)
        ) {
            return
        }


        recognizer =
            SpeechRecognizer
                .createSpeechRecognizer(context)


        recognizer?.setRecognitionListener(
            SimpleRecognitionListener(
                onResult
            )
        )


        val intent =
            Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH
            )

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            "en-IN"
        )


        recognizer?.startListening(intent)
    }


    fun stopListening() {

        recognizer?.stopListening()

        recognizer?.destroy()

        recognizer = null
    }
}
