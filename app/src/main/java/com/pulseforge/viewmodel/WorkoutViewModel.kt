
package com.pulseforge.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pulseforge.data.model.WorkoutRoutine
import com.pulseforge.data.model.PerformanceRecord
import com.pulseforge.data.repository.WorkoutRepository
import com.pulseforge.service.NotificationService
import com.pulseforge.data.RecommendationEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date

class WorkoutViewModel(
    application: Application,
    private val repository: WorkoutRepository,
    private val recommendationEngine: RecommendationEngine
) : AndroidViewModel(application) {
    private val notificationService = NotificationService(application)

    private val _workoutRoutines = MutableStateFlow<List<WorkoutRoutine>>(emptyList())
    val workoutRoutines: StateFlow<List<WorkoutRoutine>> = _workoutRoutines

    private val _recommendedWorkouts = MutableStateFlow<List<WorkoutRoutine>>(emptyList())
    val recommendedWorkouts: StateFlow<List<WorkoutRoutine>> = _recommendedWorkouts

    private val _sortOption = MutableStateFlow(SortOption.NAME)
    val sortOption: StateFlow<SortOption> = _sortOption

    private var streakCount = 0

    init {
        viewModelScope.launch {
            repository.allWorkoutRoutines.collect { routines ->
                _workoutRoutines.value = sortRoutines(routines, _sortOption.value)
            }
        }
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
        _workoutRoutines.value = sortRoutines(_workoutRoutines.value, option)
    }

    private fun sortRoutines(routines: List<WorkoutRoutine>, option: SortOption): List<WorkoutRoutine> {
        return when (option) {
            SortOption.NAME -> routines.sortedBy { it.name }
            SortOption.DATE -> routines.sortedByDescending { it.createdAt }
            SortOption.EXERCISES -> routines.sortedByDescending { it.exercises.size }
        }
    }

    fun insertWorkoutRoutine(workoutRoutine: WorkoutRoutine) {
        viewModelScope.launch {
            val id = repository.insertWorkoutRoutine(workoutRoutine)
            scheduleWorkoutReminder(workoutRoutine.copy(id = id.toInt()))
        }
    }

    fun updateWorkoutRoutine(workoutRoutine: WorkoutRoutine) {
        viewModelScope.launch {
            repository.updateWorkoutRoutine(workoutRoutine)
            scheduleWorkoutReminder(workoutRoutine)
        }
    }

    fun deleteWorkoutRoutine(workoutRoutine: WorkoutRoutine) {
        viewModelScope.launch {
            repository.deleteWorkoutRoutine(workoutRoutine)
            notificationService.cancelWorkoutReminder(workoutRoutine.id)
        }
    }

    fun scheduleWorkout(workoutRoutine: WorkoutRoutine, date: Date) {
        viewModelScope.launch {
            val updatedRoutine = workoutRoutine.copy(scheduledDate = date)
            repository.updateWorkoutRoutine(updatedRoutine)
            scheduleWorkoutReminder(updatedRoutine)
        }
    }

    private fun scheduleWorkoutReminder(workoutRoutine: WorkoutRoutine) {
        workoutRoutine.scheduledDate?.let { scheduledDate ->
            notificationService.scheduleWorkoutReminder(
                workoutRoutine.id,
                workoutRoutine.name,
                scheduledDate.time
            )
        }
    }

    fun addPerformanceRecord(routineId: Int, exerciseIndex: Int, performanceRecord: PerformanceRecord) {
        viewModelScope.launch {
            repository.addPerformanceRecord(routineId, exerciseIndex, performanceRecord)
            updateStreak()
        }
    }

    suspend fun getExercisePerformanceRecords(routineId: Int, exerciseIndex: Int): List<PerformanceRecord>? {
        return repository.getExercisePerformanceRecords(routineId, exerciseIndex)
    }

    fun generateRecommendations(userProfile: UserProfile) {
        viewModelScope.launch {
            val workoutHistory = repository.getAllWorkoutRoutines()
            val performanceHistory = repository.getAllPerformanceRecords()
            val recommendations = recommendationEngine.generateRecommendations(
                userProfile,
                workoutHistory,
                performanceHistory
            )
            _recommendedWorkouts.value = recommendations
        }
    }

    private fun updateStreak() {
        streakCount++
        notificationService.scheduleStreakReminder(streakCount)
    }

    fun resetStreak() {
        streakCount = 0
        notificationService.cancelStreakReminder()
    }

    fun getWorkoutRoutineById(workoutId: Int): WorkoutRoutine? {
        return _workoutRoutines.value.find { it.id == workoutId }
    }

    class WorkoutViewModelFactory(
        private val application: Application,
        private val repository: WorkoutRepository,
        private val recommendationEngine: RecommendationEngine
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WorkoutViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return WorkoutViewModel(application, repository, recommendationEngine) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

enum class SortOption {
    NAME, DATE, EXERCISES
}
