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
import com.okomilabs.dailychallenges.data.entities.LoggedDay
import com.okomilabs.dailychallenges.data.repos.ChallengeRepo
import com.okomilabs.dailychallenges.data.repos.LoggedDayRepo
import com.okomilabs.dailychallenges.helpers.DateHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException
import java.text.SimpleDateFormat
import java.util.*

class ChallengeViewModel(application: Application): AndroidViewModel(application) {
    private val challengePrefs: SharedPreferences = application.getSharedPreferences(
        R.string.challenge_key.toString(), Context.MODE_PRIVATE
    )

    private val skipsPrefs: SharedPreferences = application.getSharedPreferences(
        R.string.skips_key.toString(), Context.MODE_PRIVATE
    )

    private val listener: SharedPreferences.OnSharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                R.string.curr_title.toString() -> updateCurrent()
            }
        }

    private val challengeRepo: ChallengeRepo = ChallengeRepo(application)
    private val loggedDayRepo: LoggedDayRepo = LoggedDayRepo(application)

    private var date: String = ""
    private var challengeId: Int = -1
    private var completed: Boolean = false
    private var frozen: Boolean = false

    var title: MutableLiveData<String> = MutableLiveData<String>()
    var category: MutableLiveData<String> = MutableLiveData<String>()
    var summary: MutableLiveData<String> = MutableLiveData<String>()
    var desc: MutableLiveData<String> = MutableLiveData<String>()

    init {
        // Keeps the instance variables updated with the values in shared preferences
        challengePrefs.registerOnSharedPreferenceChangeListener(listener)

        // Doesn't refresh challenge if it's the same day
        if (isNewDay()) {

            // Checks if a new week started and resets skips
            if (isNewWeek()) {
                setSkipsRemaining(3)
                resetSkippedChallenges()
            }

            setChallengeToday()     // Gets new challenge for the day
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
     * Function that copies challenge preferences values to instance variables
     */
    private fun updateCurrent() {
        challengeId = challengePrefs.getInt(R.string.curr_id.toString(), -1)
        completed = challengePrefs.getBoolean(R.string.curr_completed.toString(), false)
        frozen = challengePrefs.getBoolean(R.string.curr_frozen.toString(), false)

        title.value = challengePrefs.getString(R.string.curr_title.toString(), "")
        category.value = challengePrefs.getString(R.string.curr_category.toString(), "")
        summary.value = challengePrefs.getString(R.string.curr_summary.toString(), "")
        desc.value = challengePrefs.getString(R.string.curr_desc.toString(), "")
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
            R.string.curr_date.toString(), null
        )

        Log.d("Day", lastLoggedIn)

        return lastLoggedIn != date
    }

    /**
     * Checks if a new week has started since last login
     *
     * @return True if today is a new week and false otherwise
     */
    private fun isNewWeek(): Boolean {
        getDateToday()      // Gets the date today first

        val lastLoggedInStr: String? = challengePrefs.getString(
            R.string.curr_date.toString(), null
        )

        with(DateHelper()) {
            // If this is the user's first login
            if (lastLoggedInStr == null) {
                return true
            }

            else {
                val lastLoggedIn = dateToInt(lastLoggedInStr)
                val today = dateToInt(date)

                return when {
                    // Comparison assumes today is the same day or after last login
                    lastLoggedIn > today -> {
                        throw IllegalArgumentException("Today cannot be before last login")
                    }

                    // Seven or more days difference implies a new week
                    today - lastLoggedIn >= 7 -> {
                        true
                    }

                    /* Reference date is a Monday so remainder represents a day of the week as a
                     * a number between 0 and 6 (with 0 as Monday and 6 as Sunday) */
                    else -> {
                        val lastLoggedInRem = lastLoggedIn % 7
                        val todayRem = today % 7

                        // If today's remainder is lower then it cannot be the same week
                        lastLoggedInRem > todayRem
                    }
                }
            }
        }
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

            val challenge: Challenge = challengeRepo.challengeById(challengeId)

            // Adds all challenge info to shared preferences
            with (challengePrefs.edit()) {
                putString(R.string.curr_date.toString(), date)
                putInt(R.string.curr_id.toString(), challengeId)
                putBoolean(R.string.curr_completed.toString(), false)                // Initially false
                putBoolean(R.string.curr_frozen.toString(), false)                  // Initially false
                putString(R.string.curr_title.toString(), challenge.title)
                putString(R.string.curr_category.toString(), challenge.category)
                putString(R.string.curr_summary.toString(), challenge.summary)

                if (challenge.desc != null) {
                    putString(R.string.curr_desc.toString(), challenge.desc)        // Can be null
                }
                else {
                    putString(R.string.curr_desc.toString(), null)
                }
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
        val excluded: MutableSet<Int> = mutableSetOf()

        // Removes the skipped challenges
        challengePrefs.getStringSet(
            R.string.skipped_challenges.toString(), null
        )?.forEach { challenge -> excluded.add(challenge.toInt()) }

        challengeId = challenges.minus(excluded).random()
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////// Button Functions /////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Function to call private function setChallengeToday
     */
    fun refreshChallenge() {
        setChallengeToday()
    }

    /**
     * Sets today's challenge as complete in the view model, shared preferences and logged day db
     */
    fun markComplete() {
        challengePrefs.edit().putBoolean(R.string.curr_completed.toString(), true).apply()
        completed = true
        addLoggedDay()
    }

    /**
     * Sets today's challenge as frozen in the view model, shared preferences and logged day db
     */
    fun freezeDay() {
        challengePrefs.edit().putBoolean(R.string.curr_frozen.toString(), true).apply()
        frozen = true
        addLoggedDay()
    }

    /**
     * Adds current challenge to the skipped set and sets a new challenge for today
     */
    fun skipChallenge() {
        val skipsRemaining: Int = skipsPrefs.getInt(R.string.skips_remaining.toString(), 0)

        if (skipsRemaining > 0) {
            setSkipsRemaining(skipsRemaining - 1)
            setSkippedChallenges(generateSkippedSet())
            setChallengeToday()
        }
    }

    /**
     * Gets the remaining skips allowed
     */
    fun getSkips(): Int {
        return skipsPrefs.getInt(R.string.skips_remaining.toString(), 0)
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////// Skip Helper Functions //////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Sets the skipped set of challenges to shared preferences
     *
     * @param skipped The set of challenges to be added
     */
    private fun setSkippedChallenges(skipped: Set<String>?) {
        skipsPrefs.edit().putStringSet(R.string.skipped_challenges.toString(), skipped).apply()
    }

    /**
     * Sets the number of skips remaining to shared preferences
     *
     * @param skips The number of skips to be set
     */
    private fun setSkipsRemaining(skips: Int) {
        skipsPrefs.edit().putInt(R.string.skips_remaining.toString(), skips).apply()
    }

    /**
     * Clears the skipped set of challenges
     */
    private fun resetSkippedChallenges() {
        setSkippedChallenges(null)
    }

    /**
     * Combines the current challenge with the existing skipped challenges
     *
     * @return The resulting set of challenges
     */
    private fun generateSkippedSet(): Set<String> {
        val currentSkipped: MutableSet<String>? = skipsPrefs.getStringSet(
            R.string.skipped_challenges.toString(), null
        )
        return if (currentSkipped == null) {
            setOf(challengeId.toString())
        } else {
            currentSkipped.add(challengeId.toString())
            currentSkipped
        }
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
        val skipsRemaining: Int = skipsPrefs.getInt(R.string.skips_remaining.toString(), 0)
        Log.d("Skips", skipsRemaining.toString())
    }

}