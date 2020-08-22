package com.okomilabs.dailychallenges.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.okomilabs.dailychallenges.data.entities.ChallengeListItem

@Dao
interface ChallengeListItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addListItem(item: ChallengeListItem)

    @Query("SELECT * FROM ChallengeListItem")
    suspend fun getAllItems(): List<ChallengeListItem>

    @Query("SELECT * FROM ChallengeListItem WHERE id IN (:challengeId)")
    suspend fun getListItemById(challengeId: Int): List<ChallengeListItem>

    @Query("DELETE FROM ChallengeListItem")
    suspend fun deleteAll()
}