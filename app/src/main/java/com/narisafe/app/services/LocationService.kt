package com.narisafe.app.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.firestore.FirebaseFirestore

object LocationService {

    private lateinit var context: Context

    private lateinit var fusedLocationClient:
            FusedLocationProviderClient

    private val db =
        FirebaseFirestore.getInstance()

    private var locationCallback:
            LocationCallback? = null

    private var trackingActive = false

    fun initialize(context: Context) {

        if (!::context.isInitialized) {

            this.context =
                context.applicationContext

            fusedLocationClient =
                LocationServices
                    .getFusedLocationProviderClient(
                        this.context
                    )
        }
    }

    fun startLiveTracking(
        userId: String,
        sosId: String
    ) {

        // Prevent duplicate tracking
        if (trackingActive) {
            return
        }

        if (!::context.isInitialized) {
            return
        }

        val fineLocationGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (
            !fineLocationGranted &&
            !coarseLocationGranted
        ) {
            return
        }

        trackingActive = true

        val locationRequest =
            LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                5000L
            )
                .setMinUpdateIntervalMillis(3000L)
                .build()

        locationCallback =
            object : LocationCallback() {

                override fun onLocationResult(
                    locationResult: LocationResult
                ) {

                    if (!trackingActive) {
                        return
                    }

                    for (
                    location in
                    locationResult.locations
                    ) {

                        if (!trackingActive) {
                            return
                        }

                        val liveLocation =
                            hashMapOf(
                                "userId" to userId,
                                "sosId" to sosId,
                                "latitude" to location.latitude,
                                "longitude" to location.longitude,
                                "timestamp" to
                                        System.currentTimeMillis()
                            )

                        db.collection("liveLocations")
                            .add(liveLocation)
                    }
                }
            }

        fusedLocationClient
            .requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                context.mainLooper
            )
    }

    fun stopLiveTracking() {

        // Stop Firebase writes immediately
        trackingActive = false

        locationCallback?.let { callback ->

            fusedLocationClient
                .removeLocationUpdates(callback)
        }

        locationCallback = null
    }
}