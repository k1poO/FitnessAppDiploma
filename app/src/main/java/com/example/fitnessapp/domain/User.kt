package com.example.fitnessapp.domain

data class User(
    val id: String,
    val name: String,
    val lastName: String,
    val gender: String,
    val level: String,
    val weight: Double = 70.00,
    val height: Int = 170,
    val isUserLoginWithGoogle: Boolean = false,
    val listOfPlans: List<WorkoutPlan>? = listOf()
) {
    constructor() : this("", "", "", "", "", 0.0, 0, false, listOf())
}