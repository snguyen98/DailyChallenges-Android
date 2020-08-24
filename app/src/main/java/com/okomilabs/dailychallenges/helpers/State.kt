package com.okomilabs.dailychallenges.helpers

/**
 * Class which assigns integers to possible states of a challenge for a login day
 */
class State {
    companion object {
        const val INCOMPLETE = 0
        const val COMPLETE = 1
        const val FROZEN = 2
    }
}