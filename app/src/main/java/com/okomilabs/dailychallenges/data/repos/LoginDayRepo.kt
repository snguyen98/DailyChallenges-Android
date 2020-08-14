package com.okomilabs.dailychallenges.data.repos

import android.app.Application
import com.okomilabs.dailychallenges.data.daos.LoginDayDao
import com.okomilabs.dailychallenges.data.databases.LoginDayDatabase
import com.okomilabs.dailychallenges.data.entities.LoginDay

class LoginDayRepo(application: Application) {
    private val dao: LoginDayDao = LoginDayDatabase.getInstance(application).loginDayDao()

    suspend fun addLoggedDay(day: LoginDay) {
        dao.addLoginDay(day)
    }

    suspend fun getAllLoggedDays(): List<LoginDay> {
        return dao.getAllLoginDays()
    }

    suspend fun getLoginDaysByState(state: Int): List<LoginDay> {
        return dao.getLoginDaysByState(state)
    }

    suspend fun getLoginDayByDate(dateVal: Int): LoginDay? {
        val loginDays: List<LoginDay> = dao.getLoginDayByDate(dateVal)

        return if (loginDays.isEmpty()) {
            null
        } else {
            loginDays[0]
        }
    }
}