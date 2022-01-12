package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel


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


        _viewModel.validatedNowStartGeofence.observe(viewLifecycleOwner, Observer {
          enabled->
            if(enabled)
            requestForegroundAndBackgroundPermissions()
        })

        binding.saveReminder.setOnClickListener {

            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude
            val longitude = _viewModel.longitude.value

            val reminderDataItem = ReminderDataItem(
                title,
                description.value,
                location,
                latitude.value,
                longitude

            )
            _viewModel.validateAndSaveReminder(reminderDataItem)

        }

    }

  override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }


    private fun foregroundAndBackgroundPermissionsApproved() : Boolean{

        val foregroundPermissionsApproved  = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                )

        val backgroudPermissionsApproved = (
                if(runningQOrLater)
                {
                    PackageManager.PERMISSION_GRANTED==
                            ActivityCompat.checkSelfPermission(requireContext(),
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
                else
                    true
                )

        return foregroundPermissionsApproved && backgroudPermissionsApproved
    }


    private fun requestForegroundAndBackgroundPermissions(){
        if(foregroundAndBackgroundPermissionsApproved())
        {
            return
        }

        var permissionArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        val resultCode = when{
            runningQOrLater->{
                permissionArray+= Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_ONLY_PERMISSION_REQUEST_CODE
        }
        requestPermissions(permissionArray,resultCode)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if(grantResults.isEmpty()||
            grantResults[LOCATION_PERMISSION_INDEX]== PackageManager.PERMISSION_DENIED ||
            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX]== PackageManager.PERMISSION_DENIED)   )
        {
            Snackbar.make(binding.root,getString(R.string.describle_permisssion_requirement), Snackbar.LENGTH_INDEFINITE )
                .setAction(getString(R.string.user_permission_acceptance_string)){
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID,null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()


        }
        else
        {
            checkDeviceLocationSettings()
        }

    }

    private fun checkDeviceLocationSettings(resolve:Boolean = true){
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingClient = LocationServices.getSettingsClient(requireActivity())

        val locationSettingResponseTask = settingClient.checkLocationSettings(builder.build())

        locationSettingResponseTask.addOnFailureListener{
                exception ->
            if(exception is ResolvableApiException && resolve)
            {
                try {
                    exception.startResolutionForResult(requireActivity(),
                        REQUEST_TURN_DEVICE_LOCATION_ON)
                }catch (sendEx : IntentSender.SendIntentException){
                    //log error
                }
            }
            else
            {
                Snackbar.make(binding.root,
                    "TURN ON LOCATION SETTING", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Retry"){
                        checkDeviceLocationSettings()
                    }.show()
            }
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUEST_TURN_DEVICE_LOCATION_ON)
            checkDeviceLocationSettings(false)
    }

    companion object {
        val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 111
        val REQUEST_FOREGROUND_ONLY_PERMISSION_REQUEST_CODE  = 222
        val REQUEST_TURN_DEVICE_LOCATION_ON = 333
        private const val TAG = "myTag"
        private const val LOCATION_PERMISSION_INDEX = 0
        private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
        const val ACTION_GEOFENCE_EVENT = "com.udacity.project4.locationreminders.GEOFENCING_EVENT"

    }

   }
