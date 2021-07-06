package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var dataSource: ReminderDataSource

    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @Before
    fun setup() {
        dataSource = FakeDataSource(null)
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), dataSource)
    }

    @After
    fun close() {
        stopKoin()
    }

    @Test
    fun validateEnteredData_titleEmpty_shouldReturnError() = runBlocking {
        val reminder = ReminderDataItem("","description","location",0.0,0.0,"id")

        saveReminderViewModel.validateEnteredData(reminder)
        assertThat(saveReminderViewModel.showSnackBarInt.value, `is`(R.string.err_enter_title))
    }

    @Test
    fun validateEnteredData_titleNull_shouldReturnError() = runBlocking {
        val reminder = ReminderDataItem(null,"description","location",0.0,0.0,"id")

        saveReminderViewModel.validateEnteredData(reminder)
        assertThat(saveReminderViewModel.showSnackBarInt.value, `is`(R.string.err_enter_title))
    }


    @Test
    fun validateEnteredData_locationEmpty_shouldReturnError() = runBlocking {
        val reminder = ReminderDataItem("title","description","",0.0,0.0,"id")

        saveReminderViewModel.validateEnteredData(reminder)
        assertThat(saveReminderViewModel.showSnackBarInt.value, `is`(R.string.err_select_location))
    }

    @Test
    fun validateEnteredData_locationNull_shouldReturnError() = runBlocking {
        val reminder = ReminderDataItem("title","description",null,0.0,0.0,"id")

        saveReminderViewModel.validateEnteredData(reminder)
        assertThat(saveReminderViewModel.showSnackBarInt.value, `is`(R.string.err_select_location))
    }

    @Test
    fun saveReminder_check_loading() {
        val reminder = ReminderDataItem("title","description","location",0.0,0.0,"id")

        // When - Trying to save a reminder and before starting viewModelScope.launch
        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.saveReminder(reminder)

        // Then - showLoading should be true
        assertThat(saveReminderViewModel.showLoading.value, `is`(true))

        // When - After viewModelScope.launch
        mainCoroutineRule.resumeDispatcher()

        // Then - showLoading should be false
        assertThat(saveReminderViewModel.showLoading.value, `is`(false))

    }
}