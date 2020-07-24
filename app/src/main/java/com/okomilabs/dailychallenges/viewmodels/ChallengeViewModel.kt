package com.okomilabs.dailychallenges.viewmodels

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.okomilabs.dailychallenges.R
import com.okomilabs.dailychallenges.data.entities.Challenge
import com.okomilabs.dailychallenges.data.entities.Link
import com.okomilabs.dailychallenges.data.entities.LoggedDay
import com.okomilabs.dailychallenges.data.repos.ChallengeRepo
import com.okomilabs.dailychallenges.data.repos.LoggedDayRepo
import com.okomilabs.dailychallenges.helpers.DateHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ChallengeViewModel(application: Application): AndroidViewModel(application) {
    private val challengePrefs: SharedPreferences = application.getSharedPreferences(
        application.getString(R.string.challenge_key), Context.MODE_PRIVATE
    )

    private val skipsPrefs: SharedPreferences = application.getSharedPreferences(
        application.getString(R.string.skips_key), Context.MODE_PRIVATE
    )

    private val listener: SharedPreferences.OnSharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                application.getString(R.string.curr_id) -> updateCurrent()
            }
        }

    private val challengeRepo: ChallengeRepo = ChallengeRepo(application)
    private val loggedDayRepo: LoggedDayRepo = LoggedDayRepo(application)

    private val datePrefs: String = application.getString(R.string.curr_date)
    private val idPrefs: String = application.getString(R.string.curr_id)
    private val skippedChallengePrefs: String = application.getString(R.string.skipped_challenge)
    private val skipsLeftPrefs: String = application.getString(R.string.skips_remaining)

    private var date: String = ""
    private var challengeId: Int = -1
    private var completed: Boolean = false
    private var frozen: Boolean = false

    var challenge: MutableLiveData<Challenge> = MutableLiveData<Challenge>()
    var links: MutableLiveData<List<Link>> = MutableLiveData<List<Link>>()


    init {
        // Keeps the challenge id instance variables updated with the value in shared preferences
        challengePrefs.registerOnSharedPreferenceChangeListener(listener)

        // Doesn't refresh challenge if it's the same day
        if (isNewDay()) {
            refreshChallenge()
        }

        updateCurrent()
    }


    /**
     * Detaches listener when the view model is cleared
     */
    override fun onCleared() {
        challengePrefs.unregisterOnSharedPreferenceChangeListener(listener)
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////// Setting Functions /////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Function that updates the challenge id with the value in shared preferences and retrieves
     * the challenge info from the database
     */
    private fun updateCurrent() {
        challengeId = challengePrefs.getInt(idPrefs, -1)

        viewModelScope.launch(Dispatchers.IO) {
            challenge.postValue(challengeRepo.challengeById(challengeId))
            links.postValue(challengeRepo.getLinksById(challengeId))
        }
    }

    /**
     * Function that adds today's challenge to the logged day database
     */
    private fun addLoggedDay() {
        viewModelScope.launch {
            val dateInt: Int = DateHelper().dateToInt(date)       // Date to be stored as an int

            loggedDayRepo.addLoggedDay(
                LoggedDay(
                    dateInt,
                    challengeId,
                    completed,
                    frozen
                )
            )
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////// Date Functions ///////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Checks if a new day has started since last login
     *
     * @return True if today is a new day and false otherwise
     */
    fun isNewDay(): Boolean {
        getDateToday()      // Gets the date today first

        val lastLoggedIn: String? = challengePrefs.getString(
            datePrefs, null
        )

        return lastLoggedIn != date
    }

    /**
     * Sets the date instance variable to today in the form dd/MM/yyyy
     */
    private fun getDateToday() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy")
        date = dateFormat.format(Date())
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////// Challenge Functions ////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Chooses a challenge for today and sets the details into shared preferences
     */
    private fun setChallengeToday() {
        viewModelScope.launch(Dispatchers.IO) {
            val total: Int = challengeRepo.getTotal()
            chooseRandomChallenge(total)                // Sets challenge ID instance variable

            // Adds all challenge info to shared preferences
            with (challengePrefs.edit()) {
                putString(datePrefs, date)
                putInt(idPrefs, challengeId)
                apply()
            }

            addLoggedDay()
        }
    }

    /**
     * Chooses a random challenge from the database excluding those that were skipped today
     *
     * @param total The total number of challenges in the database
     */
    private fun chooseRandomChallenge(total: Int) {
        val challenges: MutableSet<Int> = (1..total).toMutableSet()
        val skipped: Int = skipsPrefs.getInt(skippedChallengePrefs, -1)

        if (challengeId != -1) {
            challenges.remove(challengeId)
        }

        if (skipped != -1) {
            challenges.remove(skipped)
        }

        setSkippedChallenge(challengeId)
        challengeId = challenges.random()
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////// Button Functions /////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Function to call private function setChallengeToday
     */
    fun refreshChallenge() {
        // Resets skips remaining and skipped challenges
        setSkipsRemaining(2)
        resetSkippedChallenges()

        setChallengeToday()     // Gets new challenge for the day
    }

    /**
     * Sets today's challenge as complete in the view model, shared preferences and logged day db
     */
    fun markComplete() {
        completed = true
        addLoggedDay()
    }

    /**
     * Sets today's challenge as frozen in the view model, shared preferences and logged day db
     */
    fun freezeDay() {
        frozen = true
        addLoggedDay()
    }

    /**
     * Adds current challenge to the skipped set and sets a new challenge for today
     */
    fun skipChallenge() {
        val skipsRemaining: Int = skipsPrefs.getInt(skipsLeftPrefs, 0)

        if (skipsRemaining > -100) {
            setSkipsRemaining(skipsRemaining - 1)
            setChallengeToday()
        }
    }

    /**
     * Gets the remaining skips allowed
     */
    fun getSkips(): Int {
        return skipsPrefs.getInt(skipsLeftPrefs, 0)
    }

    /**
     * Checks if the current challenge is complete
     */
    fun isComplete(): Boolean {
        return completed
    }

    /**
     * Checks if the current challenge is frozen
     */
    fun isFrozen(): Boolean {
        return frozen
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////// Skip Helper Functions //////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Sets the skipped set of challenges to shared preferences
     *
     * @param skipped The set of challenges to be added
     */
    private fun setSkippedChallenge(skipped: Int) {
        skipsPrefs.edit().putInt(skippedChallengePrefs, skipped).apply()
    }

    /**
     * Sets the number of skips remaining to shared preferences
     *
     * @param skips The number of skips to be set
     */
    private fun setSkipsRemaining(skips: Int) {
        skipsPrefs.edit().putInt(skipsLeftPrefs, skips).apply()
    }

    /**
     * Clears the skipped set of challenges
     */
    private fun resetSkippedChallenges() {
        skipsPrefs.edit().remove(skippedChallengePrefs)
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////// Testing Functions /////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    private fun printDays() {
        viewModelScope.launch {
            val days = loggedDayRepo.getLoggedDays()
            Log.d("Days", days.toString())
        }
    }

    private fun printSkips() {
        val skipsRemaining: Int = skipsPrefs.getInt(skipsLeftPrefs, 0)
        Log.d("Skips", skipsRemaining.toString())
    }

}