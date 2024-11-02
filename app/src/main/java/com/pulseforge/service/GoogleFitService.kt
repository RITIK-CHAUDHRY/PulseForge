
package com.pulseforge.service

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import java.util.Date

class GoogleFitService(private val context: Context) {
    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
        .build()

    fun hasPermissions(): Boolean {
        return GoogleSignIn.hasPermissions(getGoogleAccount(), fitnessOptions)
    }

    fun requestPermissions(activity: android.app.Activity, requestCode: Int) {
        GoogleSignIn.requestPermissions(
            activity,
            requestCode,
            getGoogleAccount(),
            fitnessOptions)
    }

    private fun getGoogleAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getAccountForExtension(context, fitnessOptions)
    }

    suspend fun getStepCount(startTime: Long, endTime: Long): Int {
        return withContext(Dispatchers.IO) {
            try {
                val response = Fitness.getHistoryClient(context, getGoogleAccount()!!)
                    .readData(
                        DataReadRequest.Builder()
                            .read(DataType.TYPE_STEP_COUNT_DELTA)
                            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                            .build()
                    )
                    .await()

                response.buckets
                    .flatMap { it.dataSets }
                    .flatMap { it.dataPoints }
                    .sumOf { it.getValue(DataType.FIELD_STEPS).asInt() }
            } catch (e: Exception) {
                Log.e("GoogleFitService", "Error getting step count", e)
                0
            }
        }
    }

    suspend fun getDistance(startTime: Long, endTime: Long): Float {
        return withContext(Dispatchers.IO) {
            try {
                val response = Fitness.getHistoryClient(context, getGoogleAccount()!!)
                    .readData(
                        DataReadRequest.Builder()
                            .read(DataType.TYPE_DISTANCE_DELTA)
                            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                            .build()
                    )
                    .await()

                response.buckets
                    .flatMap { it.dataSets }
                    .flatMap { it.dataPoints }
                    .sumOf { it.getValue(DataType.FIELD_DISTANCE).asFloat().toDouble() }
                    .toFloat()
            } catch (e: Exception) {
                Log.e("GoogleFitService", "Error getting distance", e)
                0f
            }
        }
    }

    suspend fun getCaloriesBurned(startTime: Long, endTime: Long): Float {
        return withContext(Dispatchers.IO) {
            try {
                val response = Fitness.getHistoryClient(context, getGoogleAccount()!!)
                    .readData(
                        DataReadRequest.Builder()
                            .read(DataType.TYPE_CALORIES_EXPENDED)
                            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                            .build()
                    )
                    .await()

                response.buckets
                    .flatMap { it.dataSets }
                    .flatMap { it.dataPoints }
                    .sumOf { it.getValue(DataType.FIELD_CALORIES).asFloat().toDouble() }
                    .toFloat()
            } catch (e: Exception) {
                Log.e("GoogleFitService", "Error getting calories burned", e)
                0f
            }
        }
    }

    suspend fun getAverageHeartRate(startTime: Long, endTime: Long): Float {
        return withContext(Dispatchers.IO) {
            try {
                val response = Fitness.getHistoryClient(context, getGoogleAccount()!!)
                    .readData(
                        DataReadRequest.Builder()
                            .read(DataType.TYPE_HEART_RATE_BPM)
                            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                            .build()
                    )
                    .await()

                val heartRates = response.buckets
                    .flatMap { it.dataSets }
                    .flatMap { it.dataPoints }
                    .map { it.getValue(DataType.FIELD_AVERAGE).asFloat() }

                if (heartRates.isNotEmpty()) {
                    heartRates.average().toFloat()
                } else {
                    0f
                }
            } catch (e: Exception) {
                Log.e("GoogleFitService", "Error getting average heart rate", e)
                0f
            }
        }
    }
}
