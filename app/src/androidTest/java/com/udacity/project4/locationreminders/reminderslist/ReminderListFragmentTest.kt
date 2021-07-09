package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.core.IsNot.not
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {

//    DONE_TODO: test the navigation of the fragments.

    @Test
    fun clickFab_navigateToAddReminder() = runBlockingTest {
        // On ReminderListFragment
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.addReminderFAB)).perform(click())

        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

//    DONE_TODO: test the displayed data on the UI.
    @Test
    fun reminderList_DisplayedData() = runBlockingTest {
    val reminder = ReminderDataItem("title", "description", "location", 0.0, 0.0, "id")
    val reminders: List<ReminderDataItem> = listOf(reminder)

    val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
    scenario.onFragment {
        it._viewModel.remindersList.value = reminders
    }

    onView(withId(R.id.noDataTextView)).check(matches(not(isDisplayed())))
    onView(withId(R.id.title)).check(matches(isDisplayed()))
    onView(withId(R.id.title)).check(matches(withText(reminder.title)))
    onView(withId(R.id.description)).check(matches(isDisplayed()))
    onView(withId(R.id.description)).check(matches(withText(reminder.description)))
    onView(withId(R.id.addReminderFAB)).check(matches(isDisplayed()))
}

//    Don't_Know_What_TODO: add testing for the error messages.
    //  No idea what error messages should I test because there are no "error contents" in ReminderListFragmentTest

}