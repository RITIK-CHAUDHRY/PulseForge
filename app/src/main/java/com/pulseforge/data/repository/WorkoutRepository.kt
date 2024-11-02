
package com.pulseforge.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.pulseforge.data.db.WorkoutRoutineDao
import com.pulseforge.data.model.WorkoutRoutine
import com.pulseforge.data.model.PerformanceRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class WorkoutRepository(
    private val workoutRoutineDao: WorkoutRoutineDao,
    private val firestore: FirebaseFirestore
) {
    private val workoutRoutinesCollection = firestore.collection("workoutRoutines")

    val allWorkoutRoutines: Flow<List<WorkoutRoutine>> = workoutRoutineDao.getAllWorkoutRoutines()
        .map { localRoutines ->
            syncWithFirestore(localRoutines)
            localRoutines
        }

    suspend fun getWorkoutRoutineById(id: Int): WorkoutRoutine? {
        return workoutRoutineDao.getWorkoutRoutineById(id)
    }

    suspend fun insertWorkoutRoutine(workoutRoutine: WorkoutRoutine): Long {
        val id = workoutRoutineDao.insertWorkoutRoutine(workoutRoutine)
        syncWorkoutRoutineToFirestore(workoutRoutine.copy(id = id.toInt()))
        return id
    }

    suspend fun updateWorkoutRoutine(workoutRoutine: WorkoutRoutine) {
        workoutRoutineDao.updateWorkoutRoutine(workoutRoutine)
        syncWorkoutRoutineToFirestore(workoutRoutine)
    }

    suspend fun deleteWorkoutRoutine(workoutRoutine: WorkoutRoutine) {
        workoutRoutineDao.deleteWorkoutRoutine(workoutRoutine)
        deleteWorkoutRoutineFromFirestore(workoutRoutine)
    }

    suspend fun addPerformanceRecord(routineId: Int, exerciseIndex: Int, performanceRecord: PerformanceRecord) {
        val routine = workoutRoutineDao.getWorkoutRoutineById(routineId)
        routine?.let {
            val updatedExercises = it.exercises.toMutableList()
            val exercise = updatedExercises[exerciseIndex]
            val updatedExercise = exercise.copy(
                performanceRecords = exercise.performanceRecords + performanceRecord
            )
            updatedExercises[exerciseIndex] = updatedExercise
            val updatedRoutine = it.copy(exercises = updatedExercises)
            workoutRoutineDao.updateWorkoutRoutine(updatedRoutine)
            syncWorkoutRoutineToFirestore(updatedRoutine)
        }
    }

    suspend fun getExercisePerformanceRecords(routineId: Int, exerciseIndex: Int): List<PerformanceRecord>? {
        val routine = workoutRoutineDao.getWorkoutRoutineById(routineId)
        return routine?.exercises?.getOrNull(exerciseIndex)?.performanceRecords
    }

    private suspend fun syncWithFirestore(localRoutines: List<WorkoutRoutine>) {
        val firestoreRoutines = workoutRoutinesCollection.get().await().documents.mapNotNull { doc ->
            doc.toObject(WorkoutRoutine::class.java)?.copy(id = doc.id.toInt())
        }

        val localIds = localRoutines.map { it.id }.toSet()
        val firestoreIds = firestoreRoutines.map { it.id }.toSet()

        // Add new routines from Firestore to local database
        firestoreRoutines.filter { it.id !in localIds }.forEach { workoutRoutineDao.insertWorkoutRoutine(it) }

        // Update existing routines in local database
        firestoreRoutines.filter { it.id in localIds }.forEach { workoutRoutineDao.updateWorkoutRoutine(it) }

        // Delete routines from local database that are not in Firestore
        localRoutines.filter { it.id !in firestoreIds }.forEach { workoutRoutineDao.deleteWorkoutRoutine(it) }
    }

    private suspend fun syncWorkoutRoutineToFirestore(workoutRoutine: WorkoutRoutine) {
        workoutRoutinesCollection.document(workoutRoutine.id.toString()).set(workoutRoutine).await()
    }

    private suspend fun deleteWorkoutRoutineFromFirestore(workoutRoutine: WorkoutRoutine) {
        workoutRoutinesCollection.document(workoutRoutine.id.toString()).delete().await()
    }
}
