package com.ramitsuri.gnucashreports.model

import kotlinx.datetime.LocalDate

data class Transaction(
    val id: String,
    val splits: List<TransactionSplit>,
    val date: LocalDate,
    val description: String,
    val num: String,
) {
    companion object
}
