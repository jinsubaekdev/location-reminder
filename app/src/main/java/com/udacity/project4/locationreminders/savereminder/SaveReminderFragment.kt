package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.checkDeviceLocationSettings
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    companion object {
        private const val TAG = "SaveReminderFragment"
    }

    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        geofencingClient = LocationServices.getGeofencingClient(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            checkPermissionForGeofence()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    @Suppress("MissingPermission")
    private fun addGeofence(reminderDataItem: ReminderDataItem) {
        val geofence = Geofence.Builder()
            .setRequestId(reminderDataItem.id)
            .setCircularRegion(
                reminderDataItem.latitude ?: 0.0,
                reminderDataItem.longitude ?: 0.0,
                100f
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val geofencingRequest = GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofence(geofence)
        }.build()

        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)

        val geofencePendingIntent: PendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
            addOnSuccessListener {
                Log.i(TAG, "Geofence Added!")
                _viewModel.validateAndSaveReminder(reminderDataItem)
            }
            addOnFailureListener {
                Log.i(TAG, "Adding Geofence Failed")
            }
        }
    }

    private fun checkPermissionForGeofence() {
        val title = _viewModel.reminderTitle.value
        val location = _viewModel.reminderSelectedLocationStr.value

        if(title.isNullOrEmpty()) {
            _viewModel.showSnackBar.value = getString(R.string.select_title)
            return
        } else if(location.isNullOrEmpty()) {
            _viewModel.showSnackBar.value = getString(R.string.select_location)
            return
        }

        val allPermissionsGranted = requestLocationPermissions()
        if(!allPermissionsGranted) {
            return
        }


        // Permissions are checked by requestLocationPermissions() function

    }

    private fun requestLocationPermissions(): Boolean {
        var permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WAKE_LOCK
        )

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions += Manifest.permission.ACCESS_BACKGROUND_LOCATION
        }

        permissions.forEach { permission ->
            if(ContextCompat.checkSelfPermission(requireActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permissions, 0)
                return false
            }
        }

        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val requiredPermission =
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) Manifest.permission.ACCESS_BACKGROUND_LOCATION
            else Manifest.permission.ACCESS_FINE_LOCATION

        for(i in permissions.indices) {
            if(permissions[i] == requiredPermission) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    _viewModel.showSnackBar.value =
                        getString(R.string.permission_denied_explanation)
                } else {
                    val title = _viewModel.reminderTitle.value
                    val description = _viewModel.reminderDescription.value
                    val location = _viewModel.reminderSelectedLocationStr.value
                    val latitude = _viewModel.latitude.value
                    val longitude = _viewModel.longitude.value

                    val reminderDataItem =
                        ReminderDataItem(title, description, location, latitude, longitude)
                    checkDeviceLocationSettings(requireActivity()) {
                        addGeofence(reminderDataItem)
                    }
                }
            }
        }
    }

}
