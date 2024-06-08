package com.example.fitnessapp.presentation.viewModels

import androidx.lifecycle.ViewModel
import com.example.fitnessapp.domain.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CompleteRegisterViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val usersReference = firebaseDatabase.getReference("Users")

    fun createUser(
        name: String,
        lastName: String,
        gender: String,
        level: String,
        weight: Double,
        height: Int
    ): User {
        val firebaseUser = auth.currentUser ?: throw RuntimeException("firebaseUser is null!")
        return User(firebaseUser.uid, name, lastName, gender, level, weight, height, false, null)
    }

    fun addUserToDatabase(user: User) {
        usersReference.child(user.id).setValue(user)
    }
}