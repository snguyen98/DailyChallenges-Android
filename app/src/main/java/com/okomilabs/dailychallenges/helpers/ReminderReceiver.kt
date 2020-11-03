package com.okomilabs.dailychallenges.helpers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.okomilabs.dailychallenges.R
import com.okomilabs.dailychallenges.activities.SplashActivity
import java.text.SimpleDateFormat
import java.util.*

class ReminderReceiver: BroadcastReceiver() {
    override fun onReceive(appContext: Context, intent: Intent?) {
        Log.d("Reminder", "Received Alarm")
        if (shouldShowNotification(appContext)) {
            showNotification(appContext)
        }
    }


    private fun showNotification(appContext: Context) {
        Log.d("Reminder", "Building Notification")
        val builder = NotificationCompat.Builder(
            appContext, appContext.getString(R.string.reminder_channel_id)
        )

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            appContext,
            0,
            Intent(appContext, SplashActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            0
        )

        builder
            .setSmallIcon(R.mipmap.complete_icon)
            .setContentTitle(appContext.getString(R.string.reminder_notif_title))
            .setContentText(appContext.getString(R.string.reminder_notif_message))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .priority = NotificationCompat.PRIORITY_DEFAULT

        createNotificationChannel(appContext)
    }

    private fun createNotificationChannel(appContext: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                appContext.getString(R.string.reminder_channel_id),
                appContext.getString(R.string.reminder_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = appContext.getString(R.string.reminder_channel_desc)
            }

            val notificationManager: NotificationManager = appContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun shouldShowNotification(appContext: Context): Boolean {
        val challengePrefs: SharedPreferences = appContext.getSharedPreferences(
            appContext.getString(R.string.challenge_key), Context.MODE_PRIVATE
        )

        val isCompleted: Boolean = challengePrefs.getBoolean(
            appContext.getString(R.string.challenge_complete), false
        )

        val loggedInToday: Boolean = challengePrefs.getString(
            appContext.getString(R.string.last_login), ""
        ) != SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        return !isCompleted || !loggedInToday
    }
}