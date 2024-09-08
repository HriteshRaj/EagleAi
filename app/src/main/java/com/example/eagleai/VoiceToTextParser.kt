package com.example.eagleai

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.intellij.lang.annotations.Language

class VoiceToTextParser(private val app:Application,private val viewModel: ChatViewModel) : RecognitionListener{
    private val _state= MutableStateFlow(VoiceToTextState())
    val state=_state.asStateFlow()

    val recognizer= SpeechRecognizer.createSpeechRecognizer(app)

    fun startListening(language: String="en"){
        _state.update { VoiceToTextState()}
        if(!SpeechRecognizer.isRecognitionAvailable(app)){
            _state.update {
                it.copy(error = "Reocgnision not Available")

        }

        }
        val intent= Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE,language)

        }
        recognizer.setRecognitionListener(this)
        recognizer.startListening(intent)
        _state.update {
            it.copy(
                isSpeaking = true
            )
        }




    }
    fun stopListening(){
        _state.update {
            it.copy(
                isSpeaking = false
            )
        }
    }

    override fun onReadyForSpeech(params: Bundle?) {
        _state.update {
            it.copy(
             error=null
            )
        }
    }

    override fun onBeginningOfSpeech() =Unit

    override fun onRmsChanged(rmsdB: Float)=Unit


    override fun onBufferReceived(buffer: ByteArray?)=Unit

    override fun onEndOfSpeech() {
        _state.update {
            it.copy(
                isSpeaking = false
            )
        }
    }

    override fun onError(error: Int) {
       if(error== SpeechRecognizer.ERROR_CLIENT){
           return
       }
         _state.update {
            it.copy(
               error = "Error: $error"
            )
        }
    }

    override fun onResults(results: Bundle?) {
        results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.getOrNull(0)
            ?.let {
                result->
                _state.update {
                    it.copy(
                        spokenText = result
                    )

                }
                viewModel.onVoiceInput(result)
            }
    }

    override fun onPartialResults(partialResults: Bundle?)=Unit

    override fun onEvent(eventType: Int, params: Bundle?) = Unit
}
data class VoiceToTextState(
    val spokenText:String="",
    val isSpeaking:Boolean=false,
    val error:String?=null
)