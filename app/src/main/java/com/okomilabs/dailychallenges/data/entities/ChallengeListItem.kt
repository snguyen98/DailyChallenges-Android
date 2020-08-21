package com.okomilabs.dailychallenges.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ChallengeListItem(
    @PrimaryKey val id: Int,
    val title: String,
    val category: String,
    val lastCompleted: String,
    val totalCompleted: Int
)