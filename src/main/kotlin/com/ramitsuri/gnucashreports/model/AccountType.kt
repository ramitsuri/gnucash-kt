package com.ramitsuri.gnucashreports.model

enum class AccountType {
    ROOT,
    ASSET,
    INCOME,
    EXPENSE,
    LIABILITY,
    EQUITY,
    ;

    companion object {
        fun from(value: String): AccountType {
            return when (value) {
                "ROOT" -> ROOT

                "ASSET",
                "BANK",
                "CASH",
                -> ASSET

                "INCOME" -> INCOME

                "EXPENSE" -> EXPENSE

                "CREDIT",
                "LIABILITY",
                -> LIABILITY

                "EQUITY" -> EQUITY

                else -> {
                    error("Unknown account type: $value")
                }
            }
        }
    }
}
