package com.example.fitnessapp.presentation.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class RegisterViewModel : ViewModel() {

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

    fun signUp(email: String, password: String, name: String, lastName: String) {
        if (email.isBlank() || password.isBlank() || name.isBlank() || lastName.isBlank()) {
            _error.value = "Заполните все поля!"
            return
        }
        auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
            _user.value = it.user
        }.addOnFailureListener {
            _error.value = it.message
        }
    }

}