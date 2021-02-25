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
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
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

    @get:Rule
    var instanceExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(), RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun saveReminder_andGetById() = runBlocking {

        val reminder = ReminderDTO("Hello World", "Super location", "loc", 50.50, 50.4)

        database.reminderDao().saveReminder(reminder)

        val loaded = database.reminderDao().getReminderById(reminder.id)

        assertThat(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is` (reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
    }

    @Test
    fun getReminders() = runBlockingTest {

        val reminders = database.reminderDao().getReminders()

        val loaded = database.reminderDao().getReminders()

        assertThat(loaded, `is`(reminders))
    }

    @Test
    fun noReminderFound_shouldReturnError() = runBlocking {

        val reminder = ReminderDTO("Hello World", "Super location", "loc", 50.50, 50.4, "Id")

        val loaded = database.reminderDao().getReminderById(reminder.id)

        assertThat(loaded, `is`(CoreMatchers.nullValue()))
    }
}