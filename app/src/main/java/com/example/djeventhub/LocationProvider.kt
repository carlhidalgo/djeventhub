package com.example.djeventhub

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Location provider using Android's LocationManager
class LocationProvider(private val context: Context) {

    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    // Suspends and returns the last known location if permissions are granted, otherwise returns null
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (!fine && !coarse) {
            // Permissions not granted
            return null
        }

        return try {
            // Try last known locations from providers
            val gpsLocation = try { locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) } catch (e: Exception) { null }
            if (gpsLocation != null) return gpsLocation

            val networkLocation = try { locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) } catch (e: Exception) { null }
            if (networkLocation != null) return networkLocation

            // If no last known, request a single update and resume when received
            suspendCancellableCoroutine { cont ->
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        if (!cont.isCompleted) cont.resume(location)
                        try { locationManager.removeUpdates(this) } catch (_: Exception) {}
                    }

                    // Deprecated callbacks removed
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }

                try {
                    val provider = when {
                        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                        else -> null
                    }

                    if (provider == null) {
                        cont.resume(null)
                        return@suspendCancellableCoroutine
                    }

                    locationManager.requestLocationUpdates(provider, 0L, 0f, listener)

                    cont.invokeOnCancellation {
                        try { locationManager.removeUpdates(listener) } catch (_: Exception) {}
                    }
                } catch (e: Exception) {
                    cont.resumeWithException(e)
                }
            }

        } catch (e: Exception) {
            null
        }
    }
}
