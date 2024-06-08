package com.example.fitnessapp.presentation.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.domain.User
import com.example.fitnessapp.domain.WorkoutPlan
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class WorkoutViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val usersReference = firebaseDatabase.getReference("Users")

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>
        get() = _error

    private val _user = MutableLiveData<User>()
    val user: LiveData<User>
        get() = _user

    init {
        viewModelScope.launch {
            val currentUserId = auth.currentUser?.uid
            currentUserId?.let {
                getUserData(it)
            } ?: run {
                _error.value = "User not logged in"
            }
        }
    }

    private suspend fun getUserData(userId: String) {
        try {
            val userSnapshot = withContext(Dispatchers.IO) {
                usersReference.child(userId).get().await()
            }
            _user.value = userSnapshot.getValue(User::class.java)
        } catch (e: Exception) {
            _error.value = e.message
        }
    }

    fun addWorkoutPlanToUser(workoutPlan: WorkoutPlan, isAddedFromRecRecycler: Boolean) {
        workoutPlan.workoutDay.map {  Log.d("WorkoutPlanGenerator", "Day ${it.day}: Rest Day - ${it.restDay}, Exercises - ${it.exercises.size}") }
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                val currentUser = _user.value ?: return@launch
                val updatedPlans = currentUser.listOfPlans?.toMutableList() ?: mutableListOf()

                if (!isPlanAlreadyAdded(updatedPlans, workoutPlan.bodyPart)) {
                    if (isAddedFromRecRecycler) {
                        updatedPlans.add(workoutPlan)
                    }

                    val updatedUser = currentUser.copy(listOfPlans = updatedPlans)

                    Log.d("WorkoutViewModel", "Updating user with plans: $updatedUser")

                    withContext(Dispatchers.IO) {
                        usersReference.child(userId).setValue(updatedUser).await()
                    }
                    if (isActive) {
                        _user.value = updatedUser
                        Log.d("WorkoutViewModel", "Workout plan added successfully")
                    }
                } else {
                    Log.d("WorkoutViewModel", "Workout plan for ${workoutPlan.bodyPart} already exists")
                }
            } catch (e: Exception) {
                if (isActive) {
                    _error.value = e.message
                    Log.e("WorkoutViewModel", "Error adding workout plan: ${e.message}")
                }
            }
        }
    }

    private fun isPlanAlreadyAdded(plans: List<WorkoutPlan>, bodyPart: String): Boolean {
        return plans.any { it.bodyPart == bodyPart }
    }
}