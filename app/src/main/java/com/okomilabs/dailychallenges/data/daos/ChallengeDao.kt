package com.okomilabs.dailychallenges.data.daos

import androidx.room.Dao
import androidx.room.Query
import com.okomilabs.dailychallenges.data.entities.Challenge

@Dao
interface ChallengeDao {
    @Query("SELECT * FROM Challenge WHERE id IN (:challengeId)")
    suspend fun challengeById(challengeId: Int): List<Challenge>

    @Query("SELECT COUNT(*) FROM Challenge")
    suspend fun getTotal(): Int
}