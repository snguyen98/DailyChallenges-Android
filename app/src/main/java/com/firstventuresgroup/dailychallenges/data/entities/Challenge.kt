package com.firstventuresgroup.dailychallenges.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Challenge(
    @PrimaryKey val id: Int,
    val title: String,
    val category: String,
    val summary: String,
    val desc: String?
)