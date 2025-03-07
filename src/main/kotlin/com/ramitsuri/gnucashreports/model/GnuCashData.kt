package com.ramitsuri.gnucashreports.model

data class GnuCashData(
    val accounts: List<Account>,
    val transactions: List<Transaction>,
)
