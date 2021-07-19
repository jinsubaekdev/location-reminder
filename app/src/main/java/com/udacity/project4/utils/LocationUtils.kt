package com.udacity.project4.utils

import android.content.IntentSender
import android.util.Log
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest


const val REQUEST_TURN_DEVICE_LOCATION_ON = 2

private const val TAG = "LocationUtils"

fun checkDeviceLocationSettings(fragment: Fragment, completed: () -> Unit = {}) {

    val locationRequest = LocationRequest.create().apply {
        priority = LocationRequest.PRIORITY_LOW_POWER
    }
    val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
    val settingsClient = LocationServices.getSettingsClient(fragment.requireActivity())
    val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())
    locationSettingsResponseTask.addOnFailureListener { exception ->
        if(exception is ResolvableApiException) {
            try {
                fragment.startIntentSenderForResult(exception.resolution.intentSender, REQUEST_TURN_DEVICE_LOCATION_ON, null, 0, 0, 0, null)
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