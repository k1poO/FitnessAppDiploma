package com.example.fitnessapp.domain

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "exercises")
data class ExercisesItem(
    val bodyPart: String,
    val equipment: String,
    val gifUrl: String,
    @PrimaryKey val id: String,
    val instructions: List<String>,
    val name: String,
    val secondaryMuscles: List<String>,
    val target: String
) : Parcelable
{
    constructor() : this("", "", "", "0", listOf(), "", listOf(), "")
}