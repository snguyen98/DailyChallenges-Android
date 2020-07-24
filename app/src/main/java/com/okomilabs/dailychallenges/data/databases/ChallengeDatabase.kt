package com.okomilabs.dailychallenges.data.databases

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.okomilabs.dailychallenges.R
import com.okomilabs.dailychallenges.data.daos.ChallengeDao
import com.okomilabs.dailychallenges.data.entities.Challenge
import com.okomilabs.dailychallenges.data.entities.Link

@Database(entities = [Challenge::class, Link::class], version = R.integer.challenge_db_ver)
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