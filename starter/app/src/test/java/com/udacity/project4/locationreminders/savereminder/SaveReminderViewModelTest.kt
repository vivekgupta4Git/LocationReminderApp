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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import kotlinx.coroutines.test.runBlockingTest

import org.junit.Rule
import org.junit.runners.JUnit4
import org.koin.androidx.viewmodel.dsl.ATTRIBUTE_VIEW_MODEL


@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {
    //subject under test.
    private lateinit var viewModel: SaveReminderViewModel
    private lateinit var dataSource: FakeDataSource

    //we will test saveReminder function and will test live data objects

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    /*
    Setting up viewModel for testing
     */

    @Before
    fun setupViewModel(){
        stopKoin()
        dataSource = FakeDataSource()
        viewModel = SaveReminderViewModel(getApplicationContext(),dataSource)
    }

    //livedata showToast
    @Test
    fun saveReminder_addingNewReminderInEmptyList_showToastReminderSaved(){
        //given empty list

        //when new reminder is saved
        val reminder1 = ReminderDataItem("reminder1","description1","location1",10.0,20.2)
        viewModel.saveReminder(reminder1)
            //then show Toast Reminder Saved
        assertThat(viewModel.showToast.getOrAwaitValue(), `is`(
            getApplicationContext<Context>()
                .getString(R.string.reminder_saved))
        )

    }

    @Test
    fun validateEnterData_titleEmptyOrNullOrLocationEmptyOrNull_returnFalse(){
        //for given reminders having null or empty title

        val reminder1 = ReminderDataItem("","desc","location",0.0,0.0)
        val reminder2 = ReminderDataItem(null,"desc","location",0.0,0.0)
        val reminder3 = ReminderDataItem("title","description","",0.0,0.0)
        val reminder4 = ReminderDataItem("title","description",null,0.0,0.0)
        val reminderlist = listOf(reminder1,reminder2,reminder3,reminder4)

    //when validating EmptyOrNull Title
        for(reminder in reminderlist){
           val booleanValue=  viewModel.validateEnteredData(reminder)
            //then returned value is false
            assert(!booleanValue)
        }


    }


    //live data testing
    @Test
    fun validateEnterData_EmptyOrNullTitle_showSnackBarNoTitle() {

        //given data source
        //when validating reminder without having title name
        val reminder1 = ReminderDataItem("","description1","location1",10.0,20.2)
        val reminder2 = ReminderDataItem(null,"description2","Dummy",0.0,0.0)

        val reminderList = listOf(reminder1,reminder2)
        for(reminder in reminderList){
            viewModel.validateEnteredData(reminder)
            val value = viewModel.showSnackBarInt.getOrAwaitValue()
            //then show Snack Bar No Title
            assertThat(value,`is` (R.string.err_enter_title))
        }
    }

    //live data testing
    @Test
    fun validateEnterData_EmptyOrNullLocation_showSnackBarSelectLocation() {

        //given data source

        //when validating reminder without having location
        val reminder1 = ReminderDataItem("ti","description1","",10.0,20.2)
        val reminder2 = ReminderDataItem("tit","description2","empty",0.0,0.0)

        val reminderList = listOf(reminder1,reminder2)
        for(reminder in reminderList){
            viewModel.validateEnteredData(reminder)
            val value = viewModel.showSnackBarInt.getOrAwaitValue()
            //then show Snack Bar Select location
            assertThat(value,`is` (R.string.err_select_location))
        }

    }


}