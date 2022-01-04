package com.udacity.project4.locationreminders.savereminder

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import androidx.arch.core.executor.testing.InstantTaskExecutorRule

import org.junit.Rule




//@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    private lateinit var viewModel: SaveReminderViewModel
    private lateinit var dataSource: FakeDataSource

    //Save Reminder view model has many livedata objects and functions but here
    //we will test saveReminder function and will test showToast live data
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    /*
    Setting up viewModel for testing
     */

    @Before
    fun setupViewModel(){
        stopKoin()
        val reminder1 = ReminderDTO("reminder1","description1","location1",10.0,20.2)
        val reminder2 = ReminderDTO("reminder2","description2","location2",9.0,30.2)
        val reminder3 = ReminderDTO("reminder3","description3","location3",15.0,29.233)
        val reminderList = mutableListOf(reminder1,reminder2,reminder3)
        dataSource = FakeDataSource(reminderList)
        viewModel = SaveReminderViewModel(getApplicationContext(),dataSource)
    }

    @Test
    fun saveReminder_addingNewReminderInEmptyList_showToastReminderSaved(){
        stopKoin()
        //given empty list
        dataSource = FakeDataSource()
        //when new reminder is saved
        val reminder1 = ReminderDataItem("reminder1","description1","location1",10.0,20.2)
        viewModel.saveReminder(reminder1)
            //then show Toast Reminder Saved
        assertThat(viewModel.showToast.getOrAwaitValue(), `is`(
            getApplicationContext<Context>()
                .getString(R.string.reminder_saved))
        )

    }

    //TODO: provide testing to the SaveReminderView and its live data objects


}