package com.udacity.project4.locationreminders.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

//    DONE_TODO: Add testing implementation to the RemindersLocalRepository.kt

    lateinit var database: RemindersDatabase
    lateinit var repository: RemindersLocalRepository

    @Before
    fun init() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), RemindersDatabase::class.java
        ).build()

        repository = RemindersLocalRepository(
            database.reminderDao()
        )
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertReminderAndGetById() = runBlocking {
        // GIVEN - a reminder
        val reminder = ReminderDTO("title", "description", "location", 0.0, 0.0, "id")

        // WHEN - you save the reminder and receive a reminder with the same id
        repository.saveReminder(reminder)

        val result = repository.getReminder("id")

        // THEN - The received data should have expected values
        assertThat(result is Result.Success, `is`(true))
        result as Result.Success
        assertThat(result.data.title, `is`(reminder.title))
        assertThat(result.data.description, `is`(reminder.description))
        assertThat(result.data.location, `is`(reminder.location))
        assertThat(result.data.latitude, `is`(reminder.latitude))
        assertThat(result.data.longitude, `is`(reminder.longitude))
        assertThat(result.data.id, `is`(reminder.id))

    }

    @Test
    fun insertReminderAndDeleteAll() = runBlocking {
        // GIVEN - a reminder
        val reminder = ReminderDTO("title", "description", "location", 0.0, 0.0, "id")

        // WHEN - you save the reminder and delete all
        repository.saveReminder(reminder)
        repository.deleteAllReminders()

        // THEN - The database should have no values
        val result = repository.getReminders()

        assertThat(result is Result.Success, `is`(true))
        result as Result.Success
        assertThat(result.data.size, `is`(0))
    }

    @Test
    fun getReminder_invalidId_returnsError() = runBlocking {
        // WHEN - you try to get a reminder with an invalid id
        val result = repository.getReminder("invalidId")

        // THEN - result should be Result.Error
        assertThat(result is Result.Error, `is`(true))
    }



}