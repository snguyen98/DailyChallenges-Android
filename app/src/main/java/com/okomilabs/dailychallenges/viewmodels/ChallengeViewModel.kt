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
import com.okomilabs.dailychallenges.data.entities.LoginDay
import com.okomilabs.dailychallenges.data.repos.ChallengeRepo
import com.okomilabs.dailychallenges.data.repos.LoginDayRepo
import com.okomilabs.dailychallenges.helpers.DateHelper
import com.okomilabs.dailychallenges.helpers.State
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ChallengeViewModel(application: Application): AndroidViewModel(application) {
    // Room database repositories
    private val challengeRepo: ChallengeRepo = ChallengeRepo(application)
    private val loginDayRepo: LoginDayRepo = LoginDayRepo(application)

    // Shared preferences references
    private val challengePrefs: SharedPreferences = application.getSharedPreferences(
        application.getString(R.string.challenge_key), Context.MODE_PRIVATE
    )

    private val statsPrefs: SharedPreferences = application.getSharedPreferences(
        application.getString(R.string.stats_key), Context.MODE_PRIVATE
    )

    private val resourcesPrefs: SharedPreferences = application.getSharedPreferences(
        application.getString(R.string.resources_key), Context.MODE_PRIVATE
    )

    // Shared preferences strings
    private val lastLoginPrefs: String = application.getString(R.string.last_login)
    private val idPrefs: String = application.getString(R.string.curr_id)
    private val streakPrefs: String = application.getString(R.string.streak_value)
    private val skippedChallengePrefs: String = application.getString(R.string.skipped_challenge)
    private val skipsLeftPrefs: String = application.getString(R.string.skips_remaining)
    private val freezesLeftPrefs: String = application.getString(R.string.freezes_remaining)

    // Instance variables
    private var date: String = ""
    private var loginDay: MutableLiveData<LoginDay> = MutableLiveData<LoginDay>()
    var challenge: MutableLiveData<Challenge> = MutableLiveData<Challenge>()

    init {
        initialise()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////// Setting Functions /////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Performs checks and sets data when the fragment is created or refreshed
     */
    fun initialise() {
        // Doesn't refresh challenge if it's the same day
        if (isNewDay()) {
            checkStreak()           // Checks if the streak was kept last login
            refreshChallenge()      // Gets a new challenge
        }
        // Gets the challenge and login day info from shared preferences
        else {
            viewModelScope.launch {
                updateChallenge()
                updateLoginDay()
            }
        }
    }

    /**
     * Function that updates the challenge id with the value in shared preferences and retrieves
     * the challenge info from the database
     */
    private suspend fun updateChallenge() {
        val id = challengePrefs.getInt(idPrefs, -1)

        if (id == -1) {
            Log.d("Challenge ID", "Challenge could not be found in shared preferences")
        }

        challenge.postValue(challengeRepo.challengeById(id))
    }

    /**
     * Function that updates the login day variable with the login info for today in the database
     */
    private suspend fun updateLoginDay() {
        loginDay.postValue(loginDayRepo.getLoginDayByDate(DateHelper().dateToInt(date)))
    }

    /**
     * Chooses a challenge for today and sets the details into shared preferences
     */
    private fun setChallengeToday() {
        viewModelScope.launch(Dispatchers.IO) {
            val total: Int = challengeRepo.getTotal()
            val id: Int = chooseRandomChallenge(total)                // Sets challenge ID instance variable

            // Adds all challenge info to shared preferences
            with (challengePrefs.edit()) {
                putString(lastLoginPrefs, date)
                putInt(idPrefs, id)
                apply()
            }

            updateChallenge()
            addLoggedDay(State.INCOMPLETE)
        }
    }

    /**
     * Chooses a random challenge from the database excluding those that were skipped today
     *
     * @param total The total number of challenges in the database
     */
    private fun chooseRandomChallenge(total: Int): Int {
        val challenges: MutableSet<Int> = (1..total).toMutableSet()

        val id: Int = challengePrefs.getInt(idPrefs, -1)
        val skipped: Int = resourcesPrefs.getInt(skippedChallengePrefs, -1)

        // Prevents the user getting the same challenge as last time
        if (id != -1) {
            challenges.remove(id)
        }
        if (skipped != -1) {
            challenges.remove(skipped)      // Removes skipped challenge if the user skipped today
        }
        else {
            setSkippedChallenge(id)    // Otherwise sets skipped challenge as current one
        }

        return challenges.random()
    }

    /**
     * Function that adds today's challenge to the logged day database
     *
     * @param state The state of the challenge: 0 - Incomplete, 1 - Complete, 2 - Frozen
     */
    private fun addLoggedDay(state: Int) {
        viewModelScope.launch {
            val dateInt: Int = DateHelper().dateToInt(date)       // Date to be stored as an int

            loginDayRepo.addLoggedDay(
                LoginDay(
                    dateInt,
                    challenge.value?.id ?: -1,
                    state
                )
            )
            updateLoginDay()
        }
    }

    /**
     * Function to call private function setChallengeToday
     */
    fun refreshChallenge() {
        // Resets skips remaining and skipped challenges
        setSkipsRemaining(2)
        resetSkippedChallenges()

        setChallengeToday()     // Gets new challenge for the day
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
        val lastLogin: String? = challengePrefs.getString(lastLoginPrefs, null)
        getDateToday()      // Sets date instance variable to the date today

        return lastLogin != date
    }

    /**
     * Sets the date instance variable to today in the form dd/MM/yyyy
     */
    private fun getDateToday() {
        date = SimpleDateFormat("dd/MM/yyyy").format(Date())
    }

    /**
     * Takes a date in the form dd/MM/yyyy and returns the month value
     *
     * @return The month in the form MM
     */
    private fun getMonthFromString(dateString: String): String {
        return dateString.substring(3, 4)
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////// Complete Functions ////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Sets today's challenge as complete in the view model, shared preferences and logged day db
     */
    fun markComplete() {
        addLoggedDay(State.COMPLETE)
        setStreak(statsPrefs.getInt(streakPrefs, -1) + 1)   // Increments the streak

        if (getStreak() % 7 == 0) {
            setFreezesRemaining(resourcesPrefs.getInt(freezesLeftPrefs, 0) + 1)
        }
    }

    /**
     * Checks if the current challenge is complete
     */
    fun isComplete(): Boolean {
        return loginDay.value?.state ?: -1 == State.COMPLETE
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////// Streak Functions /////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Checks if the streak was broken last time they logged in
     */
    private fun checkStreak() {
        val lastLogin: String? = challengePrefs.getString(lastLoginPrefs, null)

        if (lastLogin != null) {
            val todayInt: Int = DateHelper().dateToInt(date)
            val lastLoginInt: Int = DateHelper().dateToInt(lastLogin)

            viewModelScope.launch {
                // Can't be null if the date exists in shared preferences
                val lastLoginDay: LoginDay? = loginDayRepo.getLoginDayByDate(lastLoginInt)

                // Check if challenge wasn't completed last time or the last login wasn't yesterday
                if ((lastLoginDay?.state == State.INCOMPLETE) || (todayInt - lastLoginInt != 1)) {
                    streakBroken()
                }
            }
        }
        else {
            setStreak(0)        // Sets streak to zero when user launches app for the first time
        }
    }

    /**
     * To be called when the streak has been broken
     */
    private fun streakBroken() {
        setStreak(0)                // Resets streak to zero
        setFreezesRemaining(0)      // Resets freezes to zero
        // Store best streak when stats page is made //
    }

    /**
     * Gets the current streak
     */
    fun getStreak(): Int {
        return statsPrefs.getInt(streakPrefs, 0)
    }

    /**
     * Sets the current streak to shared preferences
     */
    private fun setStreak(streak: Int) {
        statsPrefs.edit().putInt(streakPrefs, streak).apply()
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////// Skip Functions //////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Adds current challenge to the skipped set and sets a new challenge for today
     */
    fun skipChallenge() {
        val skipsRemaining: Int = resourcesPrefs.getInt(skipsLeftPrefs, 0)

        if (skipsRemaining > 0) {
            setSkipsRemaining(skipsRemaining - 1)
            setChallengeToday()
        }
    }

    /**
     * Gets the remaining skips allowed
     */
    fun getSkips(): Int {
        return resourcesPrefs.getInt(skipsLeftPrefs, 0)
    }

    /**
     * Sets the skipped set of challenges to shared preferences
     *
     * @param skipped The set of challenges to be added
     */
    private fun setSkippedChallenge(skipped: Int) {
        resourcesPrefs.edit().putInt(skippedChallengePrefs, skipped).apply()
    }

    /**
     * Sets the number of skips remaining to shared preferences
     *
     * @param skips The number of skips to be set
     */
    private fun setSkipsRemaining(skips: Int) {
        resourcesPrefs.edit().putInt(skipsLeftPrefs, skips).apply()
    }

    /**
     * Clears the skipped set of challenges
     */
    private fun resetSkippedChallenges() {
        resourcesPrefs.edit().remove(skippedChallengePrefs).apply()
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////// Freeze Functions /////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Sets today's challenge as frozen in the view model, shared preferences and logged day db
     */
    fun freezeDay() {
        val freezesRemaining: Int = resourcesPrefs.getInt(freezesLeftPrefs, 0)

        if (freezesRemaining > 0) {
            setFreezesRemaining(freezesRemaining - 1)
            addLoggedDay(State.FROZEN)
        }
    }

    /**
     * Gets the remaining freezes allowed
     */
    fun getFreezes(): Int {
        return resourcesPrefs.getInt(freezesLeftPrefs, 0)
    }

    /**
     * Checks if the current challenge is frozen
     */
    fun isFrozen(): Boolean {
        return loginDay.value?.state ?: -1 == State.FROZEN
    }

    /**
     * Resets the number of freezes available to 2
     */
    private fun setFreezesRemaining(freezes: Int) {
        resourcesPrefs.edit().putInt(freezesLeftPrefs, freezes).apply()
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////// Debugging Functions ////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    private fun printDays() {
        viewModelScope.launch {
            val days = loginDayRepo.getAllLoggedDays()
            Log.d("Days", days.toString())
        }
    }

    private fun printStreak() {
        val streak: Int = statsPrefs.getInt(streakPrefs, 0)
        Log.d("Streak", streak.toString())
    }

    private fun printSkips() {
        val skipsRemaining: Int = resourcesPrefs.getInt(skipsLeftPrefs, 0)
        Log.d("Skips", skipsRemaining.toString())
    }

}