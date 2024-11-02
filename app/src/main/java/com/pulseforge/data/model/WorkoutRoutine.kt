
package com.pulseforge.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "workout_routines")
data class WorkoutRoutine(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val exercises: List<Exercise>,
    val createdAt: Date = Date(),
    var scheduledDate: Date? = null,
    var notificationTime: Long? = null
)

data class Exercise(
    val name: String,
    val type: ExerciseType,
    val sets: Int,
    val reps: Int,
    val weight: Float? = null,
    val duration: Int? = null, // in seconds
    val distance: Float? = null, // in kilometers
    val performanceRecords: List<PerformanceRecord> = emptyList()
)

enum class ExerciseType {
    STRENGTH,
    CARDIO,
    FLEXIBILITY
}

data class PerformanceRecord(
    val date: Date,
    val completedSets: Int,
    val completedReps: Int,
    val weight: Float? = null,
    val duration: Int? = null, // in seconds
    val distance: Float? = null // in kilometers
)
