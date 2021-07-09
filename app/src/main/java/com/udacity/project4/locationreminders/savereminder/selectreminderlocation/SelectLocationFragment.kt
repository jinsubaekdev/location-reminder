package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SelectLocationFragment : BaseFragment() {

    val TAG = "SelectLocationFragment"
    val REQUEST_TURN_DEVICE_LOCATION_ON = 1

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()

    private lateinit var binding: FragmentSelectLocationBinding

    lateinit var marker: Marker
    lateinit var selectedPoi: PointOfInterest
    lateinit var googleMap: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        checkDeviceLocationSettings()
        initMap(savedInstanceState)
//        DONE_TODO: zoom to the user location after taking his permission

        binding.buttonSaveLocation.setOnClickListener {
            onLocationSelected()
        }

        return binding.root
    }

    private fun onLocationSelected() {
        //        DONE_TODO: When the user confirms on the selected location,
        if(!::marker.isInitialized) {
            Toast.makeText(requireContext(), "No location selected", Toast.LENGTH_SHORT).show()
            return
        }

        _viewModel.selectedPOI.value = selectedPoi
        _viewModel.reminderSelectedLocationStr.value = selectedPoi.name
        _viewModel.latitude.value = selectedPoi.latLng.latitude
        _viewModel.longitude.value = selectedPoi.latLng.longitude
        _viewModel.navigationCommand.value = NavigationCommand.Back

        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
    }

    private fun initMap(savedInstanceState: Bundle?) {

        binding.map.onCreate(savedInstanceState)
        binding.map.onResume()
        MapsInitializer.initialize(requireActivity().applicationContext)
        binding.map.getMapAsync { map ->
            googleMap = map

            initLocation(requireContext(), googleMap)
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.style_json))

            googleMap.setOnPoiClickListener { poi ->
                if(::marker.isInitialized)
                    marker.remove()

                marker = googleMap.addMarker(
                    MarkerOptions()
                        .position(poi.latLng)
                        .title(poi.name)
                        .snippet(String.format("latitude: %.2f, longitude: %.2f", poi.latLng.latitude, poi.latLng.longitude))
                ).apply { showInfoWindow() }

                selectedPoi = poi
            }
        }
    }

    private fun checkDeviceLocationSettings() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireContext())
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if(exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(requireActivity(), REQUEST_TURN_DEVICE_LOCATION_ON)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                _viewModel.showSnackBar.value = "Error getting location settings resolution"
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if(it.isSuccessful) {
                _viewModel.showSnackBar.value = "Getting location Success!!"
            }
        }

    }

    private fun initLocation(context: Context, map:GoogleMap) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        map.isMyLocationEnabled = true

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val latLng = LatLng(location.latitude, location.longitude)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            }
        }
    }

        override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }


}
