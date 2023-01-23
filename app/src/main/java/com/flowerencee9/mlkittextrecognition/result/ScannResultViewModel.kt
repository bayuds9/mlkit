package com.flowerencee9.mlkittextrecognition.result

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.flowerencee9.mlkittextrecognition.support.getCurrentTime
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ScannResultViewModel : ViewModel() {
    companion object {
        private val TAG = ScannResultViewModel::class.java.simpleName
    }

    private val database = Firebase.firestore
    private val _storedText : MutableLiveData<Map<String, String>> = MutableLiveData(mutableMapOf())
    val storedText : LiveData<Map<String, String>> get() = _storedText

    private var _success: MutableLiveData<Boolean> = MutableLiveData()
    val success: LiveData<Boolean> get() = _success

    private var _progress: MutableLiveData<Boolean> = MutableLiveData()
    val progress: LiveData<Boolean> get() = _progress

    fun retrieveStoredText() {
        database.collection("scann")
            .get()
            .addOnSuccessListener { response ->
                val valueOfResult = mutableMapOf<String, String>()
                for (text in response) {
                    Log.d(TAG, "${text.id} -> ${text.data.entries}")
                    val resultKey = text.data.keys.firstOrNull()
                    val resultValue = text.data.values.firstOrNull()
                    valueOfResult[resultKey.toString()] = resultValue.toString()
                }
                _storedText.value = valueOfResult
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                Log.e(TAG, "failed retrieve stored data")
            }
    }

    fun saveText(text: String) {
        _progress.value = true
        val storeResult = hashMapOf(
            getCurrentTime() to text
        )
        database.collection("scann")
            .add(storeResult)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    retrieveStoredText()
                    _success.value = true
                    Log.d(TAG, "success store data ${task.result}")
                } else {
                    _success.value = false
                    task.exception?.printStackTrace()
                    Log.e(TAG, task.exception.toString())
                    Log.d(TAG, "failed store data $storeResult")
                }
                _progress.value = false
            }
    }
}