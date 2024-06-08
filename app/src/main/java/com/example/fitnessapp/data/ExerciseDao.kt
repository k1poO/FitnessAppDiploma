package com.example.fitnessapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fitnessapp.domain.ExercisesItem

@Dao
interface ExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExercisesItem>)

    @Query("SELECT * FROM exercises WHERE bodyPart = :bodyPart")
    suspend fun getExercisesByBodyPart(bodyPart: String): List<ExercisesItem>

    @Query("DELETE FROM exercises")
    suspend fun deleteAllExercises()
}