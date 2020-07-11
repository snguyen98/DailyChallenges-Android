package com.okomilabs.dailychallenges.helpers

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * A class to convert between dates and ints for easy database storage
 */
class DateHelper {
    private val format: SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
    private val refDate: String = "01/06/2020"      // Monday
    private val reference: Date = format.parse(refDate) ?: Date()

    /**
     * Converts a date to an integer by counting the days from the reference
     *
     * @param date The date to be converted into an integer
     * @return The number of days from the reference date as an integer
     */
    fun dateToInt(date: String): Int {
        val formattedDate: Date = format.parse(date) ?: return -1

        return TimeUnit.DAYS.convert(
            formattedDate.time - reference.time,
            TimeUnit.MILLISECONDS
        ).toInt()
    }

    /**
     * Converts an integer to a date by using it to calculate the date by days from the reference
     *
     * @param days The number of days to be added to the reference
     * @return The date as a triple in the form (dd,MM,yyyy)
     */
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
}