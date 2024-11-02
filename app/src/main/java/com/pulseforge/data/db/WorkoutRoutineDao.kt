
package com.pulseforge.data.db

import androidx.room.*
import com.pulseforge.data.model.WorkoutRoutine
import com.pulseforge.data.model.Exercise
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutRoutineDao {
    @Query("SELECT * FROM workout_routines")
    fun getAllWorkoutRoutines(): Flow<List<WorkoutRoutine>>

    @Query("SELECT * FROM workout_routines WHERE id = :id")
    suspend fun getWorkoutRoutineById(id: Int): WorkoutRoutine?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutRoutine(workoutRoutine: WorkoutRoutine): Long

    @Update
    suspend fun updateWorkoutRoutine(workoutRoutine: WorkoutRoutine)

    @Delete
    suspend fun deleteWorkoutRoutine(workoutRoutine: WorkoutRoutine)

    @Query("SELECT * FROM workout_routines WHERE scheduledDate IS NOT NULL")
    fun getScheduledWorkoutRoutines(): Flow<List<WorkoutRoutine>>

    @Query("UPDATE workout_routines SET exercises = :exercises WHERE id = :routineId")
    suspend fun updateExercises(routineId: Int, exercises: List<Exercise>)
}
