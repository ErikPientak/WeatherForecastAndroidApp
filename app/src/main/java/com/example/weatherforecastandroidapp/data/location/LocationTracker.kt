package com.example.weatherforecastandroidapp.data.location

import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LocationTracker @Inject constructor(
    private val fusedLocationProviderClient: FusedLocationProviderClient,
) {
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            .build()
        return fusedLocationProviderClient.getCurrentLocation(request, null).await()
    }
}
