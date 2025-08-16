package com.ramitsuri.gnucashreports.model.report

import com.ramitsuri.gnucashreports.utils.BigDecimalSerializer
import java.math.BigDecimal
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Report(

    @SerialName("name")
    val name: String,

    @SerialName("with_cumulative_balance")
    val withCumulativeBalance: Boolean,

    // Rows
    @SerialName("accounts")
    val accounts: List<Account>,
) {
    @Serializable
    data class Account(
        @SerialName("name")
        val name: String,

        // Order in which this account should be displayed. It's there so that client can sort these
        // accounts and build a new report if needed with maybe different month years in there.
        @SerialName("order")
        val order: Int,

        // Columns
        @SerialName("month_totals")
        val monthTotals: Map<
            MonthYear,
            @Serializable(BigDecimalSerializer::class)
            BigDecimal,
            >,
    )
}
