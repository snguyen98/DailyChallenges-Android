package com.okomilabs.dailychallenges.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LoggedDay(
    @PrimaryKey val date: Int,
    val challenge: Int,
    val completed: Boolean,
    val skipped: Boolean
)