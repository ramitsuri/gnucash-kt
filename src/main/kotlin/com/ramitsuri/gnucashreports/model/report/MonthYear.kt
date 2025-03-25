package com.ramitsuri.gnucashreports.model.report

import com.ramitsuri.gnucashreports.utils.nowLocal
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number

data class MonthYear(
    val month: Month,
    val year: Int,
) : Comparable<MonthYear> {
    fun next(): MonthYear {
        return if (month == Month.DECEMBER) {
            MonthYear(year = year + 1, month = Month.JANUARY)
        } else {
            MonthYear(year = year, month = Month.of(month.number + 1))
        }
    }

    fun previous(): MonthYear {
        return if (month == Month.JANUARY) {
            MonthYear(year = year - 1, month = Month.DECEMBER)
        } else {
            MonthYear(year = year, month = Month.of(month.number - 1))
        }
    }

    constructor(localDateTime: LocalDateTime) : this(
        month = localDateTime.month,
        year = localDateTime.year,
    )

    fun string() = "$year-${month.number.toString().padStart(2, '0')}"

    companion object {
        fun now(
            clock: Clock = Clock.System,
            timeZone: TimeZone = TimeZone.currentSystemDefault(),
        ): MonthYear {
            return clock.nowLocal(timeZone).let { MonthYear(year = it.year, month = it.month) }
        }

    }

    override operator fun compareTo(other: MonthYear): Int {
        return if (year < other.year) {
            -1
        } else if (year > other.year) {
            1
        } else {
            if (month < other.month) {
                -1
            } else if (month > other.month) {
                1
            } else {
                1
            }
        }
    }
}
