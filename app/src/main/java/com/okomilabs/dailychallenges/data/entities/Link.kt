package com.okomilabs.dailychallenges.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Link (
    @PrimaryKey val id: Int,
    val title: String,
    val link: String,
    val challenge: Int
)