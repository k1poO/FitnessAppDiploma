package com.example.fitnessapp.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class WorkoutPlan(
    val id: String = UUID.randomUUID().toString(),
    val workoutDay: List<WorkoutDay>,
    val level: String,
    val bodyPart: String,
    val dayNum: Int = 1
) : Parcelable
{
    constructor() : this(UUID.randomUUID().toString(), listOf(), "", "", 1)
}
