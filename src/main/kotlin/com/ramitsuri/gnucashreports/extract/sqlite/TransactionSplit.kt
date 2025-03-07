package com.ramitsuri.gnucashreports.extract.sqlite

import com.ramitsuri.gnucashreports.model.TransactionSplit
import java.math.BigDecimal
import java.math.BigInteger

fun TransactionSplit.Companion.getResultSetMatcher() = ResultSetMatcher(
    listOf(
        Field.TRANSACTION_GUID,
        Field.ACCOUNT_GUID,
        Field.VALUE_NUMERATOR,
        Field.VALUE_DENOMINATOR,
    ),
)

fun TransactionSplit.Companion.fromRow(row: Map<Field, String>): Pair<String, TransactionSplit> {
    var scale = 0
    var denominator = row.getOrError(Field.VALUE_DENOMINATOR).toLong()
    while (denominator >= 10) {
        scale++
        denominator /= 10
    }
    val numerator = row.getOrError(Field.VALUE_NUMERATOR)
    return row.getOrError(Field.TRANSACTION_GUID) to
        TransactionSplit(
            accountId = row.getOrError(Field.ACCOUNT_GUID),
            amount = BigDecimal(BigInteger(numerator), scale),
        )
}
