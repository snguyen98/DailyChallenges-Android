package com.okomilabs.dailychallenges.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.okomilabs.dailychallenges.R
import com.okomilabs.dailychallenges.data.entities.Challenge
import com.okomilabs.dailychallenges.data.entities.ChallengeListItem
import com.okomilabs.dailychallenges.data.entities.LoginDay
import com.okomilabs.dailychallenges.data.repos.ChallengeListItemRepo
import com.okomilabs.dailychallenges.data.repos.ChallengeRepo
import com.okomilabs.dailychallenges.data.repos.LoginDayRepo
import com.okomilabs.dailychallenges.helpers.DateHelper
import com.okomilabs.dailychallenges.helpers.State
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ChallengeViewModel(application: Application): AndroidViewModel(application) {
    private val appContext = application.applicationContext

    // Room database repositories
    private val challengeRepo: ChallengeRepo = ChallengeRepo(application)
    private val loginDayRepo: LoginDayRepo = LoginDayRepo(application)
    private val challengeListItemRepo: ChallengeListItemRepo = ChallengeListItemRepo(application)

    // Shared preferences keys
    private val challengeKey: String = appContext.getString(R.string.challenge_key)
    private val statsKey: String = appContext.getString(R.string.stats_key)
    private val resourcesKey: String = appContext.getString(R.string.resources_key)

    // Shared preferences strings
    private val lastLoginPrefs: String = appContext.getString(R.string.last_login)
    private val idPrefs: String = appContext.getString(R.string.curr_id)
    private val streakPrefs: String = appContext.getString(R.string.streak_value)
    private val skipChallengesPrefs: String = appContext.getString(R.string.skip_challenges)
    private val numSkippedPrefs: String = appContext.getString(R.string.num_skipped)
    private val skipsLeftPrefs: String = appContext.getString(R.string.skips_remaining)
    private val freezesLeftPrefs: String = appContext.getString(R.string.freezes_remaining)
    private val shownFreezePrefs: String = appContext.getString(R.string.shown_freeze_msg)

    // Instance variables
    private var date: String = ""
    var loginDay: MutableLiveData<LoginDay> = MutableLiveData<LoginDay>()
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
    private fun initialise() {
        // Doesn't refresh challenge if it's the same day
        if (isNewDay()) {
            checkStreak()
            refreshChallenge()
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
        val id = appContext
            .getSharedPreferences(challengeKey, Context.MODE_PRIVATE)
            .getInt(idPrefs, -1)

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
            val id: Int = chooseRandomChallenge(total)

            // Adds all challenge info to shared preferences
            with (appContext.getSharedPreferences(challengeKey, Context.MODE_PRIVATE).edit()) {
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
     * @return The chosen challenge
     */
    private fun chooseRandomChallenge(total: Int): Int {
        val challenges: MutableSet<Int> = (1..total).toMutableSet()

        // Takes the last assigned challenge and adds it to the skipped set
        addSkipChallenge(
            appContext
                .getSharedPreferences(challengeKey, Context.MODE_PRIVATE)
                .getInt(idPrefs, -1),
            appContext
                .getSharedPreferences(resourcesKey, Context.MODE_PRIVATE)
                .getInt(numSkippedPrefs, 0) + 1
        )

        challenges.minus(getSkipChallenges())

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
     * Function to reset values when a new day starts
     */
    private fun refreshChallenge() {
        // Resets skips remaining and skipped challenges
        setSkipsRemaining(2)
        resetSkipChallenges()

        // Gets new challenge for the day
        setChallengeToday()
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
        val lastLogin: String? = appContext
            .getSharedPreferences(challengeKey, Context.MODE_PRIVATE)
            .getString(lastLoginPrefs, null)

        getDateToday()      // Sets date instance variable to the date today

        return lastLogin != date
    }

    /**
     * Sets the date instance variable to today in the form dd/MM/yyyy
     */
    private fun getDateToday() {
        date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////// Complete Functions ////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Sets today's challenge as complete in the view model, shared preferences and logged day db
     */
    fun markComplete() {
        addLoggedDay(State.COMPLETE)        // Logs the challenge completion today
        updateListItem()                    // Updates the last completed and total completed

        // Increment the streak
        setStreak(appContext
            .getSharedPreferences(statsKey, Context.MODE_PRIVATE)
            .getInt(streakPrefs, -1) + 1)   // Increments the streak

        // If five consecutive challenges in a row are completed then add a freeze
        if (getStreak() % 5 == 0) {
            setFreezesRemaining(
                appContext
                    .getSharedPreferences(resourcesKey, Context.MODE_PRIVATE)
                    .getInt(freezesLeftPrefs, 0) + 1
            )
        }
    }

    /**
     * Adds or updates the challenge list item data for the completed challenge
     */
    private fun updateListItem() {
        val id: Int = challenge.value?.id ?: -1

        viewModelScope.launch {
            val item: ChallengeListItem? = challengeListItemRepo.getListItemById(id)
            var total = 1

            // Increment the total if it has been initialised
            if (item != null) {
                total = item.totalCompleted + 1
            }

            // Add item to the db
            challengeListItemRepo.addListItem(
                ChallengeListItem(
                    id,
                    challenge.value?.title ?: "",
                    challenge.value?.category ?: "",
                    date,
                    total
                )
            )
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////// Streak Functions /////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Checks if the streak was broken last time they logged in
     */
    private fun checkStreak() {
        val lastLogin: String? = appContext
            .getSharedPreferences(challengeKey, Context.MODE_PRIVATE)
            .getString(lastLoginPrefs, null)

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
     *
     * @return The streak value
     */
    fun getStreak(): Int {
        return appContext
            .getSharedPreferences(statsKey, Context.MODE_PRIVATE)
            .getInt(streakPrefs, 0)
    }

    /**
     * Sets the current streak to shared preferences
     *
     * @param streak The current streak value
     */
    private fun setStreak(streak: Int) {
        appContext
            .getSharedPreferences(statsKey, Context.MODE_PRIVATE)
            .edit()
            .putInt(streakPrefs, streak)
            .apply()
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////// Skip Functions //////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Adds current challenge to the skipped set and sets a new challenge for today
     */
    fun skipChallenge() {
        val skipsRemaining: Int = appContext
            .getSharedPreferences(resourcesKey, Context.MODE_PRIVATE)
            .getInt(skipsLeftPrefs, 0)

        // If there are skips remaining then decrement the skips
        if (skipsRemaining > 0) {
            setSkipsRemaining(skipsRemaining - 1)
            setChallengeToday()
        }
    }

    /**
     * Gets the remaining skips allowed
     *
     * @return The skips remaining
     */
    fun getSkips(): Int {
        return appContext
            .getSharedPreferences(resourcesKey, Context.MODE_PRIVATE)
            .getInt(skipsLeftPrefs, 0)
    }

    /**
     * Sets the number of skips remaining to shared preferences
     *
     * @param skips The number of skips to be set
     */
    private fun setSkipsRemaining(skips: Int) {
        appContext
            .getSharedPreferences(resourcesKey, Context.MODE_PRIVATE)
            .edit()
            .putInt(skipsLeftPrefs, skips)
            .apply()
    }

    /**
     * Adds a challenge id at an index to the skipped challenges and increases the number of skipped
     *
     * @param challenge The challenge id of the skipped challenge to be added
     * @param index The index in the skipped set to add the challenge to
     */
    private fun addSkipChallenge(challenge: Int, index: Int) {
        appContext.getSharedPreferences(resourcesKey, Context.MODE_PRIVATE)
            .edit()
            .putInt(skipChallengesPrefs + index, challenge)
            .putInt(numSkippedPrefs, index)
            .apply()
    }

    /**
     * Returns the set of challenges which should be skipped from shared preferences
     *
     * @return The set of challenges to be skipped
     */
    private fun getSkipChallenges(): MutableSet<Int> {
        val skipSet: MutableSet<Int> = mutableSetOf()

        for (index in 1..3) {
            with (appContext.getSharedPreferences(resourcesKey, Context.MODE_PRIVATE)) {
                skipSet.add(getInt(skipChallengesPrefs + index, -1))
            }
        }

        return skipSet
    }

    /**
     * Clears the set of skipped challenges and the number of skipped challenges in shared prefs
     */
    private fun resetSkipChallenges() {
        with (appContext.getSharedPreferences(resourcesKey, Context.MODE_PRIVATE).edit()) {
            for (index in 1..3) {
                remove(skipChallengesPrefs + index)
            }

            putInt(numSkippedPrefs, 0)
            apply()
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////// Freeze Functions /////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Sets today's challenge as frozen in the view model, shared preferences and logged day db
     */
    fun freezeDay() {
        val freezesRemaining: Int = appContext
            .getSharedPreferences(resourcesKey, Context.MODE_PRIVATE)
            .getInt(freezesLeftPrefs, 0)

        if (freezesRemaining > 0) {
            setFreezesRemaining(freezesRemaining - 1)
            addLoggedDay(State.FROZEN)
        }
    }

    /**
     * Gets the remaining freezes allowed
     *
     * @return The freezes remaining
     */
    fun getFreezes(): Int {
        return appContext
            .getSharedPreferences(resourcesKey, Context.MODE_PRIVATE)
            .getInt(freezesLeftPrefs, 0)
    }

    /**
     * Checks if the user gained a freeze for completing their last challenge and ensures the freeze
     * message is only shown once
     *
     * @return True if freeze message should be shown and false otherwise
     */
    fun showFreezeMsg(): Boolean {
        // If there are non-zero freezes and the streak is a multiple of seven then show the msg
        val show: Boolean =
            appContext
                .getSharedPreferences(resourcesKey, Context.MODE_PRIVATE)
                .getInt(freezesLeftPrefs, 0) != 0 &&
            appContext
                .getSharedPreferences(statsKey, Context.MODE_PRIVATE)
                .getInt(streakPrefs, 0) % 5 == 0

        return if (show) {
            // Only shows the message once in the day
            if (
                !appContext
                    .getSharedPreferences(resourcesKey, Context.MODE_PRIVATE)
                    .getBoolean(shownFreezePrefs, false)
            ) {
                // Sets shown pref to true
                appContext
                    .getSharedPreferences(resourcesKey, Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean(shownFreezePrefs, true)
                    .apply()
                true
            }
            else {
                false
            }
        }

        else {
            // Keeps show pref as false
            appContext
                .getSharedPreferences(resourcesKey, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(shownFreezePrefs, false)
                .apply()
            false
        }
    }

    /**
     * Sets the number of freezes available
     *
     * @param freezes The number of freezes to be set
     */
    private fun setFreezesRemaining(freezes: Int) {
        appContext
            .getSharedPreferences(resourcesKey, Context.MODE_PRIVATE)
            .edit()
            .putInt(freezesLeftPrefs, freezes)
            .apply()
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////// Debugging Functions ////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    private fun printDays() {
        viewModelScope.launch {
            val days = loginDayRepo.getAllLoggedDays()
            Log.d("Days", "Logged Days: $days")
        }
    }

    private fun printStreak() {
        val streak: Int = appContext
            .getSharedPreferences(statsKey, Context.MODE_PRIVATE)
            .getInt(streakPrefs, 0)

        Log.d("Streak", "Streak: $streak")
    }

    private fun printSkips() {
        val skipsRemaining: Int = appContext
            .getSharedPreferences(resourcesKey, Context.MODE_PRIVATE)
            .getInt(skipsLeftPrefs, 0)

        with (appContext.getSharedPreferences(resourcesKey, Context.MODE_PRIVATE)) {
            for (index in 1..3) {
                Log.d(
                    "Skips",
                    "Skip Challenge: " + getInt(skipChallengesPrefs + index, -1).toString()
                )
            }
        }

        Log.d("Skips", "Skips Remaining: $skipsRemaining")
    }

}