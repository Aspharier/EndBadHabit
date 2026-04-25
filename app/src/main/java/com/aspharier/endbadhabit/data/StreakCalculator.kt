package com.aspharier.endbadhabit.data

import java.time.LocalDate
import java.time.temporal.ChronoUnit

object StreakCalculator {

    /**
     * Calculate the current streak in days from the start date to today.
     */
    fun calculateStreak(startDate: LocalDate): Long {
        val today = LocalDate.now()
        return ChronoUnit.DAYS.between(startDate, today)
    }

    /**
     * Check if the streak is broken.
     * A streak is broken if the user hasn't opened the app since yesterday
     * (i.e., lastOpenedDate is before yesterday).
     */
    fun isStreakBroken(lastOpenedDate: LocalDate): Boolean {
        val yesterday = LocalDate.now().minusDays(1)
        return lastOpenedDate.isBefore(yesterday)
    }
}
