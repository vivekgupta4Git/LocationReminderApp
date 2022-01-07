package com.udacity.project4.locationreminders.reminderslist

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.assertion.ViewAssertions.matches

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.MyApp
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.RemindersDao
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest :KoinTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    //lazy inject property
    private  val remindersListViewModel: RemindersListViewModel by inject()
    private val remindersDao : RemindersDao by inject()

    @Before fun startKoinForTest() {
        stopKoin()// stop the original app koin, which is launched when the application starts (in "MyApp")
        val myModule = module {
            // define your module for test here
            viewModel {
                RemindersListViewModel(
                    getApplicationContext(),
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(getApplicationContext(),
                get() as ReminderDataSource)
            }

            single {
                RemindersLocalRepository(get()) as ReminderDataSource
            }

            // in-memory database dao
            single {
                  Room.inMemoryDatabaseBuilder(
                    getApplicationContext(),
                    RemindersDatabase::class.java
                )
                    // disable the main thread query check for Room
                    .allowMainThreadQueries()
                    .build()
                    .reminderDao()
            }
        }
        startKoin {
            androidLogger()
            androidContext(getApplicationContext())
            modules(listOf(myModule))
        }
    }
    @After
    fun stopKoinAfterTest() = stopKoin()

    @Test
    fun reminderListFragment_DisplayedInUI() = runBlockingTest  {
        //given data in the database
        val reminder  = ReminderDTO("title1","description1","location1",10.0,20.0)
        remindersDao.saveReminder(reminder)
        //when Reminder List Fragment is loaded with the data
            remindersListViewModel.loadReminders()
        launchFragmentInContainer<ReminderListFragment>(null,R.style.AppTheme)

        onView(withText(reminder.title)).check(matches(isDisplayed()))
        onView(withText(reminder.description)).check(matches(isDisplayed()))
        onView(withText(reminder.location)).check(matches(isDisplayed()))
    }



    @Test
    fun reminderListFragment_NoData_NoDataTextViewVisisble() = runBlockingTest {

        //Given no data in the database
        remindersDao.deleteAllReminders()

        //when list is loaded with data
        remindersListViewModel.loadReminders()
        launchFragmentInContainer<ReminderListFragment>(null,R.style.AppTheme)

        //then no Data Textview is visible and also error message
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
        onView(withText(R.string.no_data)).check(matches(isDisplayed()))

    }
//    TODO: test the navigation of the fragments.
}