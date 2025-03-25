package com.ramitsuri.gnucashreports.model

data class Account(
    val id: String,
    val name: String,
    val fullName: String,
    val type: AccountType,
    val parentId: String,
) {
    companion object
}
