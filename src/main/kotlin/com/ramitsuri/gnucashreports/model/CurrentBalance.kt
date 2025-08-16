package com.ramitsuri.gnucashreports.model

import com.ramitsuri.gnucashreports.utils.BigDecimalSerializer
import java.math.BigDecimal
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CurrentBalance(
    @SerialName("name")
    val name: String,

    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("balance")
    val balance: BigDecimal,

    @SerialName("group_name")
    val groupName: String,
)
