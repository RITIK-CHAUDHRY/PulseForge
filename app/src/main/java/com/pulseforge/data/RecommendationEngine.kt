package com.pulseforge.data
import com.pulseforge.data.model.*
import kotlin.random.Random
import java.util.Date

class RecommendationEngine {
    fun generateRecommendations(
        userProfile: UserProfile,
        workoutHistory: List<WorkoutRoutine>,
        performanceHistory: Map<Int, List<PerformanceRecord>>
    ): List<WorkoutRoutine> {
        val recommendations = mutableListOf<WorkoutRoutine>()

        // Generate recommendations based on user's fitness goal
        when (userProfile.fitnessGoal) {
            "Weight Loss" -> recommendations.addAll(generateWeightLossWorkouts())
            "Muscle Gain" -> recommendations.addAll(generateMuscleGainWorkouts())
            "Endurance" -> recommendations.addAll(generateEnduranceWorkouts())
            else -> recommendations.addAll(generateBalancedWorkouts())
        }

        // Adjust recommendations based on user's performance history
        adjustRecommendationsBasedOnPerformance(recommendations, performanceHistory)

        // Consider user's preferred workout duration and frequency
        recommendations.removeAll { it.estimatedDuration() > userProfile.preferredWorkoutDuration }

        // Prioritize exercises based on user's preferred exercise types
        recommendations.forEach { routine ->
            routine.exercises = routine.exercises.sortedByDescending { exercise ->
                if (exercise.type in userProfile.preferredExerciseTypes) 1 else 0
            }
        }

        // Ensure we have at least 3 recommendations
        while (recommendations.size < 3) {
            recommendations.add(generateRandomWorkout())
        }

        return recommendations.take(3)
    }

    private fun adjustRecommendationsBasedOnPerformance(recommendations: MutableList<WorkoutRoutine>, performanceHistory: Map<Int, List<PerformanceRecord>>) {
        recommendations.forEach { routine ->
            routine.exercises.forEach { exercise ->
                val exercisePerformance = performanceHistory.values.flatten().filter { it.exerciseName == exercise.name }
                if (exercisePerformance.isNotEmpty()) {
                    val averagePerformance = exercisePerformance.map { it.getPerformanceScore() }.average()
                    when {
                        averagePerformance > 0.8 -> increaseIntensity(exercise)
                        averagePerformance < 0.5 -> decreaseIntensity(exercise)
                    }
                }
            }
        }
    }

    private fun increaseIntensity(exercise: Exercise) {
        when (exercise.type) {
            ExerciseType.STRENGTH -> {
                exercise.weight = (exercise.weight ?: 0f) * 1.1f
                exercise.reps += 2
            }
            ExerciseType.CARDIO -> {
                exercise.duration = (exercise.duration ?: 0) + 60
                exercise.distance = (exercise.distance ?: 0f) * 1.1f
            }
            ExerciseType.FLEXIBILITY -> {
                exercise.duration = (exercise.duration ?: 0) + 30
            }
        }
    }

    private fun decreaseIntensity(exercise: Exercise) {
        when (exercise.type) {
            ExerciseType.STRENGTH -> {
                exercise.weight = (exercise.weight ?: 0f) * 0.9f
                exercise.reps -= 2
            }
            ExerciseType.CARDIO -> {
                exercise.duration = (exercise.duration ?: 0) - 60
                exercise.distance = (exercise.distance ?: 0f) * 0.9f
            }
            ExerciseType.FLEXIBILITY -> {
                exercise.duration = (exercise.duration ?: 0) - 30
            }
        }
    }

    // ... (keep the existing methods for generating specific workout types)

    private fun WorkoutRoutine.estimatedDuration(): Int {
        return exercises.sumOf { it.duration ?: 0 }
    }
}

fun PerformanceRecord.getPerformanceScore(): Double {
    return when {
        weight != null -> completedReps.toDouble() / reps.toDouble()
        distance != null -> distance / (duration / 60.0)
        else -> completedReps.toDouble() / reps.toDouble()
    }
}
