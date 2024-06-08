package com.example.fitnessapp.domain

import java.util.Locale

enum class BodyParts {
    BACK,
    CARDIO,
    CHEST,
    LOWER_ARMS,
    LOWER_LEGS,
    NECK,
    SHOULDERS,
    UPPER_ARMS,
    UPPER_LEGS,
    WAIST;

    fun main() {
        val bodyParts = entries.map { it.name.toLowerCase() }
        println("Body parts loaded: $bodyParts")
    }
}
