package com.okomilabs.dailychallenges.viewmodels

import android.app.*
import android.app.PendingIntent.*
import com.okomilabs.dailychallenges.R
import android.content.Context
import androidx.lifecycle.MutableLiveData
import android.content.SharedPreferences
import android.content.Intent
import android.util.Log
import android.view.Display
import android.widget.Toast
import androidx.core.app.AlarmManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.okomilabs.dailychallenges.utilities.AlarmReceiver
import com.okomilabs.dailychallenges.utilities.cancelNotifications
import com.okomilabs.dailychallenges.data.repos.LoginDayRepo
import com.okomilabs.dailychallenges.helpers.DateHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*


class NotificationViewModel(application: Application): AndroidViewModel(application) {
    private val appContext = application.applicationContext
    // Alarm Manager intents
    private var alarmMgr: AlarmManager? = null
    private var alarmIntent: PendingIntent
    private lateinit var cancelIntent: PendingIntent
    private val notifyIntent = Intent(application, AlarmReceiver::class.java)
    private val loginDayRepo: LoginDayRepo = LoginDayRepo(application)
    private val notificationKey: String = appContext.getString(R.string.notifications_key)
    val switchState = application.getSharedPreferences(notificationKey, Context.MODE_PRIVATE)
        .getBoolean(notificationKey, false)
    private var date: String = ""

    //Set time for Notification to be sent
    private val calendar: Calendar = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 1)
    }


    init {
        alarmMgr = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmIntent = getBroadcast(application, 0,notifyIntent, FLAG_UPDATE_CURRENT)
    }

    fun updateNotificationKey(key: Boolean) {
    viewModelScope.launch {
        appContext
                .getSharedPreferences(notificationKey, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(notificationKey, key)
            .apply()
            getDateToday()
            val todayInt: Int = DateHelper().dateToInt(date)
            val completedToday = loginDayRepo.getLoginDayByDate(todayInt)
            val chalCompletedToday = completedToday?.state
            Log.d("Check","$chalCompletedToday")

        if (key && chalCompletedToday == 0){
            setAlarm(calendar)
        }
        else if (key) {
            calendar.add(Calendar.DATE,1)
            setAlarm(calendar)
        }
        else{
            cancelIntent = getBroadcast(getApplication(), 0, notifyIntent, FLAG_CANCEL_CURRENT);
            cancelIntent
        }
    }}


    private fun setAlarm(calendar: Calendar) {
        alarmMgr?.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            alarmIntent
        )
        //Cancel pre-existing notifications
        val notificationManager = ContextCompat.getSystemService(appContext,NotificationManager::class.java) as NotificationManager
        notificationManager.cancelNotifications()

    }

    private fun getDateToday() {
        date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    }
}