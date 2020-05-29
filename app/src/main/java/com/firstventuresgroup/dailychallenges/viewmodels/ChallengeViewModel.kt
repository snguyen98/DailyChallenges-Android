package com.firstventuresgroup.dailychallenges.viewmodels

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.firstventuresgroup.dailychallenges.R
import com.firstventuresgroup.dailychallenges.data.entities.Challenge
import com.firstventuresgroup.dailychallenges.data.repos.ChallengeRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ChallengeViewModel(application: Application) : AndroidViewModel(application) {
    private val challengePrefs: SharedPreferences = application.getSharedPreferences(
        R.string.challenge_key.toString(), Context.MODE_PRIVATE
    )

    private val listener: SharedPreferences.OnSharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                R.string.curr_title.toString() -> updateCurrent()
            }
        }

    private val repo: ChallengeRepo = ChallengeRepo(application)

    var title: MutableLiveData<String> = MutableLiveData<String>()
    var category: MutableLiveData<String> = MutableLiveData<String>()
    var summary: MutableLiveData<String> = MutableLiveData<String>()
    var desc: MutableLiveData<String> = MutableLiveData<String>()

    init {
        if (getDateToday() != challengePrefs.getString(
                R.string.curr_date.toString(), null)
        ) {
            setChallengeToday()
        }

        updateCurrent()
        challengePrefs.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun onCleared() {
        challengePrefs.unregisterOnSharedPreferenceChangeListener(listener)
    }

    private fun updateCurrent() {
        title.value = challengePrefs.getString(R.string.curr_title.toString(), "")
        category.value = challengePrefs.getString(R.string.curr_category.toString(), "")
        summary.value = challengePrefs.getString(R.string.curr_summary.toString(), "")
        desc.value = challengePrefs.getString(R.string.curr_desc.toString(), "")
    }

    private fun setChallengeToday() {
        viewModelScope.launch(Dispatchers.IO) {
            val total: Int = repo.getTotal()
            val challenge: Challenge = repo.challengeById(chooseRandomChallenge(total))

            with (challengePrefs.edit()) {
                putString(R.string.curr_date.toString(), getDateToday())
                putString(R.string.curr_title.toString(), challenge.title)
                putString(R.string.curr_category.toString(), challenge.category)
                putString(R.string.curr_summary.toString(), challenge.summary)

                if (challenge.desc != null) {
                    putString(R.string.curr_desc.toString(), challenge.summary)
                }
                else {
                    putString(R.string.curr_desc.toString(), null)
                }

                apply()
            }
        }
    }

    private fun chooseRandomChallenge(total: Int): Int {
        return (1..total).random()
    }

    private fun getDateToday(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy")
        return dateFormat.format(Date())
    }

}