package com.okomilabs.dailychallenges.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.okomilabs.dailychallenges.data.entities.LoggedDay

@Dao
interface LoggedDayDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addLoggedDay(day: LoggedDay)
}