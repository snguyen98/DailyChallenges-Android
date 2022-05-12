package com.okomilabs.dailychallenges.utilities

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.okomilabs.dailychallenges.R


class AlarmReceiver: BroadcastReceiver() {

//On set alarm time, send notification
    override fun onReceive(context: Context, intent: Intent) {

        val notificationManager = ContextCompat.getSystemService(
            context,
            NotificationManager::class.java
        ) as NotificationManager

        notificationManager.sendNotification(
            context.getText(R.string.reminder_content).toString(),
            context

        )

    }

}