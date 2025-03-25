package com.ramitsuri.gnucashreports.model

import com.ramitsuri.gnucashreports.utils.BigDecimalSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.math.BigDecimal

@Serializable
data class TransactionSplit(
    // We don't really need to send this to clients that's why transient
    @Transient
    val accountId: String = "",

    @SerialName("account_name")
    val accountName: String = "",

    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("amount")
    val amount: BigDecimal,
) {
    companion object
}
