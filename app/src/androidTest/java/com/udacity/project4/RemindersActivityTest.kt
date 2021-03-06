package com.udacity.project4

import android.app.Application
import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */

    @get:Rule
    val activityTestRule: ActivityTestRule<RemindersActivity> = ActivityTestRule(RemindersActivity::class.java)

    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

//    DONE_TODO: add End to End testing to the app

    @ExperimentalCoroutinesApi
    @Test
    fun totalTest() {
        // Give a fake email as if the user logged in before
        appContext.getSharedPreferences(RemindersActivity.PACKAGE_NAME, Context.MODE_PRIVATE)
            .edit().putString(RemindersActivity.EMAIL, "FakeEmailForTest")
            .apply()

        // start activity
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)

        // check if there is no data
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).check(matches(isDisplayed()))
        onView(withId(R.id.reminderDescription)).check(matches(isDisplayed()))
        onView(withId(R.id.selectLocation)).check(matches(isDisplayed()))
        onView(withId(R.id.saveReminder)).check(matches(isDisplayed()))

        onView(withId(R.id.reminderTitle)).perform(replaceText("Title"))
        onView(withId(R.id.reminderDescription)).perform(replaceText("Description"))
        onView(withId(R.id.saveReminder)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.select_location)))

        Thread.sleep(3000)  // Waits until the snack bar is disappeared
        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.map)).check(matches(isDisplayed()))

        onView(withId(R.id.button_save_location)).perform(click())
        onView(withText(R.string.no_location_selected)).inRoot(withDecorView(not(`is`(activityTestRule.activity.window.decorView)))).check(matches(isDisplayed()))

        onView(withId(R.id.map)).perform(click())
        onView(withId(R.id.button_save_location)).perform(click())
        onView(withId(R.id.reminderTitle)).check(matches(withText("Title")))
        onView(withId(R.id.reminderDescription)).check(matches(withText("Description")))

        onView(withId(R.id.saveReminder)).perform(click())
        onView(withId(R.id.reminderCardView)).check(matches(isDisplayed()))
        onView(withText("Title")).check(matches(withId(R.id.title)))
        onView(withText("Description")).check(matches(withId(R.id.description)))
        onView(withId(R.id.logout)).check(matches(isDisplayed()))

        onView(withId(R.id.logout)).perform(click())
        val email = appContext.getSharedPreferences(RemindersActivity.PACKAGE_NAME, Context.MODE_PRIVATE)
            .getString(RemindersActivity.EMAIL, "default")

        assertThat(email, `is`(""))
    }
}
