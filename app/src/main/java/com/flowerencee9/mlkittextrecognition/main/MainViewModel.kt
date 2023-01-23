package com.flowerencee9.mlkittextrecognition.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.flowerencee9.mlkittextrecognition.support.getCurrentTime
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class MainViewModel : ViewModel() {
    companion object {
        private val TAG = MainViewModel::class.java.simpleName
    }

    private var recognizer: TextRecognizer =
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private var database: FirebaseFirestore = Firebase.firestore

    private var _success: MutableLiveData<Boolean> = MutableLiveData()
    val success: LiveData<Boolean> get() = _success

    private var _resultText: MutableLiveData<String> = MutableLiveData()
    val resultText: LiveData<String> get() = _resultText

    private var _message: MutableLiveData<String> = MutableLiveData()
    val message: LiveData<String> get() = _message

    private var _progress: MutableLiveData<Boolean> = MutableLiveData()
    val progress: LiveData<Boolean> get() = _progress

    fun readText(inputImage: InputImage) {
        _progress.value = true
        Log.d(TAG, "input image $inputImage")
        recognizer.process(inputImage)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val resultText = task.result.text
                    processRawResult(resultText)
                    Log.d(TAG, "result text $resultText")
                } else {
                    task.exception?.printStackTrace()
                    _resultText.value = task.exception?.message
                    _message.value = "Failed to read text"
                    _success.value = false
                    _progress.value = false
                }
            }

    }

    private fun processRawResult(result: String) {
        val storedValue = hashMapOf(
            getCurrentTime() to result
        )
        database.collection("scann")
            .add(storedValue)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _resultText.value = result
                    _success.value = true
                    Log.d(TAG, "success store data ${task.result}")
                } else {
                    task.exception?.printStackTrace()
                    _message.value = "Failed to store value to cloud\nYou still can store it later"
                    _resultText.value = task.exception?.message
                    _success.value = false
                    Log.e(TAG, task.exception.toString())
                }
                _progress.value = false
            }
    }
}