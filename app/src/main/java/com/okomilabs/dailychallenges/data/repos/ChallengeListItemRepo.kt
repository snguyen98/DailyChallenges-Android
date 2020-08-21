package com.okomilabs.dailychallenges.data.repos

import android.app.Application
import com.okomilabs.dailychallenges.data.daos.ChallengeListItemDao
import com.okomilabs.dailychallenges.data.databases.ChallengeListItemDatabase
import com.okomilabs.dailychallenges.data.entities.ChallengeListItem

class ChallengeListItemRepo(application: Application) {
    private val dao: ChallengeListItemDao = ChallengeListItemDatabase
        .getInstance(application)
        .challengeListItemDao()

    suspend fun addListItem(item: ChallengeListItem) {
        dao.addListItem(item)
    }

    suspend fun getAllItems(): List<ChallengeListItem> {
        return dao.getAllItems()
    }

    suspend fun getListItemById(challengeId: Int): ChallengeListItem? {
        val listItems: List<ChallengeListItem> = dao.getListItemById(challengeId)

        return if (listItems.isNotEmpty()) {
            listItems[0]
        }
        else {
            null
        }
    }

    suspend fun deleteAll() {
        dao.deleteAll()
    }
}