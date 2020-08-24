package com.okomilabs.dailychallenges.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.okomilabs.dailychallenges.data.entities.Challenge
import com.okomilabs.dailychallenges.data.entities.Link
import com.okomilabs.dailychallenges.data.repos.ChallengeRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReadMoreViewModel(application: Application, id: Int): AndroidViewModel(application) {
    private val challengeRepo: ChallengeRepo = ChallengeRepo(application)

    private val challengeId: Int = id

    var challenge: MutableLiveData<Challenge> = MutableLiveData<Challenge>()
    var links: MutableLiveData<List<Link>> = MutableLiveData<List<Link>>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            challenge.postValue(challengeRepo.challengeById(challengeId))   // Gets challenge by id
            links.postValue(challengeRepo.getLinksById(challengeId))        // Gets links by id
        }
    }
}