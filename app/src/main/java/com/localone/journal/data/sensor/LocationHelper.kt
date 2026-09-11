package com.localone.journal.data.sensor

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import com.localone.journal.domain.model.EntryLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

object LocationHelper {

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): EntryLocation? = withContext(Dispatchers.IO) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return@withContext null

        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )

        var bestLocation: Location? = null
        for (provider in providers) {
            try {
                if (locationManager.isProviderEnabled(provider)) {
                    val loc = locationManager.getLastKnownLocation(provider)
                    if (loc != null) {
                        if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                            bestLocation = loc
                        }
                    }
                }
            } catch (e: SecurityException) {
                // 忽略未授权异常
            } catch (e: Exception) {
                // 忽略
            }
        }

        val location = bestLocation ?: return@withContext null
        geocodeLocation(context, location.latitude, location.longitude, location.altitude)
    }

    suspend fun geocodeLocation(
        context: Context,
        lat: Double,
        lng: Double,
        altitude: Double? = null
    ): EntryLocation = withContext(Dispatchers.IO) {
        var placeName: String? = null
        var locality: String? = null
        var adminArea: String? = null
        var country: String? = null

        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    placeName = addr.featureName ?: addr.thoroughfare ?: addr.subLocality
                    locality = addr.locality ?: addr.subAdminArea
                    adminArea = addr.adminArea
                    country = addr.countryName
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    placeName = addr.featureName ?: addr.thoroughfare ?: addr.subLocality
                    locality = addr.locality ?: addr.subAdminArea
                    adminArea = addr.adminArea
                    country = addr.countryName
                }
            }
        } catch (e: Exception) {
            // Geocoder 偶尔网络异常，容错处理
        }

        EntryLocation(
            latitude = lat,
            longitude = lng,
            altitude = altitude,
            placeName = placeName ?: "当前位置",
            localityName = locality,
            administrativeArea = adminArea,
            country = country
        )
    }
}
