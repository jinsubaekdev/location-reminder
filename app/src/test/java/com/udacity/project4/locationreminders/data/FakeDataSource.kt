package com.udacity.project4.locationreminders.data

import android.util.Log
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.runBlocking

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(private val reminders: List<ReminderDTO>? = listOf()) : ReminderDataSource {

    val TAG = "FakeDataSource"

//    DONE_TODO: Create a fake data source to act as a double to the real data source

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return try {
            if(reminders == null)
                throw Exception("Reminders List is null")

            Result.Success(reminders)
        } catch (ex: Exception) {
            Result.Error(ex.localizedMessage)
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        Log.i(TAG, "Fake reminder is saved ")
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        reminders?.let {
            val reminder: ReminderDTO? = reminders.find { reminderDTO -> reminderDTO.id == id }

            if (reminder != null) {
                return Result.Success(reminder)
            }
        }

        return Result.Error("Reminder not found!")
    }

    override suspend fun deleteAllReminders() {
        Log.i(TAG, "Deleted all the reminders ")
    }


}