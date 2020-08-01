package com.okomilabs.dailychallenges.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LoginDay(
    @PrimaryKey val date: Int,
    val challenge: Int,
    val state: Int
)