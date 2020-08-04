package com.okomilabs.dailychallenges.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.okomilabs.dailychallenges.R
import com.okomilabs.dailychallenges.data.entities.Challenge
import com.okomilabs.dailychallenges.data.entities.Link
import com.okomilabs.dailychallenges.data.repos.ChallengeRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReadMoreViewModel(application: Application): AndroidViewModel(application) {
    private val challengeRepo: ChallengeRepo = ChallengeRepo(application)

    private val challengeId: Int = application.applicationContext
        .getSharedPreferences(
            application.applicationContext.getString(R.string.challenge_key),
            Context.MODE_PRIVATE
        )
        .getInt(application.getString(R.string.curr_id), -1)

    var challenge: MutableLiveData<Challenge> = MutableLiveData<Challenge>()
    var links: MutableLiveData<List<Link>> = MutableLiveData<List<Link>>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            challenge.postValue(challengeRepo.challengeById(challengeId))
            links.postValue(challengeRepo.getLinksById(challengeId))
        }
    }
}