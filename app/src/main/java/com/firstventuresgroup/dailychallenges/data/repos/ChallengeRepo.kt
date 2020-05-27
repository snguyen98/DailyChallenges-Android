package com.firstventuresgroup.dailychallenges.data.repos

import android.app.Application
import com.firstventuresgroup.dailychallenges.data.daos.ChallengeDao
import com.firstventuresgroup.dailychallenges.data.databases.ChallengeDatabase
import com.firstventuresgroup.dailychallenges.data.entities.Challenge

class ChallengeRepo(application: Application) {
    private val dao: ChallengeDao = ChallengeDatabase.getInstance(application).challengeDao()

    suspend fun challengeById(id: Int): Challenge {
        return dao.findById(id)[0]
    }

}