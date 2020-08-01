package com.okomilabs.dailychallenges.data.databases

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.okomilabs.dailychallenges.data.daos.LoginDayDao
import com.okomilabs.dailychallenges.data.entities.LoginDay

@Database(entities = [LoginDay::class], version = 1)
abstract class LoginDayDatabase: RoomDatabase() {
    abstract fun loginDayDao(): LoginDayDao

    companion object {
        @Volatile
        private var INSTANCE: LoginDayDatabase? = null

        @Synchronized
        fun getInstance(context: Context): LoginDayDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    LoginDayDatabase::class.java,
                    "LoginDays.db"
                ).build()
            }
            return INSTANCE!!
        }
    }
}