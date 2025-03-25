package com.ramitsuri.gnucashreports.writer

import com.ramitsuri.gnucashreports.model.report.TransactionsByMonthYear

interface TransactionsWriter {
    fun write(transactions: TransactionsByMonthYear)
}
