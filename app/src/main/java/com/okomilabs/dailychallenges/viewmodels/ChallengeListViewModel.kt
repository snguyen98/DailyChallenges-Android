package com.okomilabs.dailychallenges.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.okomilabs.dailychallenges.R
import com.okomilabs.dailychallenges.data.entities.Challenge
import com.okomilabs.dailychallenges.data.entities.ChallengeListItem
import com.okomilabs.dailychallenges.data.entities.LoginDay
import com.okomilabs.dailychallenges.data.repos.ChallengeRepo
import com.okomilabs.dailychallenges.data.repos.LoginDayRepo
import com.okomilabs.dailychallenges.helpers.DateHelper
import com.okomilabs.dailychallenges.helpers.State
import kotlinx.coroutines.launch

class ChallengeListViewModel(application: Application): AndroidViewModel(application) {
    private val appContext = application.applicationContext

    private val challengeRepo: ChallengeRepo = ChallengeRepo(application)
    private val loginDayRepo: LoginDayRepo = LoginDayRepo(application)

    var cList: MutableLiveData<List<ChallengeListItem>> = MutableLiveData<List<ChallengeListItem>>()

    init {
        viewModelScope.launch {
            cList.postValue(sortList(createChallengeList(getCompletedDays())))
        }
    }

    private fun sortList(challengeList: List<ChallengeListItem>): List<ChallengeListItem> {
        return challengeList.sortedWith(compareBy({ categoryToInt(it.category) }, { it.id }))
    }

    private suspend fun createChallengeList(loginDays: List<LoginDay>): List<ChallengeListItem> {
        val idList: MutableList<Int> = mutableListOf()
        val lastCompletedList: MutableList<Int> = mutableListOf()
        val totalList: MutableList<Int> = mutableListOf()

        for (day in loginDays) {
            if (idList.contains(day.challenge)) {
                val index: Int = idList.indexOf(day.challenge)

                if (lastCompletedList[index] < day.date) {
                    lastCompletedList[index] = day.date
                }
                totalList[index]++
            }
            else {
                idList.add(day.challenge)
                lastCompletedList.add(day.date)
                totalList.add(1)
            }
        }

        val challengeList: MutableList<ChallengeListItem> = mutableListOf()

        for (index in 0 until idList.size) {
            val challenge: Challenge? = challengeRepo.challengeById(idList[index])

            if (challenge != null) {
                challengeList.add(
                    ChallengeListItem(
                        challenge.id,
                        challenge.title,
                        challenge.category,
                        DateHelper().intToDate(lastCompletedList[index]),
                        totalList[index]
                    )
                )
            }
        }

        return challengeList
    }

    private suspend fun getCompletedDays(): List<LoginDay> {
        return loginDayRepo.getLoginDaysByState(State.COMPLETE)
    }

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

}