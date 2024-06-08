package com.example.fitnessapp.presentation.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>
        get() = _error

    private val _success = MutableLiveData<Boolean?>()
    val success: LiveData<Boolean?>
        get() = _success

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _error.value = "Email for reset must not be empty"
            return
        }
        auth.sendPasswordResetEmail(email).addOnSuccessListener {
            _success.value = true
        }.addOnFailureListener {
            _error.value = it.message
        }
    }
}