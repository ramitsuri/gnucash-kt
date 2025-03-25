package com.ramitsuri.gnucashreports.utils

import com.ramitsuri.gnucashreports.model.report.MonthYear
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

fun LocalDateTime.minus(
    period: DateTimePeriod,
): LocalDateTime {
    val timeZone = TimeZone.UTC
    return toInstant(timeZone)
        .minus(period, timeZone)
        .toLocalDateTime(timeZone)
}

fun Clock.nowLocal(
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): LocalDateTime = now().toLocalDateTime(timeZone)

fun MonthYear.toLocalDateTime(): LocalDateTime = LocalDateTime(
    year = year,
    month = month,
    dayOfMonth = 1,
    hour = 0,
    minute = 0,
    second = 0,
    nanosecond = 0,
)

fun MonthYear.minus(
    period: DateTimePeriod,
): MonthYear {
    return MonthYear(toLocalDateTime().minus(period))
}

fun LocalDateTime.isAfterOrSame(other: LocalDateTime): Boolean {
    return compareTo(other) >= 0
}
