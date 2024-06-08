package com.example.fitnessapp.presentation.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>
        get() = _error

    private val _user = MutableLiveData<FirebaseUser?>()
    val user: LiveData<FirebaseUser?>
        get() = _user

    init {
        auth.addAuthStateListener {
            if (it.currentUser != null) {
                _user.value = it.currentUser
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _error.value = "Email and password must not be empty"
            return
        }
        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
        }.addOnFailureListener {
            _error.value = it.message
        }
    }
}
