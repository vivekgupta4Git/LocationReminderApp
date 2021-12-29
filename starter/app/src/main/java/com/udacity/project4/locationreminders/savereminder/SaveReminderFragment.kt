package com.udacity.project4.locationreminders.savereminder

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.ReminderListFragment
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import kotlinx.android.synthetic.main.fragment_select_location.*
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit

class SaveReminderFragment : BaseFragment()
{
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient
companion object{
  const  val ACTION_GEOFENCE_EVENT = "com.udacity.project4.locationreminders.GEOFENCING_EVENT"
}

    private val goeFencingPendingIntent by lazy{
        val intent = Intent(requireActivity(),GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(requireContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude
            val longitude = _viewModel.longitude.value

            _viewModel.validateAndSaveReminder( ReminderDataItem(
                title,
                description.value,
                location,
                latitude.value ,
                longitude
            ))

            _viewModel.enableGeofence.observe(viewLifecycleOwner, Observer { enabled->
                if(enabled)
                {
                    addGeofencing()
                    _viewModel.onceEnabledDisableAgainGeofence()
                }
            })

       }
    }

    @SuppressLint("MissingPermission")
    private fun addGeofencing(){

        //Use the dwell transition type to reduce alert spam
        val geofence = Geofence.Builder().apply {
            setRequestId("reminderGeofence")
                setCircularRegion(_viewModel.latitude.value!!,_viewModel.longitude.value!!,100f)
                setExpirationDuration(TimeUnit.HOURS.toMillis(1))
                setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL)
                setLoiteringDelay(300000) //5 minutes.
                }.build()

        val geofencingRequest = GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofence(geofence)
                                                                    }.build()


        geofencingClient.removeGeofences(goeFencingPendingIntent)?.run {
            addOnCompleteListener {
                geofencingClient.addGeofences(geofencingRequest,goeFencingPendingIntent)?.run {
                    addOnSuccessListener {
                        Log.i("myTag","Success")
                        Toast.makeText(activity?.applicationContext,"Geofencing Activated",Toast.LENGTH_LONG).show()
                    }
                    addOnFailureListener{
                        Log.i("myTag","Failed")
                        Toast.makeText(activity?.applicationContext,"Geofencing Failed",Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }


}
