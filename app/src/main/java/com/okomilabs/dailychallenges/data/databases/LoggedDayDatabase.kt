package com.okomilabs.dailychallenges.data.databases

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.okomilabs.dailychallenges.data.daos.LoggedDayDao
import com.okomilabs.dailychallenges.data.entities.LoggedDay

@Database(entities = [LoggedDay::class], version = 1)
abstract class LoggedDayDatabase: RoomDatabase() {
    abstract fun loggedDayDao(): LoggedDayDao

    companion object {
        @Volatile
        private var INSTANCE: LoggedDayDatabase? = null

        @Synchronized
        fun getInstance(context: Context): LoggedDayDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    LoggedDayDatabase::class.java,
                    "LoggedDays.db"
                ).build()
            }
            return INSTANCE!!
        }
    }
}