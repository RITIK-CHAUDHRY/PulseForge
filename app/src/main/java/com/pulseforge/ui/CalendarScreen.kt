
package com.pulseforge.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pulseforge.data.model.WorkoutRoutine
import com.pulseforge.viewmodel.WorkoutViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarScreen(
    workoutViewModel: WorkoutViewModel,
    onEditRoutine: (WorkoutRoutine) -> Unit
) {
    val workoutRoutines by workoutViewModel.workoutRoutines.collectAsState()
    var selectedDate by remember { mutableStateOf(Date()) }

    Column(modifier = Modifier.fillMaxSize()) {
        DatePicker(
            selectedDate = selectedDate,
            onDateSelected = { selectedDate = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Scheduled Workouts",
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
        ) {
            val scheduledWorkouts = workoutRoutines.filter { 
                it.scheduledDate?.let { date ->
                    isSameDay(date, selectedDate)
                } ?: false
            }

            items(scheduledWorkouts) { routine ->
                WorkoutRoutineItem(
                    routine = routine,
                    onEditClick = { onEditRoutine(routine) },
                    onDeleteClick = { 
                        workoutViewModel.updateWorkoutRoutine(routine.copy(scheduledDate = null))
                    }
                )
            }

            item {
                if (scheduledWorkouts.isEmpty()) {
                    Text(
                        text = "No workouts scheduled for this day",
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DatePicker(
    selectedDate: Date,
    onDateSelected: (Date) -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dateFormatter.format(selectedDate),
            style = MaterialTheme.typography.h6,
            modifier = Modifier.weight(1f)
        )
        Button(
            onClick = {
                val calendar = Calendar.getInstance()
                calendar.time = selectedDate
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                onDateSelected(calendar.time)
            }
        ) {
            Text("Previous")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = {
                val calendar = Calendar.getInstance()
                calendar.time = selectedDate
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                onDateSelected(calendar.time)
            }
        ) {
            Text("Next")
        }
    }
}

fun isSameDay(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = date1 }
    val cal2 = Calendar.getInstance().apply { time = date2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
           cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
}
