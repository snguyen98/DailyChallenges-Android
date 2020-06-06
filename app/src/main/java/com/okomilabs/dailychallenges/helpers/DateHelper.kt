package com.okomilabs.dailychallenges.helpers

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class DateHelper(refDate: String) {
    private val format: SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
    private val reference: Date = format.parse(refDate) ?: Date()

    fun intToDate(days: Int): Triple<Int,Int,Int> {
        val calendar: Calendar = Calendar.getInstance()
        calendar.time = reference
        calendar.add(Calendar.DATE, days)
        return Triple(
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.YEAR)
        )
    }

    fun dateToInt(date: Date): Int {
        return TimeUnit.DAYS.convert(
            date.time - reference.time,
            TimeUnit.MILLISECONDS
        ).toInt()
    }
}