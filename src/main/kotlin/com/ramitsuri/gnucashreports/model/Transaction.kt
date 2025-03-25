package com.ramitsuri.gnucashreports.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    @SerialName("id")
    val id: String,

    @SerialName("splits")
    val splits: List<TransactionSplit>,

    @SerialName("date")
    val date: LocalDate,

    @SerialName("description")
    val description: String,

    @SerialName("num")
    val num: String,
) {
    companion object
}
