package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*
import java.util.concurrent.TimeUnit

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var googleMap : GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var currentLocation: Location? = null


    companion object{
       const  val REQUEST_CODE = 10
        const val ZOOM_LEVEL = 17f
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        //instantiate the SupportMapFragment and use the getMapAsync() method initialize the map
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)


        return binding.root
    }

    /*
    Setting the selected Poi to viewModel and navigating back.
     */
    private fun onLocationSelected(selectedPoi : PointOfInterest) {
        Log.i("myTag","Selected Poi (Lat & Long ) : ${selectedPoi.name}  (${selectedPoi.latLng.latitude} , ${selectedPoi.latLng.longitude})")
       // _viewModel.showSnackBar.value = "Save Location"

        Snackbar.make(requireView(), "Save Location", Snackbar.LENGTH_INDEFINITE)
            .setAction("Save") {
                _viewModel.setPoi(selectedPoi)
                _viewModel.navigationCommand.postValue(
                    NavigationCommand.Back
                )
              //  fragmentManager?.popBackStack()
            }.show()

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

    override fun onMapReady(map: GoogleMap) {
        googleMap = map



        setMapStyle()


        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            val permission = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION ,Manifest.permission.ACCESS_COARSE_LOCATION)
            ActivityCompat.requestPermissions(requireActivity(),permission,REQUEST_CODE)
            return
        }
        enableMyLocation()
        setPoiClickListener(map)
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation(){
        googleMap.isMyLocationEnabled = true



//using fused location provider client as it efficient in terms of battery usage.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationRequest = LocationRequest().apply {
            interval = TimeUnit.SECONDS.toMillis(60)
            fastestInterval = TimeUnit.SECONDS.toMillis(30)
            maxWaitTime = TimeUnit.MINUTES.toMillis(2)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object :LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                locationResult?.lastLocation.let {
                    currentLocation = it
                    if(currentLocation!=null)
                    {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            LatLng(currentLocation!!.latitude, currentLocation!!.longitude),
                            ZOOM_LEVEL
                        ))
                    }
                }
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper())



/*
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {location->
            if (location!=null) {
                googleMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(location.latitude, location.longitude), ZOOM_LEVEL))
                Log.i("myTag","${location.latitude}, ${location.longitude}")
            }
        }
*/

    }


    override fun onDestroy() {

        super.onDestroy()
        val removeTask = fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        removeTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("myTag", "Location Callback removed.")
            } else {
                Log.d("myTag", "Failed to remove Location Callback.")
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if(REQUEST_CODE == requestCode)
        {
            if(grantResults.size>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED && grantResults[1] ==PackageManager.PERMISSION_GRANTED)
            {
                enableMyLocation()
            }
        }

    }




    private fun setPoiClickListener(gmap : GoogleMap) {

        gmap.setOnPoiClickListener {
            val poiMarker = gmap.addMarker(
                MarkerOptions().position(it.latLng)
                    .title(it.name)
                    .snippet("${it.latLng.latitude},${it.latLng.longitude}")
            )
            poiMarker?.showInfoWindow()
            onLocationSelected(it)
        }

    }

    private fun setMapStyle()
    {
        try {
            val success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(activity,R.raw.map_style))
            if(!success)
            {
                Log.e("myMap","Style parsing Failed.")
            }
        }catch (e :Resources.NotFoundException)
        {
            Log.e("myMap","can't find the style. Error : ",e)
        }
    }


    /*
    // FusedLocationProviderClient - Main class for receiving location updates.
private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

// LocationRequest - Requirements for the location updates, i.e.,
// how often you should receive updates, the priority, etc.
private lateinit var locationRequest: LocationRequest

// LocationCallback - Called when FusedLocationProviderClient
// has a new Location
private lateinit var locationCallback: LocationCallback

// This will store current location info
private var currentLocation: Location? = null

fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)




//subscribe
fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())

//unsubscribe
val removeTask = fusedLocationProviderClient.removeLocationUpdates(locationCallback)
removeTask.addOnCompleteListener { task ->
if (task.isSuccessful) {
	Log.d(TAG, "Location Callback removed.")
} else {
	Log.d(TAG, "Failed to remove Location Callback.")
}
}


     */


}
