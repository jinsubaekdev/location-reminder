package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.MyApp
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.nullValue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.android.get
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var dataSource: ReminderDataSource

    private lateinit var remindersListViewModel: RemindersListViewModel

    @Before
    fun setup() {
        dataSource = FakeDataSource(null)
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), dataSource)
    }

    @After
    fun close() {
        stopKoin()
    }

    //TODO: provide testing to the RemindersListViewModel and its live data objects

    @Config(sdk = [Build.VERSION_CODES.O_MR1])
    @Test
    fun loadReminders_shouldReturnError() {
        // GIVEN - Reminders are null

        assertThat(remindersListViewModel.showSnackBar.value, nullValue())

        runBlocking {
            // WHEN - Trying to load reminders
            remindersListViewModel.loadReminders()

            // THEN - Snack bar should get a error message
            assertThat(remindersListViewModel.showSnackBar.value, `is`("Reminders List is null"))
        }
    }

    @Config(sdk = [Build.VERSION_CODES.O_MR1])
    @Test
    fun loadReminders_check_loading() {

        // When - Trying to load reminders and before starting viewModelScope.launch
        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()

        // Then - showLoading should be true
        assertThat(remindersListViewModel.showLoading.value, `is`(true))

        // When - After viewModelScope.launch
        mainCoroutineRule.resumeDispatcher()

        // Then - showLoading should be false
        assertThat(remindersListViewModel.showLoading.value, `is`(false))

    }

}