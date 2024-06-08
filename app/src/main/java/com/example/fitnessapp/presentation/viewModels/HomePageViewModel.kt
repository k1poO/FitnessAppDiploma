package com.example.fitnessapp.presentation.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.fitnessapp.api.ApiFactory
import com.example.fitnessapp.data.AppDatabase
import com.example.fitnessapp.domain.ExerciseWithReps
import com.example.fitnessapp.domain.ExercisesItem
import com.example.fitnessapp.domain.User
import com.example.fitnessapp.domain.WorkoutDay
import com.example.fitnessapp.domain.WorkoutPlan
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class HomePageViewModel(application: Application) : AndroidViewModel(application) {
    private val db: AppDatabase = Room.databaseBuilder(
        application,
        AppDatabase::class.java, "fitness-database"
    ).build()

    private val auth = FirebaseAuth.getInstance()
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val usersReference = firebaseDatabase.getReference("Users")

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> get() = _user

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _bodyParts = MutableLiveData<List<String>>()
    private val bodyParts: LiveData<List<String>> get() = _bodyParts

    private val _workoutPlans = MutableLiveData<List<WorkoutPlan>>()
    val workoutPlans: LiveData<List<WorkoutPlan>> get() = _workoutPlans

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    init {
        _loading.value = true
        viewModelScope.launch {
            val currentUserId = auth.currentUser?.uid
            currentUserId?.let {
                getUserData(it)
                getBodyParts()
            } ?: run {
                _loading.value = false
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
        checkLoadingComplete()
    }

    private suspend fun getBodyParts() {
        try {
            val exercises = withContext(Dispatchers.IO) {
                ApiFactory.apiService.getBodyParts().blockingGet()
            }
            _bodyParts.value = exercises
            Log.d("HomePageViewModel", bodyParts.value.toString())
            createWorkoutPlans()
        } catch (e: Exception) {
            _error.value = e.message
        }
        checkLoadingComplete()
    }

    private fun createWorkoutPlans() = viewModelScope.launch {
        val bodyPartsList = bodyParts.value ?: return@launch
        val workoutList = mutableListOf<WorkoutPlan>()

        bodyPartsList.forEach { bodyPart ->
            val exercises = getExercises(bodyPart)
            if (exercises.isNotEmpty()) {
                user.value?.let {
                    val workoutPlan = generateWorkoutPlan(exercises, it.level, bodyPart)
                    workoutList.add(workoutPlan)
                }
            } else {
                Log.e("HomePageViewModel", "No exercises found for body part: $bodyPart")
            }
        }
        _workoutPlans.value = workoutList
        checkLoadingComplete()
    }

    private suspend fun getExercises(bodyPart: String): List<ExercisesItem> =
        withContext(Dispatchers.IO) {
            var exercises = db.exerciseDao().getExercisesByBodyPart(bodyPart)
            if (exercises.isEmpty()) {
                try {
                    val apiExercises = ApiFactory.apiService.getExercises(bodyPart).blockingGet()
                    Log.d("HomePageViewModel", apiExercises.toString())
                    db.exerciseDao().insertExercises(apiExercises)
                    exercises = db.exerciseDao().getExercisesByBodyPart(bodyPart)
                } catch (e: Exception) {
                    Log.e("HomePageViewModel", "Error loading exercises from API", e)
                    _error.postValue(e.message)
                }
            }
            exercises
        }

    private fun generateWorkoutPlan(
        exercises: List<ExercisesItem>,
        level: String,
        bodyPart: String
    ): WorkoutPlan {
        val planId = UUID.randomUUID().toString()
        val listOfDays = mutableListOf<WorkoutDay>()
        val restDayFrequency = when (level) {
            "Beginner" -> 3
            "Intermediate" -> 4
            "Expert" -> 5
            else -> 4
        }

        for (day in 1..30) {
            if (day % restDayFrequency == 0) {
                listOfDays.add(WorkoutDay(day, "true", planId, listOf()))
            } else {
                val dailyExercises = exercises.shuffled().take(5)
                val repetitions = when (level) {
                    "Beginner" -> 10..15
                    "Intermediate" -> 15..20
                    "Expert" -> 20..25
                    else -> 15..20
                }
                val exercisesWithReps = dailyExercises.map { exercise ->
                    ExerciseWithReps(exercise, repetitions.random())
                }
                listOfDays.add(WorkoutDay(day, "false", planId, exercisesWithReps))
            }
        }
        return WorkoutPlan(planId, listOfDays, level, bodyPart)
    }


    private fun checkLoadingComplete() {
        val userLoaded = _user.value != null
        val bodyPartsLoaded = _bodyParts.value != null
        val workoutPlansLoaded = _workoutPlans.value != null
        _loading.value = !(userLoaded && bodyPartsLoaded && workoutPlansLoaded)
    }
}