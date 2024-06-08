package com.example.fitnessapp.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WorkoutDay(
    val day: Int = 1,
    val restDay: String,
    val planId: String,
    val exercises: List<ExerciseWithReps>
) : Parcelable
{
    constructor() : this(1, "", "", listOf())
}
