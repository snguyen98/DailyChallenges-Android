package com.okomilabs.dailychallenges.utilities

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.okomilabs.dailychallenges.R
import com.okomilabs.dailychallenges.activities.MainActivity
import com.okomilabs.dailychallenges.helpers.State

private val NOTIFICATION_ID = 0


fun NotificationManager.sendNotification(messageBody: String, applicationContext: Context) {
    // Create the content intent for the notification, which launches
    // this activity
    val contentIntent = Intent(applicationContext, MainActivity::class.java)
    val contentPendingIntent = PendingIntent.getActivity(
        applicationContext,
        NOTIFICATION_ID,
        contentIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    // Build the notification
    val builder = NotificationCompat.Builder(
        applicationContext,
        applicationContext.getString(R.string.channel_id))

    //set title, text and icon to builder
        .setSmallIcon(R.mipmap.notification_bell)
        .setContentTitle("Venture: Daily Challenge Reminder")
        .setContentText("Don't forget to complete your challenge for the day")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // set content intent
        .setContentIntent(contentPendingIntent)
        .setAutoCancel(true)


    notify(NOTIFICATION_ID, builder.build())
}

fun NotificationManager.cancelNotifications() {
    cancelAll()
}
