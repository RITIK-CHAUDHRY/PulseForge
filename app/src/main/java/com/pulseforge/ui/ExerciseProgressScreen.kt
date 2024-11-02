
package com.pulseforge.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.pulseforge.data.model.Exercise
import com.pulseforge.data.model.PerformanceRecord
import com.pulseforge.viewmodel.WorkoutViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExerciseProgressScreen(
    workoutViewModel: WorkoutViewModel,
    routineId: Int,
    exerciseIndex: Int,
    onBack: () -> Unit
) {
    val routine by workoutViewModel.workoutRoutines.collectAsState()
    val exercise = routine.find { it.id == routineId }?.exercises?.getOrNull(exerciseIndex)
    var performanceRecords by remember { mutableStateOf<List<PerformanceRecord>?>(null) }

    LaunchedEffect(routineId, exerciseIndex) {
        performanceRecords = workoutViewModel.getExercisePerformanceRecords(routineId, exerciseIndex)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(exercise?.name ?: "Exercise Progress") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            exercise?.let { 
                ExerciseDetails(it)
                Spacer(modifier = Modifier.height(16.dp))
                performanceRecords?.let { records ->
                    ProgressChart(records)
                    Spacer(modifier = Modifier.height(16.dp))
                    PerformanceStatistics(records)
                    Spacer(modifier = Modifier.height(16.dp))
                    PerformanceRecordsList(records)
                } ?: run {
                    Text("No performance records yet")
                }
            } ?: run {
                Text("Exercise not found")
            }
        }
    }
}

@Composable
fun ExerciseDetails(exercise: Exercise) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = exercise.name, style = MaterialTheme.typography.h6)
            Text(text = "Type: ${exercise.type}", style = MaterialTheme.typography.body1)
            Text(text = "Sets: ${exercise.sets}", style = MaterialTheme.typography.body1)
            Text(text = "Reps: ${exercise.reps}", style = MaterialTheme.typography.body1)
            exercise.weight?.let { Text(text = "Weight: $it kg", style = MaterialTheme.typography.body1) }
            exercise.duration?.let { Text(text = "Duration: $it seconds", style = MaterialTheme.typography.body1) }
        }
    }
}

@Composable
fun ProgressChart(records: List<PerformanceRecord>) {
    val entries = records.mapIndexed { index, record ->
        Entry(index.toFloat(), record.weight ?: record.completedReps.toFloat())
    }

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(true)

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    valueFormatter = IndexAxisValueFormatter(records.map { 
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
        update = { chart ->
            val dataSet = LineDataSet(entries, "Progress").apply {
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

            chart.data = LineData(dataSet)
            chart.invalidate()
        }
    )
}

@Composable
fun PerformanceStatistics(records: List<PerformanceRecord>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Performance Statistics", style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(8.dp))
            val avgWeight = records.mapNotNull { it.weight }.average()
            val maxWeight = records.mapNotNull { it.weight }.maxOrNull()
            val avgReps = records.map { it.completedReps }.average()
            val maxReps = records.map { it.completedReps }.maxOrNull()

            Text("Average Weight: ${String.format("%.2f", avgWeight)} kg")
            Text("Max Weight: ${maxWeight?.let { String.format("%.2f", it) } ?: "N/A"} kg")
            Text("Average Reps: ${String.format("%.1f", avgReps)}")
            Text("Max Reps: $maxReps")
        }
    }
}

@Composable
fun PerformanceRecordsList(records: List<PerformanceRecord>) {
    Text("Performance History", style = MaterialTheme.typography.h6)
    Spacer(modifier = Modifier.height(8.dp))
    LazyColumn {
        items(records.sortedByDescending { it.date }) { record ->
            PerformanceRecordItem(record)
        }
    }
}

@Composable
fun PerformanceRecordItem(record: PerformanceRecord) {
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = dateFormatter.format(record.date), style = MaterialTheme.typography.subtitle1)
                Text(text = "Sets: ${record.completedSets}, Reps: ${record.completedReps}", style = MaterialTheme.typography.body2)
                record.weight?.let { Text(text = "Weight: $it kg", style = MaterialTheme.typography.body2) }
                record.duration?.let { Text(text = "Duration: $it seconds", style = MaterialTheme.typography.body2) }
                record.distance?.let { Text(text = "Distance: $it km", style = MaterialTheme.typography.body2) }
            }
        }
    }
}
