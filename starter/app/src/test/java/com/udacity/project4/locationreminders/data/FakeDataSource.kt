package com.udacity.project4.locationreminders.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.runBlocking
import java.lang.Error

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(private val reminders:MutableList<ReminderDTO>?= mutableListOf()) : ReminderDataSource {

    //for livedata testing
    private val observableRemindersList = MutableLiveData<Result<List<ReminderDTO>>>()


    override suspend fun getReminders(): Result<List<ReminderDTO>> {

        reminders?.let {
            return Result.Success(ArrayList(it))
            }
        return Result.Error("Not found")
    }

    suspend fun refreshTasks(){
        observableRemindersList.value = getReminders()
    }

    //observing live data for testing
    suspend fun observeTasks() : LiveData<Result<List<ReminderDTO>>>{

        runBlocking {
            refreshTasks()
        }
        return observableRemindersList
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }


    override suspend fun getReminder(id: String): Result<ReminderDTO> {

         val foundReminder = reminders?.find { it.id == id }

        return if(foundReminder!=null)
            Result.Success(foundReminder)
        else
            Result.Error("Not found")
    }


    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }


    //for testing
    fun addReminders(vararg remindersToAdd: ReminderDTO)
    {
        for(reminder in remindersToAdd)
        {
            reminders?.add(reminder)
        }
        runBlocking {
            refreshTasks()
        }
    }

}