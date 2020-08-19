package com.okomilabs.dailychallenges.data.entities

data class ChallengeListItem(
    val id: Int,
    val title: String,
    val category: String,
    val lastCompleted: String,
    val totalCompleted: Int
)