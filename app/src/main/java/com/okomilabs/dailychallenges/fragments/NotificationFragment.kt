package com.okomilabs.dailychallenges.fragments

import android.app.*
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.widget.Toast
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.okomilabs.dailychallenges.R
import com.okomilabs.dailychallenges.data.repos.LoginDayRepo
import com.okomilabs.dailychallenges.helpers.DateHelper
import com.okomilabs.dailychallenges.viewmodels.NotificationViewModel
import kotlinx.android.synthetic.main.fragment_notification.view.*
import java.text.SimpleDateFormat
import java.util.*


class NotificationFragment: Fragment() {

    private lateinit var notificationViewModel: NotificationViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        notificationViewModel = ViewModelProvider(this).get(NotificationViewModel::class.java)
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_notification, container, false)

        createNotificationChannel(
            getString(R.string.channel_id),
            getString(R.string.channel_name)
        )

        return root
    }

    //Trigger notification workflow
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val boon: Boolean = notificationViewModel.switchState
        view.findViewById<Switch>(R.id.simpleSwitch).isChecked = boon


        //Notification switch functionality
        view.findViewById<Switch>(R.id.simpleSwitch).setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                notificationViewModel.updateNotificationKey(true)
                val myToast = Toast.makeText(context,"Enabled Notifications",Toast.LENGTH_SHORT)
                myToast.show()
            }
            else {
                notificationViewModel.updateNotificationKey(false)
                val myToast = Toast.makeText(context,"Disabled Notifications",Toast.LENGTH_SHORT)
                myToast.show()
            }
        }
    }


    //Set up Notification Channel
    private fun createNotificationChannel(channel_ID: String, channel_name: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channel_ID, channel_name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager = requireActivity().getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
    }
}
