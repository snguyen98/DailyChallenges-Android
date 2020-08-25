package com.okomilabs.dailychallenges.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.okomilabs.dailychallenges.R
import java.text.SimpleDateFormat
import java.util.*

class WelcomeViewModel(application: Application): AndroidViewModel(application) {
    private val appContext = application.applicationContext

    // Shared preferences keys
    private val challengeKey: String = appContext.getString(R.string.challenge_key)
    private val settingsKey: String = appContext.getString(R.string.settings_key)

    // Shared preferences strings
    private val lastLoginPrefs: String = appContext.getString(R.string.last_login)
    private val firstLaunchPrefs: String = appContext.getString(R.string.first_launch)

    /**
     * Checks if the welcome has been shown today by comparing the last login to the date today
     *
     * @return True if welcome has been shown and false otherwise
     */
    fun hasShownWelcome(): Boolean {
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        return today == appContext
            .getSharedPreferences(challengeKey, Context.MODE_PRIVATE)
            .getString(lastLoginPrefs, "")
    }

    /**
     * Checks if this is the user's first time launching the app by checking shared preferences
     *
     * @return True if first launch, false otherwise
     */
    fun checkFirstLaunch(): Boolean {
        return appContext
            .getSharedPreferences(settingsKey, Context.MODE_PRIVATE)
            .getBoolean(firstLaunchPrefs, true)
    }

    /**
     * Sets the user's shared preference value for first launch to false
     */
    fun disableFirstLaunch() {
        appContext
            .getSharedPreferences(settingsKey, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(firstLaunchPrefs, false)
            .apply()
    }

}