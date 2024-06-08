package com.example.fitnessapp.presentation.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.api.ApiFactory
import com.example.fitnessapp.domain.ExercisesItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchViewModel() : ViewModel() {

    private val _exercises = MutableLiveData<List<ExercisesItem>>()
    val exercises: LiveData<List<ExercisesItem>> get() = _exercises

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _selectedExercise = MutableLiveData<ExercisesItem?>()
    val selectedExercise: LiveData<ExercisesItem?> get() = _selectedExercise

    fun searchExercises(bodyPart: String, equipment: String, target: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                ApiFactory.apiService.getExercises(bodyPart).blockingGet()
            }
            result.filter { it.equipment == equipment && it.target == target }
            _exercises.value = result
            _isLoading.value = false
        }
    }

    fun selectExercise(exercise: ExercisesItem) {
        _selectedExercise.value = exercise
    }

    fun clearSelectedExercise() {
        _selectedExercise.value = null
    }
}