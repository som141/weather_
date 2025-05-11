package com.example.weather

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.widget.Toast
import com.google.android.gms.location.*

object LocationUtil {

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(context: Context, callback: (Location?) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        val locationRequest = LocationRequest.create().apply {
            priority = Priority.PRIORITY_HIGH_ACCURACY
            interval = 1000
            fastestInterval = 500
            numUpdates = 1
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                callback(result.lastLocation)
                fusedLocationClient.removeLocationUpdates(this)
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }
}
