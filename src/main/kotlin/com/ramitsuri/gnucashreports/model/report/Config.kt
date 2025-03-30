package com.ramitsuri.gnucashreports.model.report

import com.ramitsuri.gnucashreports.model.AccountType
import com.ramitsuri.gnucashreports.utils.isParentOfOrSelf
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Config(
    @SerialName("file")
    val file: String,

    @SerialName("assets_root_account")
    val assetsRootAccount: String,

    @SerialName("expense_root_account")
    val expenseRootAccount: String,

    @SerialName("income_root_account")
    val incomeRootAccount: String,

    @SerialName("liabilities_root_account")
    val liabilitiesRootAccount: String,

    @SerialName("reports")
    val reports: List<Report>,

    @SerialName("recent_transactions_only")
    val recentTransactionsOnly: Boolean,

    @SerialName("tx_groups")
    val txGroups: List<TxGroup>,

    @SerialName("account_balances")
    val accountBalances: List<AccountBalance>,
) {
    @Serializable
    sealed interface Report {

        @Serializable
        @SerialName("net_worth")
        data object NetWorth : Report

        @Serializable
        @SerialName("savings_rate")
        data class SavingsRate(
            @SerialName("income_filter")
            val incomeFilter: Filter,

            @SerialName("taxes_filter")
            val taxesFilter: Filter,

            @SerialName("expenses_filter")
            val expensesFilter: Filter,
        ) : Report

        @Serializable
        @SerialName("normal")
        data class Normal(
            @SerialName("account_type")
            val accountType: AccountType,

            @SerialName("report_name")
            val reportName: String,

            @SerialName("with_cumulative_balance")
            val withCumulativeBalance: Boolean? = null,

            @SerialName("filter")
            val filter: Filter = Filter.None,

            @SerialName("should_reverse_sign")
            val shouldReverseSign: Boolean = false,
        ) : Report

        @Serializable
        sealed interface Filter {

            @Serializable
            @SerialName("exclude")
            data class Exclude(
                @SerialName("account_names")
                val excludedAccountNames: List<String>,
            ) : Filter

            @Serializable
            @SerialName("include")
            data class Include(
                @SerialName("account_names")
                val includedAccountNames: List<String>,
            ) : Filter

            @Serializable
            @SerialName("none")
            data object None : Filter

            // See tests for examples
            fun canInclude(accountFullName: String): Boolean {
                return when (this) {
                    is Exclude -> {
                        excludedAccountNames.none { it.isParentOfOrSelf(accountFullName) }
                    }

                    is Include -> {
                        includedAccountNames.any { it.isParentOfOrSelf(accountFullName) }
                    }

                    is None -> true
                }
            }
        }
    }

    @Serializable
    data class TxGroup(
        @SerialName("identifier")
        val identifier: String,

        @SerialName("display_name")
        val displayName: String,

        @SerialName("valid_until")
        val validUntil: LocalDateTime,

        @SerialName("group_name")
        val groupName: String,
    )

    @Serializable
    data class AccountBalance(
        @SerialName("account_names")
        val accountNames: List<String>,

        @SerialName("display_name")
        val displayName: String,

        @SerialName("group_name")
        val groupName: String,

        @SerialName("balance")
        val balanceType: BalanceType,
    ) {

        enum class BalanceType {
            CURRENT_MONTH,
            CURRENT_YEAR,
        }
    }
}
