
package com.pulseforge.data.model

import java.util.Date

data class UserProfile(
    val userId: String = "",
    val displayName: String = "",
    val age: Int = 0,
    val height: Float = 0f,
    val weight: Float = 0f,
    val fitnessGoal: String = "",
    val preferredWorkoutDuration: Int = 60, // in minutes
    val workoutFrequency: Int = 3, // workouts per week
    val preferredExerciseTypes: List<ExerciseType> = listOf(),
    var streakCount: Int = 0,
    var lastWorkoutDate: Date? = null
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "displayName" to displayName,
            "age" to age,
            "height" to height,
            "weight" to weight,
            "fitnessGoal" to fitnessGoal,
            "preferredWorkoutDuration" to preferredWorkoutDuration,
            "workoutFrequency" to workoutFrequency,
            "preferredExerciseTypes" to preferredExerciseTypes.map { it.name },
            "streakCount" to streakCount,
            "lastWorkoutDate" to (lastWorkoutDate?.time ?: 0)
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): UserProfile {
            return UserProfile(
                userId = map["userId"] as? String ?: "",
                displayName = map["displayName"] as? String ?: "",
                age = (map["age"] as? Long)?.toInt() ?: 0,
                height = (map["height"] as? Double)?.toFloat() ?: 0f,
                weight = (map["weight"] as? Double)?.toFloat() ?: 0f,
                fitnessGoal = map["fitnessGoal"] as? String ?: "",
                preferredWorkoutDuration = (map["preferredWorkoutDuration"] as? Long)?.toInt() ?: 60,
                workoutFrequency = (map["workoutFrequency"] as? Long)?.toInt() ?: 3,
                preferredExerciseTypes = (map["preferredExerciseTypes"] as? List<String>)?.mapNotNull {
                    try {
                        ExerciseType.valueOf(it)
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                } ?: listOf(),
                streakCount = (map["streakCount"] as? Long)?.toInt() ?: 0,
                lastWorkoutDate = (map["lastWorkoutDate"] as? Long)?.let { Date(it) }
            )
        }
    }
}
