package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

//    DONE_TODO: Add testing implementation to the RemindersDao.kt
    @get:Rule()
    var instantExecutorRule = InstantTaskExecutorRule()

    lateinit var database: RemindersDatabase

    @Before
    fun init() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), RemindersDatabase::class.java)
            .build()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertReminderAndGetById() = runBlockingTest {
        // GIVEN - a reminder
        val reminder = ReminderDTO("title", "description", "location", 0.0, 0.0, "id")

        // WHEN - you save the reminder and receive a reminder with the same id
        database.reminderDao().saveReminder(reminder)

        val loaded = database.reminderDao().getReminderById("id")

        // THEN - The received data should have expected values
        assertThat(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.location, `is`(reminder.location))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.longitude, `is`(reminder.longitude))
        assertThat(loaded.id, `is`(reminder.id))

    }

    @Test
    fun insertReminderAndDeleteAll() = runBlockingTest {
        // GIVEN - a reminder
        val reminder = ReminderDTO("title", "description", "location", 0.0, 0.0, "id")

        // WHEN - you save the reminder and delete all
        database.reminderDao().saveReminder(reminder)
        database.reminderDao().deleteAllReminders()

        // THEN - The database should have no values
        val result = database.reminderDao().getReminders().size
        assertThat(result, `is`(0))
    }
}