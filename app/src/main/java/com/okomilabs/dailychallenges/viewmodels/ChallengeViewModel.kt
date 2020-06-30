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
import java.text.SimpleDateFormat
import java.util.*

class ChallengeViewModel(application: Application) : AndroidViewModel(application) {
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
    var title: MutableLiveData<String> = MutableLiveData<String>()
    var category: MutableLiveData<String> = MutableLiveData<String>()
    var summary: MutableLiveData<String> = MutableLiveData<String>()
    var desc: MutableLiveData<String> = MutableLiveData<String>()

    init {
        updateCurrent()
        getDateToday()

        challengePrefs.registerOnSharedPreferenceChangeListener(listener)

        if (date != challengePrefs.getString(
                R.string.curr_date.toString(), null)
        ) {
            if (isMondayToday()) {
                setSkipsRemaining(3)
            }
            resetSkippedChallenges()
            setChallengeToday()
        }
    }

    override fun onCleared() {
        challengePrefs.unregisterOnSharedPreferenceChangeListener(listener)
    }

    fun skipChallenge() {
        val skipsRemaining: Int = skipsPrefs.getInt(R.string.skips_remaining.toString(), 0)
        if (skipsRemaining > 0) {
            setSkipsRemaining(skipsRemaining - 1)
            setSkippedChallenges(generateSkippedSet())
            setChallengeToday()
        }
    }

    fun markComplete() {
        addLoggedDay(completed = true, frozen = false)
    }

    // TEMPORARY FUNCTION
    private fun testPrint() {
        viewModelScope.launch {
            val days = loggedDayRepo.getLoggedDays()
            Log.d("Days", days.toString())
        }
    }

    fun freezeDay() {
        addLoggedDay(completed = false, frozen = true)
    }

    private fun addLoggedDay(completed: Boolean, frozen: Boolean) {
        viewModelScope.launch {
            val dateInt: Int = DateHelper().dateToInt(date)
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

    private fun updateCurrent() {
        challengeId = challengePrefs.getInt(R.string.curr_id.toString(), -1)
        title.value = challengePrefs.getString(R.string.curr_title.toString(), "")
        category.value = challengePrefs.getString(R.string.curr_category.toString(), "")
        summary.value = challengePrefs.getString(R.string.curr_summary.toString(), "")
        desc.value = challengePrefs.getString(R.string.curr_desc.toString(), "")
    }

    private fun resetSkippedChallenges() {
        setSkippedChallenges(null)
    }

    private fun setChallengeToday() {
        viewModelScope.launch(Dispatchers.IO) {
            val total: Int = challengeRepo.getTotal()

            chooseRandomChallenge(total)        // Sets challenge ID
            addLoggedDay(completed = false, frozen = false)

            val challenge: Challenge = challengeRepo.challengeById(challengeId)

            with (challengePrefs.edit()) {
                putString(R.string.curr_date.toString(), date)
                putInt(R.string.curr_id.toString(), challengeId)
                putString(R.string.curr_title.toString(), challenge.title)
                putString(R.string.curr_category.toString(), challenge.category)
                putString(R.string.curr_summary.toString(), challenge.summary)

                if (challenge.desc != null) {
                    putString(R.string.curr_desc.toString(), challenge.desc)
                }
                else {
                    putString(R.string.curr_desc.toString(), null)
                }
                apply()
            }
        }
    }

    private fun setSkippedChallenges(skipped: Set<String>?) {
        skipsPrefs.edit().putStringSet(R.string.skipped_challenges.toString(), skipped).apply()
    }

    private fun setSkipsRemaining(skips: Int) {
        skipsPrefs.edit().putInt(R.string.skips_remaining.toString(), skips).apply()
    }

    private fun generateSkippedSet(): Set<String> {
        val currentSkipped: MutableSet<String>? = challengePrefs.getStringSet(
            R.string.skipped_challenges.toString(), null
        )
        return if (currentSkipped == null) {
            setOf(challengeId.toString())
        } else {
            currentSkipped.add(challengeId.toString())
            currentSkipped
        }
    }

    private fun chooseRandomChallenge(total: Int) {
        val challenges: MutableSet<Int> = (1..total).toMutableSet()
        val excluded: MutableSet<Int> = mutableSetOf()

        challengePrefs.getStringSet(
            R.string.skipped_challenges.toString(), null
        )?.forEach { challenge -> excluded.add(challenge.toInt()) }

        challengeId = challenges.minus(excluded).random()
    }

    private fun getDateToday() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy")
        date = dateFormat.format(Date())
    }

    private fun isMondayToday(): Boolean {
        val today = Calendar.getInstance()
        return today.get(Calendar.DAY_OF_WEEK) == 1
    }

}