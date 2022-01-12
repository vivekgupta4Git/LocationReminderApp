package com.udacity.project4.locationreminders

import android.Manifest
import android.annotation.TargetApi
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.BuildConfig
import com.udacity.project4.R

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {

    private lateinit var layout : ConstraintLayout
    private lateinit var fragment : NavHostFragment


    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)

         layout = findViewById(R.id.constraintLayout)
         Log.i(TAG,"Firebase user : ${FirebaseAuth.getInstance().currentUser?.displayName}")


           fragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment


        requestForegroundAndBackgroundPermissions()

    }






    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
               (fragment).navController.popBackStack()
                return true
            }


        }
        return super.onOptionsItemSelected(item)
    }

    private fun foregroundAndBackgroundPermissionsApproved() : Boolean{

        val foregroundPermissionsApproved  = (
            PackageManager.PERMISSION_GRANTED ==
                    ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                )

        val backgroudPermissionsApproved = (
        if(runningQOrLater)
        {
                PackageManager.PERMISSION_GRANTED==
                        ActivityCompat.checkSelfPermission(this,
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
        ActivityCompat.requestPermissions(this,
                                        permissionArray,resultCode)

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
            Snackbar.make(layout,"Permission is must",Snackbar.LENGTH_INDEFINITE )
                .setAction("Enable"){
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package",BuildConfig.APPLICATION_ID,null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()


        }
        else
        {
            checkDeviceLocationSettingsAndStartGeoFence()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }

    private fun checkDeviceLocationSettingsAndStartGeoFence(resolve:Boolean = true){
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingClient = LocationServices.getSettingsClient(this)

        val locationSettingResponseTask = settingClient.checkLocationSettings(builder.build())

        locationSettingResponseTask.addOnFailureListener{
            exception ->
            if(exception is ResolvableApiException && resolve)
            {
                try {
                    exception.startResolutionForResult(this,
                    REQUEST_TURN_DEVICE_LOCATION_ON)
                }catch (sendEx : IntentSender.SendIntentException){
                    //log error
                }
            }
            else
            {
                Snackbar.make(layout,
                "TURN ON LOCATION SETTING",Snackbar.LENGTH_INDEFINITE)
                    .setAction("Retry"){
                        checkDeviceLocationSettingsAndStartGeoFence()
                    }.show()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUEST_TURN_DEVICE_LOCATION_ON)
            checkDeviceLocationSettingsAndStartGeoFence(false)
    }

    companion object {
        val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 111
        val REQUEST_FOREGROUND_ONLY_PERMISSION_REQUEST_CODE  = 222
        val REQUEST_TURN_DEVICE_LOCATION_ON = 333
        private const val TAG = "myTag"
        private const val LOCATION_PERMISSION_INDEX = 0
        private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1

    }
}
