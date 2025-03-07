package com.ramitsuri.gnucashreports.model

import java.math.BigDecimal

data class TransactionSplit(
    val accountId: String,
    val amount: BigDecimal,
) {
    companion object
}
