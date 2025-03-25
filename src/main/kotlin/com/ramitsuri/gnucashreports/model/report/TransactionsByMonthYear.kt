package com.ramitsuri.gnucashreports.model.report

import com.ramitsuri.gnucashreports.model.Transaction

data class TransactionsByMonthYear(
    val monthYear: MonthYear,
    val transactions: List<Transaction>,
)
