package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.text.isDigitsOnly
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {
    private lateinit var database: RemindersDatabase
    private lateinit var repository: RemindersLocalRepository

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDB() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        repository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun closeDB() {
        database.close()
    }

    @Test
    fun savedReminder_equalsToRepositorySavedReminder() = runBlocking {

        //when a new reminder to save
        val savedReminder = ReminderDTO("title","description","location",0.0,0.0)
        repository.saveReminder(savedReminder)

        //get back the saved reminder
        val repoSaveReminder: Result<ReminderDTO> = repository.getReminder(savedReminder.id)
        val result: ReminderDTO = (repoSaveReminder as Result.Success).data

        assertThat(repoSaveReminder, notNullValue())
        assertThat(result.id,`is`(savedReminder.id))

    }

    @Test
    fun noReminder_ReturnError() = runBlocking {
        //removing all reminders
        repository.deleteAllReminders()
        //getting back the error as there is no reminder
        val repoSaveReminder: Result<ReminderDTO> = repository.getReminder("1")
        val result: String? = (repoSaveReminder as Result.Error).message

        assertThat(repoSaveReminder,notNullValue())
        assertEquals("Reminder not found!",result)
    }



}