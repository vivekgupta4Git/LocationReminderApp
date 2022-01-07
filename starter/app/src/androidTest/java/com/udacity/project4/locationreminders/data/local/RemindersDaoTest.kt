package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import junit.framework.TestCase.assertNotNull

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private lateinit var database: RemindersDatabase
    private lateinit var reminderDao: RemindersDao

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initializeDatabase(){

        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),RemindersDatabase::class.java)
            .allowMainThreadQueries().build()
        reminderDao = database.reminderDao()
    }

    @After
    fun closeDb(){
        database.close()
    }

    @Test
    fun saveReminder_newReminder_addedToDatabase()= runBlockingTest{
        //given database @Before
        //when a new reminder to save
        val reminder = ReminderDTO("title","description","location",0.0,0.0)
        reminderDao.saveReminder(reminder)

        //then check whether database has the reminder with the same id as above reminder
        val savedReminder = reminderDao.getReminderById(reminder.id)
       //asserting that reminder did found as it is not null
        assertNotNull(savedReminder)
        assertThat(savedReminder,equalTo(reminder))

    }
}