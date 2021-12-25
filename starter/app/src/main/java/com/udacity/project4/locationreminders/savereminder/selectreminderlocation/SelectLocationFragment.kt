package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
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
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var googleMap : GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    companion object{
       const  val REQUEST_CODE = 10
        const val ZOOM_LEVEL = 15f
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
        Snackbar.make(requireView(), "Save Location", Snackbar.LENGTH_INDEFINITE)
            .setAction("Save") {
                _viewModel.setPoi(selectedPoi)
                _viewModel.navigationCommand.value =
                    NavigationCommand.To(SelectLocationFragmentDirections.actionSelectLocationFragmentToSaveReminderFragment())
            }.show()

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            true
        }
        R.id.hybrid_map -> {
            true
        }
        R.id.satellite_map -> {
            true
        }
        R.id.terrain_map -> {
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
       // setLongClickListener()
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
        setPoiClickListener()
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation(){
        googleMap.isMyLocationEnabled = true

        //using fused location provider client as it efficient in terms of battery usage.
        fusedLocationProviderClient = FusedLocationProviderClient(requireActivity())


        fusedLocationProviderClient.lastLocation.addOnSuccessListener {location->
            if (location!=null) {
                googleMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(location.latitude, location.longitude), ZOOM_LEVEL))
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

    private fun setLongClickListener()
    {
        googleMap.setOnMapLongClickListener {
            googleMap.addMarker(MarkerOptions()
                .position(it)


            )
        }
    }


    private fun setPoiClickListener() {

        googleMap.setOnPoiClickListener {
            googleMap.addMarker(MarkerOptions()
                .position(it.latLng)
                .title(it.name)
                .snippet("${it.latLng.latitude} ,${it.latLng.longitude}")
            ).showInfoWindow()

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

}
