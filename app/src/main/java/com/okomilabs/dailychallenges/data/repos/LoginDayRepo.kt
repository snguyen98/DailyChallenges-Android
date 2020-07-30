package com.okomilabs.dailychallenges.data.repos

import android.app.Application
import com.okomilabs.dailychallenges.data.daos.LoginDayDao
import com.okomilabs.dailychallenges.data.databases.LoginDayDatabase
import com.okomilabs.dailychallenges.data.entities.LoginDay

class LoginDayRepo(application: Application) {
    private val dao: LoginDayDao = LoginDayDatabase.getInstance(application).loginDayDao()

    suspend fun addLoggedDay(day: LoginDay) {
        dao.addLoggedDay(day)
    }

    suspend fun getAllLoggedDays(): List<LoginDay> {
        return dao.getAllLoggedDays()
    }

    suspend fun getLoginDayByDate(dateVal: Int): LoginDay? {
        val loggedDays: List<LoginDay> = dao.getLoggedDayByDate(dateVal)

        return if (loggedDays.isEmpty()) {
            null
        } else {
            loggedDays[0]
        }
    }
}