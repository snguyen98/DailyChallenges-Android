package com.okomilabs.dailychallenges.data.repos

import android.app.Application
import com.okomilabs.dailychallenges.data.daos.ChallengeDao
import com.okomilabs.dailychallenges.data.databases.ChallengeDatabase
import com.okomilabs.dailychallenges.data.entities.Challenge

class ChallengeRepo(application: Application) {
    private val dao: ChallengeDao = ChallengeDatabase.getInstance(application).challengeDao()

    suspend fun challengeById(id: Int): Challenge {
        return dao.challengeById(id)[0]
    }

    suspend fun getTotal(): Int {
        return dao.getTotal()
    }

}