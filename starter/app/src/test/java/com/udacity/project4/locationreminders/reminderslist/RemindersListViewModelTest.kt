package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
//import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    // Executes each task synchronously using Architecture Components.
  /*  @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
*/
     private lateinit var viewModel: RemindersListViewModel

    private lateinit var  fakeDataSource : FakeDataSource
    @Before
    fun initializeViewModel(){
        stopKoin()
        val reminder1 = ReminderDTO("reminder1","description1","location1",10.0,20.2)
        val reminder2 = ReminderDTO("reminder2","description2","location2",9.0,30.2)
        val reminder3 = ReminderDTO("reminder3","description3","location3",15.0,29.233)
        val reminderList = mutableListOf(reminder1,reminder2,reminder3)
        fakeDataSource = FakeDataSource(reminderList)

        viewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(),fakeDataSource)

    }

    @Test
    fun loadReminders_ (){

    }


    //
// TODO: provide testing to the RemindersListViewModel and its live data objects

}