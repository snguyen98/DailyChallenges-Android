package com.okomilabs.dailychallenges.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.okomilabs.dailychallenges.data.entities.LoginDay

@Dao
interface LoginDayDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addLoggedDay(day: LoginDay)

    @Query("SELECT * FROM LoginDay")
    suspend fun getAllLoggedDays(): List<LoginDay>

    @Query("SELECT * FROM LoginDay WHERE date IN (:dateVal)")
    suspend fun getLoggedDayByDate(dateVal: Int): List<LoginDay>
}
