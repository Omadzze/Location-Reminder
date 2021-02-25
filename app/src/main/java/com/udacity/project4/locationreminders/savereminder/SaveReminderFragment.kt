package com.udacity.project4.locationreminders.savereminder

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {

    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)
        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this


        geofencingClient = LocationServices.getGeofencingClient(requireActivity())

        initOnClick()
    }

    private fun initOnClick() {
        binding.selectLocation.setOnClickListener {
            navigateToSelectLocationFragment()
        }

        binding.saveReminder.setOnClickListener {
            val reminder = getUserInput()

            if (_viewModel.enteredData(reminder)) {
                addGeofenceAndSaveReminder(reminder)
            }
        }
    }

    private fun getUserInput(): ReminderDataItem {

        val title = _viewModel.reminderTitle.value
        val description = _viewModel.reminderDescription.value
        val location = _viewModel.reminderSelectedLocationStr.value
        val latitude = _viewModel.latitude.value
        val longitude = _viewModel.longitude.value

        return ReminderDataItem(
                title,
                description,
                location,
                latitude,
                longitude
        )
    }


    private val ACTION_GEOFENCE_EVENT = "LocationReminderApp.action.ACTION_GEOFENCE_EVENT"

    private val GEOFENCE_RADIUS_IN_METERS = 50f

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireActivity().applicationContext, GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(requireActivity(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT)
    }

    @SuppressLint("MissingPermission")
    private fun addGeofenceAndSaveReminder(reminder: ReminderDataItem?) {

        if (reminder != null) {
            val geofence = Geofence.Builder()
                    .setRequestId(reminder.id)
                    .setCircularRegion(
                            reminder.latitude!!, reminder.longitude!!, GEOFENCE_RADIUS_IN_METERS
                    )
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .build()

            val geofencingRequest = GeofencingRequest.Builder()
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    .addGeofence(geofence)
                    .build()

            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
                addOnSuccessListener {
                    _viewModel.validateAndSaveReminder(reminder)
                }
                addOnFailureListener {
                    Log.i("TEST", "failed geofences")
                }
            }
        }
    }

    private fun navigateToSelectLocationFragment() {
        _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
    }


    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }
}
