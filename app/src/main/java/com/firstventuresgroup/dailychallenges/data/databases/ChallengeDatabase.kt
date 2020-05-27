package com.firstventuresgroup.dailychallenges.data.databases

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.firstventuresgroup.dailychallenges.R
import com.firstventuresgroup.dailychallenges.data.daos.ChallengeDao
import com.firstventuresgroup.dailychallenges.data.entities.Challenge

@Database(entities = [Challenge::class], version = R.integer.challenge_db_ver)
abstract class ChallengeDatabase: RoomDatabase() {
    abstract fun challengeDao(): ChallengeDao

    companion object {
        @Volatile
        private var INSTANCE: ChallengeDatabase? = null

        @Synchronized
        fun getInstance(context: Context): ChallengeDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    ChallengeDatabase::class.java,
                    "Challenges.db"
                )
                    .createFromAsset("database/Challenges.db")
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return INSTANCE!!
        }
    }
}