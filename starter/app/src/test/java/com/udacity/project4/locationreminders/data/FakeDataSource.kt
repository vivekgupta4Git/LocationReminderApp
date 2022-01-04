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


    var shouldReturnError = false

    override suspend fun getReminders(): Result<List<ReminderDTO>> {

    if(shouldReturnError)
        return Result.Error("Not found")
        else
           return Result.Success(ArrayList(reminders))

    }


    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }


    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if(shouldReturnError)
            return Result.Error("Not found")


         val foundReminder = reminders?.find { it.id == id }

        return if(foundReminder!=null)
            Result.Success(foundReminder)
        else
            Result.Error("Not found")
    }


    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }


}