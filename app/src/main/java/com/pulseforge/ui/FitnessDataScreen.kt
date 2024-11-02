
package com.pulseforge.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pulseforge.service.GoogleFitService
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun FitnessDataScreen(googleFitService: GoogleFitService) {
    var stepCount by remember { mutableStateOf(0) }
    var distance by remember { mutableStateOf(0f) }
    var caloriesBurned by remember { mutableStateOf(0f) }
    var averageHeartRate by remember { mutableStateOf(0f) }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (googleFitService.hasPermissions()) {
            fetchFitnessData(googleFitService) { steps, dist, calories, heartRate ->
                stepCount = steps
                distance = dist
                caloriesBurned = calories
                averageHeartRate = heartRate
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Fitness Data", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            FitnessDataItem("Steps", stepCount.toString())
            FitnessDataItem("Distance", String.format("%.2f km", distance / 1000))
            FitnessDataItem("Calories Burned", String.format("%.1f kcal", caloriesBurned))
            FitnessDataItem("Average Heart Rate", String.format("%.1f bpm", averageHeartRate))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                coroutineScope.launch {
                    isLoading = true
                    fetchFitnessData(googleFitService) { steps, dist, calories, heartRate ->
                        stepCount = steps
                        distance = dist
                        caloriesBurned = calories
                        averageHeartRate = heartRate
                        isLoading = false
                    }
                }
            }
        ) {
            Text("Refresh Data")
        }
    }
}

@Composable
fun FitnessDataItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.body1)
        Text(value, style = MaterialTheme.typography.body1)
    }
}

private suspend fun fetchFitnessData(
    googleFitService: GoogleFitService,
    onDataFetched: (Int, Float, Float, Float) -> Unit
) {
    val endTime = System.currentTimeMillis()
    val startTime = endTime - 24 * 60 * 60 * 1000 // 24 hours ago

    val stepCount = googleFitService.getStepCount(startTime, endTime)
    val distance = googleFitService.getDistance(startTime, endTime)
    val caloriesBurned = googleFitService.getCaloriesBurned(startTime, endTime)
    val averageHeartRate = googleFitService.getAverageHeartRate(startTime, endTime)

    onDataFetched(stepCount, distance, caloriesBurned, averageHeartRate)
}
