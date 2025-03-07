package com.ramitsuri.gnucashreports.extract.sqlite

import com.ramitsuri.gnucashreports.model.Transaction
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char

fun Transaction.Companion.getResultSetMatcher() = ResultSetMatcher(
    listOf(
        Field.GUID,
        Field.CURRENCY_GUID,
        Field.POST_DATE,
        Field.DESCRIPTION,
        Field.NUM,
    ),
)

fun Transaction.Companion.fromRow(row: Map<Field, String>): Transaction {
    return Transaction(
        id = row.getOrError(Field.GUID),
        splits = listOf(),
        date = LocalDateTime.parse(
            row.getOrError(Field.POST_DATE),
            LocalDateTime.Format {
                year()
                char('-')
                monthNumber(padding = Padding.ZERO)
                char('-')
                dayOfMonth(padding = Padding.ZERO)
                char(' ')
                hour(padding = Padding.ZERO)
                char(':')
                minute(padding = Padding.ZERO)
                char(':')
                second(padding = Padding.ZERO)
            },
        ).date,
        description = row.getOrError(Field.DESCRIPTION),
        num = row.getOrError(Field.NUM),
    )
}
