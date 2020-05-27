package com.firstventuresgroup.dailychallenges.data.daos

import androidx.room.Dao
import androidx.room.Query
import com.firstventuresgroup.dailychallenges.data.entities.Challenge

@Dao
interface ChallengeDao {
    @Query("SELECT * FROM Challenge WHERE id IN (:challengeId)")
    suspend fun findById(challengeId: Int): List<Challenge>
}