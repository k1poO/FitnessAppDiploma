package com.example.fitnessapp.presentation.viewModels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.domain.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val usersReference = firebaseDatabase.getReference("Users")

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private lateinit var googleSignInClient: GoogleSignInClient

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser == null) {
                _user.postValue(null)
            } else {
                fetchUserDetails(firebaseUser.uid)
            }
        }
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(application, gso)
    }

    private fun fetchUserDetails(userId: String) {
        viewModelScope.launch {
            try {
                val userSnapshot = withContext(Dispatchers.IO) {
                    usersReference.child(userId).get().await()
                }
                val user = userSnapshot.getValue(User::class.java)
                _user.postValue(user)
            } catch (e: Exception) {
                _error.postValue(e.message)
            }
        }
    }

    private fun clearSharedPreferences() {
        viewModelScope.launch(Dispatchers.IO) {
            val sharedPreferences = getApplication<Application>().getSharedPreferences(
                "WaterIntakePrefs",
                Context.MODE_PRIVATE
            )
            sharedPreferences.edit().clear().apply()
        }
    }

    fun updateUserProfile(name: String?, lastName: String?, level: String?, weight: Double?, height: Int?) {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid

        viewModelScope.launch {
            try {
                val currentUserData = withContext(Dispatchers.IO) {
                    usersReference.child(userId).get().await().getValue(User::class.java)
                } ?: return@launch

                val updatedUser = currentUserData.copy(
                    name = name ?: currentUserData.name,
                    lastName = lastName ?: currentUserData.lastName,
                    level = level ?: currentUserData.level,
                    weight = weight ?: currentUserData.weight,
                    height = height ?: currentUserData.height
                )

                withContext(Dispatchers.IO) {
                    usersReference.child(userId).setValue(updatedUser).await()
                }

                _user.postValue(updatedUser)
            } catch (e: Exception) {
                _error.postValue(e.message)
            }
        }
    }



    fun logout() {
        clearSharedPreferences()
        googleSignInClient.signOut().addOnCompleteListener {
            auth.signOut()
            _user.postValue(null) // Очистить данные пользователя при выходе
        }
    }
}