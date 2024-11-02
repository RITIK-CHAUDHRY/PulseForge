
package com.pulseforge.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pulseforge.data.model.WorkoutRoutine
import com.pulseforge.viewmodel.SortOption
import com.pulseforge.viewmodel.WorkoutViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WorkoutRoutineListScreen(
    workoutViewModel: WorkoutViewModel,
    onCreateRoutine: () -> Unit,
    onEditRoutine: (WorkoutRoutine) -> Unit,
    onViewExerciseProgress: (Int, Int) -> Unit
) {
    val workoutRoutines by workoutViewModel.workoutRoutines.collectAsState()
    val currentSortOption by workoutViewModel.sortOption.collectAsState()
    var showDeleteConfirmation by remember { mutableStateOf<WorkoutRoutine?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showSortMenu by remember { mutableStateOf(false) }

    val filteredRoutines = remember(workoutRoutines, searchQuery) {
        if (searchQuery.isBlank()) {
            workoutRoutines
        } else {
            workoutRoutines.filter { routine ->
                routine.name.contains(searchQuery, ignoreCase = true) ||
                routine.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout Routines") },
                actions = {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Default.Sort, contentDescription = "Sort")
                    }
                    IconButton(onClick = onCreateRoutine) {
                        Icon(Icons.Default.Add, contentDescription = "Add Routine")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it }
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredRoutines) { routine ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        WorkoutRoutineItem(
                            routine = routine,
                            onEditClick = { onEditRoutine(routine) },
                            onDeleteClick = { showDeleteConfirmation = routine },
                            onExerciseClick = { exerciseIndex ->
                                onViewExerciseProgress(routine.id, exerciseIndex)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showSortMenu) {
        SortOptionMenu(
            currentSortOption = currentSortOption,
            onSortOptionSelected = { option ->
                workoutViewModel.setSortOption(option)
                showSortMenu = false
            },
            onDismiss = { showSortMenu = false }
        )
    }

    showDeleteConfirmation?.let { routine ->
        DeleteConfirmationDialog(
            routineName = routine.name,
            onConfirm = {
                workoutViewModel.deleteWorkoutRoutine(routine)
                showDeleteConfirmation = null
            },
            onDismiss = { showDeleteConfirmation = null }
        )
    }
}

@Composable
fun WorkoutRoutineItem(
    routine: WorkoutRoutine,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onExerciseClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = routine.name, style = MaterialTheme.typography.h6)
            Text(text = routine.description, style = MaterialTheme.typography.body2)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Exercises:", style = MaterialTheme.typography.subtitle1)
            routine.exercises.forEachIndexed { index, exercise ->
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier
                        .clickable { onExerciseClick(index) }
                        .padding(vertical = 4.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEditClick) {
                    Text("Edit")
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Routine")
                }
            }
        }
    }
}

// ... (rest of the file remains unchanged)
