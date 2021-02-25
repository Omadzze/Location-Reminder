package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
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

    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var dataSource: FakeDataSource

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    //needed to be added whenever testing livedata
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUpViewModel() {
        dataSource = FakeDataSource()
        saveReminderViewModel =
                SaveReminderViewModel(ApplicationProvider.getApplicationContext(), dataSource)
    }

    @After
    fun tearDownKoin() {
        stopKoin()
    }

    @Test
    fun testOnClear() = mainCoroutineRule.runBlockingTest {
        //when
        saveReminderViewModel.onClear()

        //then
        MatcherAssert.assertThat(saveReminderViewModel.reminderTitle.getOrAwaitValue(), CoreMatchers.`is`(CoreMatchers.nullValue()))
        MatcherAssert.assertThat(saveReminderViewModel.reminderDescription.getOrAwaitValue(), CoreMatchers.`is`(CoreMatchers.nullValue()))
        MatcherAssert.assertThat(
                saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue(),
                CoreMatchers.`is`(CoreMatchers.nullValue())
        )
        MatcherAssert.assertThat(saveReminderViewModel.selectedPOI.getOrAwaitValue(), CoreMatchers.`is`(CoreMatchers.nullValue()))
        MatcherAssert.assertThat(saveReminderViewModel.latitude.getOrAwaitValue(), CoreMatchers.`is`(CoreMatchers.nullValue()))
        MatcherAssert.assertThat(saveReminderViewModel.longitude.getOrAwaitValue(), CoreMatchers.`is`(CoreMatchers.nullValue()))
    }

    @Test
    fun enteredData_validData_returnsTrue() {
        //given
        val title = "title"
        val location = "location"
        val reminderDataItem = ReminderDataItem(
                title, null, location,
                null, null
        )

        // When data entered is valid
        val enteredData = saveReminderViewModel.enteredData(reminderDataItem)

        // Then the method return true
        MatcherAssert.assertThat(enteredData, CoreMatchers.`is`(true))
    }

    @Test
    fun enteredData_noLocation_returnFalse() {
        //given
        val title = "title"
        val reminderDataItem = ReminderDataItem(
                title, null, null,
                null, null
        )

        //when
        val enteredData = saveReminderViewModel.enteredData(reminderDataItem)
        val snackBarText = saveReminderViewModel.showSnackBarInt.getOrAwaitValue()

        //then
        MatcherAssert.assertThat(enteredData, CoreMatchers.`is`(false))
        MatcherAssert.assertThat(snackBarText, CoreMatchers.`is`(R.string.err_select_location))
    }

    @Test
    fun enteredData_noTitle_returnFalse() {
        //given
        val location = "location"
        val reminderDataItem = ReminderDataItem(
                null, null, location,
                null, null
        )

        //when
        val enteredData = saveReminderViewModel.enteredData(reminderDataItem)
        val snackBarText = saveReminderViewModel.showSnackBarInt.getOrAwaitValue()

        //then
        MatcherAssert.assertThat(enteredData, CoreMatchers.`is`(false))
        MatcherAssert.assertThat(snackBarText, CoreMatchers.`is`(R.string.err_enter_title))
    }

    @Test
    fun check_loading() = mainCoroutineRule.runBlockingTest {

        val reminder = ReminderDataItem("title", "desc", "loc", 0.0, 0.0)

        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.validateAndSaveReminder(reminder)
        val showLoadingBefore = saveReminderViewModel.showLoading.getOrAwaitValue()
        MatcherAssert.assertThat(showLoadingBefore, CoreMatchers.`is`(true))

        mainCoroutineRule.resumeDispatcher()
        val showLoadingAfter = saveReminderViewModel.showLoading.getOrAwaitValue()
        MatcherAssert.assertThat(showLoadingAfter, CoreMatchers.`is`(false))
    }

    @Test
    fun enteredData_noLocation_shoudReturnError() = mainCoroutineRule.runBlockingTest{
        dataSource.setReturnError(true)

        val title = "title"
        val reminderDataItem = ReminderDataItem(
                title, null, null,
                null, null
        )
        saveReminderViewModel.validateAndSaveReminder(reminderDataItem)

        MatcherAssert.assertThat(
                saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),
                CoreMatchers.`is`(R.string.err_select_location)
        )
    }


}