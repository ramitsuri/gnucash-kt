package com.ramitsuri.gnucashreports.model.report

import com.ramitsuri.gnucashreports.model.report.Report.Account.WithCumulativeBalance
import com.ramitsuri.gnucashreports.model.report.Report.Account.WithoutCumulativeBalance
import com.ramitsuri.gnucashreports.utils.BigDecimalSerializer
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class Report(

    @SerialName("name")
    val name: String,

    // Rows
    @SerialName("accounts")
    val accounts: List<Account>,
) {
    @Serializable
    sealed interface Account {
        val name: String
        val monthTotals: Map<MonthYear, @Serializable(BigDecimalSerializer::class) BigDecimal>

        @Serializable
        @SerialName("with_cumulative_balance")
        data class WithCumulativeBalance(
            @SerialName("name")
            override val name: String,

            // Columns
            @SerialName("month_totals")
            override val monthTotals: Map<MonthYear, @Serializable(BigDecimalSerializer::class) BigDecimal>,
        ) : Account

        @Serializable
        @SerialName("without_cumulative_balance")
        data class WithoutCumulativeBalance(
            @SerialName("name")
            override val name: String,

            // Columns
            @SerialName("month_totals")
            override val monthTotals: Map<MonthYear, @Serializable(BigDecimalSerializer::class) BigDecimal>,
        ) : Account {

            @OptIn(ExperimentalSerializationApi::class)
            @Serializable(BigDecimalSerializer::class)
            @SerialName("total")
            @EncodeDefault(EncodeDefault.Mode.ALWAYS)
            val total: BigDecimal = monthTotals.values.sumOf { it }
        }

        companion object
    }
}

fun Report.Companion.Account(
    name: String,
    monthTotals: Map<MonthYear, BigDecimal>,
    withCumulativeBalance: Boolean,
) = if (withCumulativeBalance) {
    WithCumulativeBalance(
        name = name,
        monthTotals = monthTotals,
    )
} else {
    WithoutCumulativeBalance(
        name = name,
        monthTotals = monthTotals,
    )
}
