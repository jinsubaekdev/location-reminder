package com.udacity.project4.utils

import android.app.Activity
import android.content.IntentSender
import android.util.Log
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest


const val REQUEST_TURN_DEVICE_LOCATION_ON = 1
private const val TAG = "LocationUtils"

fun checkDeviceLocationSettings(activity: Activity, completed: () -> Unit = {}) {

    val locationRequest = LocationRequest.create().apply {
        priority = LocationRequest.PRIORITY_LOW_POWER
    }
    val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
    val settingsClient = LocationServices.getSettingsClient(activity)
    val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())
    locationSettingsResponseTask.addOnFailureListener { exception ->
        if(exception is ResolvableApiException) {
            try {
                exception.startResolutionForResult(activity, REQUEST_TURN_DEVICE_LOCATION_ON)
            } catch (sendEx: IntentSender.SendIntentException) {
                Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
            }
        } else {
            Log.d(TAG, "Error getting location settings resolution")
        }
    }
    locationSettingsResponseTask.addOnCompleteListener {
        if(it.isSuccessful) {
            completed()
        }
    }

}