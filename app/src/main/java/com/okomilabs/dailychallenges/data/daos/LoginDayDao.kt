package com.okomilabs.dailychallenges.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.okomilabs.dailychallenges.data.entities.LoginDay

@Dao
interface LoginDayDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addLoginDay(day: LoginDay)

    @Query("SELECT * FROM LoginDay")
    suspend fun getAllLoginDays(): List<LoginDay>

    @Query("SELECT * FROM LoginDay WHERE state IN (:state)")
    suspend fun getLoginDaysByState(state: Int): List<LoginDay>

    @Query("SELECT * FROM LoginDay WHERE date IN (:dateVal)")
    suspend fun getLoginDayByDate(dateVal: Int): List<LoginDay>

    @Query("DELETE FROM LoginDay")
    suspend fun deleteAll()

}
