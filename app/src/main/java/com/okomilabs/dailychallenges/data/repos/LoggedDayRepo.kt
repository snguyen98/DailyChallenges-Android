package com.okomilabs.dailychallenges.data.repos

import android.app.Application
import com.okomilabs.dailychallenges.data.daos.LoggedDayDao
import com.okomilabs.dailychallenges.data.databases.LoggedDayDatabase
import com.okomilabs.dailychallenges.data.entities.LoggedDay

class LoggedDayRepo(application: Application) {
    private val dao: LoggedDayDao = LoggedDayDatabase.getInstance(application).loggedDayDao()

    suspend fun addLoggedDay(day: LoggedDay) {
        dao.addLoggedDay(day)
    }
}