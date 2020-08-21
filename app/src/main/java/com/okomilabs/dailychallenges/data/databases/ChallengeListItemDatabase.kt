package com.okomilabs.dailychallenges.data.databases

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.okomilabs.dailychallenges.data.daos.ChallengeListItemDao
import com.okomilabs.dailychallenges.data.entities.ChallengeListItem

@Database(entities = [ChallengeListItem::class], version = 1)
abstract class ChallengeListItemDatabase: RoomDatabase() {
    abstract fun challengeListItemDao(): ChallengeListItemDao

    companion object {
        @Volatile
        private var INSTANCE: ChallengeListItemDatabase? = null

        @Synchronized
        fun getInstance(context: Context): ChallengeListItemDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    ChallengeListItemDatabase::class.java,
                    "ChallengeListItems.db"
                ).build()
            }
            return INSTANCE!!
        }
    }
}