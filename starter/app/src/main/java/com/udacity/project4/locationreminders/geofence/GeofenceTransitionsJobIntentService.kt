package com.udacity.project4.locationreminders.geofence

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment.Companion.ACTION_GEOFENCE_EVENT
import com.udacity.project4.utils.sendNotification
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    companion object {
        private const val JOB_ID = 573

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }

    override fun onHandleWork(intent: Intent) {

        //using lessons as a reference
        if(intent.action == ACTION_GEOFENCE_EVENT){

            val geofencingEvent = GeofencingEvent.fromIntent(intent)

            if(geofencingEvent.hasError()){
                val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
                Log.e("myTag", errorMessage)
            }

            //use dwell..to save user's battery
            //for debugging I am using ENTER
            if(geofencingEvent.geofenceTransition==Geofence.GEOFENCE_TRANSITION_ENTER)
            {

                Log.i("myTag","Geofencing Transition ")
                when{
                    geofencingEvent.triggeringGeofences.isNotEmpty()->
                    {

                        sendNotification(geofencingEvent.triggeringGeofences)

                        //as mentioned in lots of question in knowledge center that we need to iterate for each geofence in the list
                    /*geofencingEvent.triggeringGeofences.forEach{
                        sendNotification(geofencingEvent.triggeringGeofences)
                    }*/

                    }

                    else-> {
                        Log.e("myTag","No Geofence Trigger found")
                        return
                    }
                }


            }

        }
   }

    private fun sendNotification(triggeringGeofences: List<Geofence>) {


        //read on knowledge centre about looping here
        for(geofence in triggeringGeofences)
        {
            val requestId = geofence.requestId

            //Get the local repository instance
            val remindersLocalRepository: RemindersLocalRepository by inject()
//        Interaction to the repository has to be through a coroutine scope
            CoroutineScope(coroutineContext).launch(SupervisorJob()) {
                //get the reminder with the request id
                val result = remindersLocalRepository.getReminder(requestId)
                if (result is Result.Success<ReminderDTO>) {
                    val reminderDTO = result.data
                    //send a notification to the user with the reminder details
                    sendNotification(
                        this@GeofenceTransitionsJobIntentService, ReminderDataItem(
                            reminderDTO.title,
                            reminderDTO.description,
                            reminderDTO.location,
                            reminderDTO.latitude,
                            reminderDTO.longitude,
                            reminderDTO.id
                        )
                    )
                }
            }
        }


    }

}