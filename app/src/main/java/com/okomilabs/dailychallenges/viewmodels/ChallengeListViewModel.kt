package com.okomilabs.dailychallenges.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.okomilabs.dailychallenges.R
import com.okomilabs.dailychallenges.data.entities.ChallengeListItem
import com.okomilabs.dailychallenges.data.repos.ChallengeListItemRepo
import com.okomilabs.dailychallenges.data.repos.LoginDayRepo
import kotlinx.coroutines.launch

class ChallengeListViewModel(application: Application): AndroidViewModel(application) {
    private val appContext = application.applicationContext

    private val loginDayRepo: LoginDayRepo = LoginDayRepo(application)
    private val challengeListItemRepo: ChallengeListItemRepo = ChallengeListItemRepo(application)

    var cList: MutableLiveData<List<ChallengeListItem>> = MutableLiveData<List<ChallengeListItem>>()

    init {
        viewModelScope.launch {
            cList.postValue(sortList(challengeListItemRepo.getAllItems()))
        }
    }

    /**
     * Sorts the challenge list by category and then by challenge id
     *
     * @param challengeList The list of challenges to be sorted
     * @return The sorted challenge list
     */
    private fun sortList(challengeList: List<ChallengeListItem>): List<ChallengeListItem> {
        return challengeList.sortedWith(compareBy({ categoryToInt(it.category) }, { it.id }))
    }

    /**
     * Assigns an integer to each category to assist in reordering
     *
     * @param category The category to be converted
     * @return The corresponding integer
     */
    private fun categoryToInt(category: String): Int {
        when (category) {
            appContext.getString(R.string.physical_wellbeing) -> return 1
            appContext.getString(R.string.mental_wellbeing) -> return 2
            appContext.getString(R.string.socialising) -> return 3
            appContext.getString(R.string.education_learning) -> return 4
            appContext.getString(R.string.skills_hobbies) -> return 5
        }

        return -1
    }

    /**
     * Deletes all user data in room and shared preferences except settings
     */
    fun resetData() {
        // Deletes all stored login days
        viewModelScope.launch {
            loginDayRepo.deleteAll()
            challengeListItemRepo.deleteAll()
        }

        // Clears shared preferences
        appContext.getSharedPreferences(
            appContext.getString(R.string.challenge_key), Context.MODE_PRIVATE
        ).edit().clear().apply()

        appContext.getSharedPreferences(
            appContext.getString(R.string.stats_key), Context.MODE_PRIVATE
        ).edit().clear().apply()

        appContext.getSharedPreferences(
            appContext.getString(R.string.resources_key), Context.MODE_PRIVATE
        ).edit().clear().apply()
    }

}