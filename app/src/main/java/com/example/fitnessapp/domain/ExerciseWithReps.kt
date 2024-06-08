package com.example.fitnessapp.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExerciseWithReps (
    val exercisesItem: ExercisesItem,
    val repetitions: Int
) : Parcelable
{
    constructor() : this(ExercisesItem(), 0)
}