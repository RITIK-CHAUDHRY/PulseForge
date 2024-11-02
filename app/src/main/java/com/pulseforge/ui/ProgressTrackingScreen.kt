
package com.pulseforge.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pulseforge.data.model.Exercise
import com.pulseforge.data.model.PerformanceRecord
import com.pulseforge.data.model.WorkoutRoutine
import com.pulseforge.viewmodel.WorkoutViewModel
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProgressTrackingScreen(workoutViewModel: WorkoutViewModel) {
    val workoutRoutines by workoutViewModel.workoutRoutines.collectAsState()
    var selectedRoutine by remember { mutableStateOf<WorkoutRoutine?>(null) }
    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Progress Tracking", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))
        
        // Routine selection
        RoutineSelector(workoutRoutines, onRoutineSelected = { selectedRoutine = it })
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Exercise selection (only show if a routine is selected)
        selectedRoutine?.let { routine ->
            ExerciseSelector(routine.exercises, onExerciseSelected = { selectedExercise = it })
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Progress chart (only show if both routine and exercise are selected)
        if (selectedRoutine != null && selectedExercise != null) {
            ProgressChart(selectedRoutine!!, selectedExercise!!)
        }
    }
}

@Composable
fun RoutineSelector(routines: List<WorkoutRoutine>, onRoutineSelected: (WorkoutRoutine) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedRoutine by remember { mutableStateOf<WorkoutRoutine?>(null) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = selectedRoutine?.name ?: "Select a routine",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            routines.forEach { routine ->
                DropdownMenuItem(
                    onClick = {
                        selectedRoutine = routine
                        expanded = false
                        onRoutineSelected(routine)
                    }
                ) {
                    Text(text = routine.name)
                }
            }
        }
    }
}

@Composable
fun ExerciseSelector(exercises: List<Exercise>, onExerciseSelected: (Exercise) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = selectedExercise?.name ?: "Select an exercise",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            exercises.forEach { exercise ->
                DropdownMenuItem(
                    onClick = {
                        selectedExercise = exercise
                        expanded = false
                        onExerciseSelected(exercise)
                    }
                ) {
                    Text(text = exercise.name)
                }
            }
        }
    }
}

@Composable
fun ProgressChart(routine: WorkoutRoutine, exercise: Exercise) {
    val performanceRecords = exercise.performanceRecords.sortedBy { it.date }
    
    if (performanceRecords.isEmpty()) {
        Text("No performance records available for this exercise.")
        return
    }

    val entries = performanceRecords.mapIndexed { index, record ->
        Entry(index.toFloat(), record.weight ?: 0f)
    }

    val dataSet = LineDataSet(entries, "Weight Progress").apply {
        color = Color.Blue.toArgb()
        setCircleColor(Color.Blue.toArgb())
        lineWidth = 2f
        circleRadius = 4f
        setDrawCircleHole(false)
        valueTextSize = 10f
        setDrawFilled(true)
        fillColor = Color.Blue.toArgb()
        fillAlpha = 30
    }

    val lineData = LineData(dataSet)

    AndroidView(
        factory = { context ->
            com.github.mikephil.charting.charts.LineChart(context).apply {
                data = lineData
                description.isEnabled = false
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(true)

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    valueFormatter = IndexAxisValueFormatter(performanceRecords.map { 
                        SimpleDateFormat("MM/dd", Locale.getDefault()).format(it.date)
                    })
                    labelRotationAngle = -45f
                }

                axisLeft.apply {
                    axisMinimum = 0f
                    setDrawGridLines(false)
                }

                axisRight.isEnabled = false

                legend.isEnabled = false
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    )
}
