package com.vnazarov.speechlistener

import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.vnazarov.speechlistener.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import net.gotev.speech.GoogleVoiceTypingDisabledException
import net.gotev.speech.Speech
import net.gotev.speech.SpeechDelegate
import net.gotev.speech.SpeechRecognitionNotAvailable
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
//    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var srIntent: Intent

    private lateinit var mediaPlayer: MediaPlayer

    private val mediaJob = Job()
    private val mediaScope = CoroutineScope(Dispatchers.Main + mediaJob)

    private lateinit var delegate: SpeechDelegate
    private lateinit var speech: Speech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPermissions()
//        mediaPlayerInit()
//        initSpeechRecognizer()
//        mediaScope.launch { timerLauncher() }

        Speech.init(this, packageName)
        speech = Speech.getInstance()
        speech.setStopListeningAfterInactivity(1800000)
        objectInit()
        initSpeech()

        mediaScope.launch { timerLauncher() }

//        binding.ringBackground.setOnClickListener {
//            speechRecognizer.startListening(srIntent)
//        }
    }

    override fun onDestroy() {
        super.onDestroy()

        speech.shutdown()
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.RECORD_AUDIO,
                    android.Manifest.permission.INTERNET,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                1
            )
        }
    }

    private fun initSpeech() {
        try {
            speech.startListening(delegate)

        } catch (e: SpeechRecognitionNotAvailable) {
            Log.e("speech", "Speech recognition is not available on this device!")
        } catch (e: GoogleVoiceTypingDisabledException) {
            Log.e("speech", "Google voice typing must be enabled!")
        }
    }

    private fun objectInit(){
        delegate = object : SpeechDelegate{

            override fun onStartOfSpeech() {}

            override fun onSpeechRmsChanged(value: Float) {
                if (value < 9) binding.messageResult.text = "Listening"
                else binding.messageResult.text = "I hear you!"
            }

            override fun onSpeechPartialResults(results: MutableList<String>?) {

                if (results != null) {
                    Speech.getInstance().say("I hear you")
                }
            }

            override fun onSpeechResult(result: String?) {}
        }
    }

//    private fun initSpeechRecognizer() {
//        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
//
//        srIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
//        srIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
//        srIntent.putExtra(
//            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
//        )
//        srIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH)
//
//        speechRecognizer.setRecognitionListener(object : RecognitionListener {
//            override fun onReadyForSpeech(p0: Bundle?) {}
//
//            override fun onBeginningOfSpeech() {}
//
//            override fun onRmsChanged(p0: Float) {
//
//                if (p0 > 9) {
//                    binding.messageResult.text = "I hear you!"
//                } else binding.messageResult.text = "Listening..."
//            }
//
//            override fun onBufferReceived(p0: ByteArray?) {}
//
//            override fun onEndOfSpeech() {}
//
//            override fun onError(p0: Int) {}
//
//            override fun onResults(p0: Bundle?) {}
//
//            override fun onPartialResults(p0: Bundle?) {
//
//                val data = p0!!.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
//                if (data != null) {
//                    mediaScope.launch { playAudio() }
//                }
//            }
//
//            override fun onEvent(p0: Int, p1: Bundle?) {}
//        })
//
//        speechRecognizer.startListening(srIntent)
//    }

//    private fun mediaPlayerInit() {
//        mediaPlayer = MediaPlayer.create(this, R.raw.answer3)
//    }
//
//    private suspend fun playAudio() {
//        mediaPlayer.start()
//        delay(5000)
//    }
//
//    private suspend fun timerLauncher() {
//        speechRecognizer.startListening(srIntent)
//        delay(1)
//        timerLauncher()
//    }

    private suspend fun timerLauncher() {
        initSpeech()
        delay(1)
        timerLauncher()
    }
}