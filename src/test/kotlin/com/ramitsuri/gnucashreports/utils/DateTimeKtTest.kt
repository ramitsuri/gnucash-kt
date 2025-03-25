package com.ramitsuri.gnucashreports.utils

import com.ramitsuri.gnucashreports.model.report.MonthYear
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.LocalDateTime
import java.time.Month
import kotlin.test.Test
import kotlin.test.assertEquals

class DateTimeKtTest {
    @Test
    fun testMonthYearMinus() {
        val initial = MonthYear(month = Month.MAY, year = 2025)

        var actual = initial.minus(DateTimePeriod(years = 1))
        assertEquals(
            MonthYear(month = Month.MAY, year = 2024),
            actual,
        )

        actual = initial.minus(DateTimePeriod(years = 2))
        assertEquals(
            MonthYear(month = Month.MAY, year = 2023),
            actual,
        )

        actual = initial.minus(DateTimePeriod(months = 2))
        assertEquals(
            MonthYear(month = Month.MARCH, year = 2025),
            actual,
        )

        actual = initial.minus(DateTimePeriod(months = 5))
        assertEquals(
            MonthYear(month = Month.DECEMBER, year = 2024),
            actual,
        )

        actual = initial.minus(DateTimePeriod(days = 5))
        assertEquals(
            MonthYear(month = Month.APRIL, year = 2025),
            actual,
        )
    }

    @Test
    fun testLocalDateTimeMinus() {
        val initial = LocalDateTime.parse("2025-05-12T13:00:00")

        var actual = initial.minus(DateTimePeriod(years = 1))
        assertEquals(
            LocalDateTime.parse("2024-05-12T13:00:00"),
            actual,
        )

        actual = initial.minus(DateTimePeriod(years = 2))
        assertEquals(
            LocalDateTime.parse("2023-05-12T13:00:00"),
            actual,
        )

        actual = initial.minus(DateTimePeriod(months = 2))
        assertEquals(
            LocalDateTime.parse("2025-03-12T13:00:00"),
            actual,
        )

        actual = initial.minus(DateTimePeriod(months = 5))
        assertEquals(
            LocalDateTime.parse("2024-12-12T13:00:00"),
            actual,
        )

        actual = initial.minus(DateTimePeriod(days = 5))
        assertEquals(
            LocalDateTime.parse("2025-05-07T13:00:00"),
            actual,
        )
    }
}
