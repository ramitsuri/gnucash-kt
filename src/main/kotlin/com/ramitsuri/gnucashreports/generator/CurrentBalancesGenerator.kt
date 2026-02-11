package com.ramitsuri.gnucashreports.generator

import com.ramitsuri.gnucashreports.model.CurrentBalance
import com.ramitsuri.gnucashreports.model.Transaction
import com.ramitsuri.gnucashreports.model.report.Config
import com.ramitsuri.gnucashreports.model.report.MonthYear
import com.ramitsuri.gnucashreports.utils.CumulativeDeterminer
import com.ramitsuri.gnucashreports.utils.isParentOfOrSelf
import com.ramitsuri.gnucashreports.utils.nowLocal
import com.ramitsuri.gnucashreports.writer.CurrentBalancesWriter
import java.math.BigDecimal
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus

class CurrentBalancesGenerator(
    private val writer: CurrentBalancesWriter,
    private val cumulativeDeterminer: CumulativeDeterminer,
    private val clock: Clock = Clock.System,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) {

    fun generate(
        txGroupConfig: Config.TxGroupConfig,
        transactions: List<Transaction>,
        accountBalancesConfig: List<Config.AccountBalance>,
        leafAccountFullNameToCumulativeTotalsMap: Map<String, Map<MonthYear, BigDecimal>>,
        leafAccountFullNameToWithoutCumulativeTotalsMap: Map<String, Map<MonthYear, BigDecimal>>,
    ) {
        (
            generateTxGroups(
                config = txGroupConfig,
                transactions = transactions,
            ) + generateAccountBalances(
                accountBalancesConfig = accountBalancesConfig,
                leafAccountFullNameToCumulativeTotalsMap =
                leafAccountFullNameToCumulativeTotalsMap,
                leafAccountFullNameToWithoutCumulativeTotalsMap =
                leafAccountFullNameToWithoutCumulativeTotalsMap,
            )
            ).let {
            writer.write(it)
        }
    }

    private fun generateTxGroups(
        config: Config.TxGroupConfig,
        transactions: List<Transaction>,
    ): List<CurrentBalance> {
        val txGroupsToBalanceDateMap = mutableMapOf<String, TxGroupBalanceDate>()
        transactions.forEach { tx ->
            if (tx.num.isEmpty()) {
                return@forEach
            }
            val currentBalanceDate = txGroupsToBalanceDateMap[tx.num] ?: TxGroupBalanceDate()
            txGroupsToBalanceDateMap[tx.num] = currentBalanceDate.update(tx)
        }
        val today = clock.nowLocal(timeZone).date
        val daysInPast = DatePeriod(days = config.daysInPastToInclude)
        val oldestTxDateToInclude = today.minus(daysInPast)
        return txGroupsToBalanceDateMap
            .filterValues { balanceDate ->
                balanceDate.mostRecentTxDate >= oldestTxDateToInclude
            }
            .mapNotNull { (identifier, txGroupBalanceDate) ->
                val txGroup = config
                    .txGroups
                    .find { it.identifier == identifier }
                CurrentBalance(
                    name = txGroup?.displayName ?: identifier,
                    // Absolute value because it could be negative, but it's understood that
                    // this money went away so it's positive for displaying
                    balance = txGroupBalanceDate.balance.abs(),
                    groupName = txGroup?.groupName ?: "Tx Group",
                )
            }
    }

    private data class TxGroupBalanceDate(
        val balance: BigDecimal = BigDecimal.ZERO,
        val mostRecentTxDate: LocalDate = LocalDate(1970, 1, 1),
    ) {
        // Creates a copy adding balance of transaction split (one whose amount is negative) with
        // current balance and date of transaction if it's newer than current date otherwise
        // current date
        fun update(tx: Transaction): TxGroupBalanceDate {
            val txAmount = tx.splits.first { it.amount < BigDecimal.ZERO }.amount
            return TxGroupBalanceDate(
                balance = balance + txAmount,
                mostRecentTxDate = maxOf(tx.date, mostRecentTxDate),
            )
        }
    }

    private fun generateAccountBalances(
        accountBalancesConfig: List<Config.AccountBalance>,
        leafAccountFullNameToCumulativeTotalsMap: Map<String, Map<MonthYear, BigDecimal>>,
        leafAccountFullNameToWithoutCumulativeTotalsMap: Map<String, Map<MonthYear, BigDecimal>>,
    ): List<CurrentBalance> {
        val currentMonthYear = MonthYear.now(clock, timeZone)
        return accountBalancesConfig
            .map { accountBalanceConfig ->
                val balance = accountBalanceConfig
                    .accountNames
                    .map { accountName ->
                        when (accountBalanceConfig.balanceType) {
                            Config.AccountBalance.BalanceType.CURRENT_MONTH -> {
                                if (isCumulative(accountBalanceConfig.accountNames)) {
                                    leafAccountFullNameToCumulativeTotalsMap
                                } else {
                                    leafAccountFullNameToWithoutCumulativeTotalsMap
                                }.filter { (leafAccountFullName, _) ->
                                    // Find relevant leaf accounts
                                    accountName.isParentOfOrSelf(leafAccountFullName)
                                }.map { (_, totals) ->
                                    // Get relevant leaf account's current MonthYear total
                                    totals[currentMonthYear] ?: BigDecimal.ZERO
                                }
                            }

                            Config.AccountBalance.BalanceType.CURRENT_YEAR -> {
                                leafAccountFullNameToWithoutCumulativeTotalsMap
                                    .filter { (leafAccountFullName, _) ->
                                        // Find relevant leaf accounts
                                        accountName.isParentOfOrSelf(leafAccountFullName)
                                    }
                                    .map { (_, totals) ->
                                        // Get relevant leaf account's totals for current year
                                        // And add them
                                        totals.filterKeys { monthYear ->
                                            monthYear.year == currentMonthYear.year
                                        }.values.sumOf { it }
                                    }
                            }
                        }
                    }
                    .flatten()
                    .sumOf { it }
                    // Additional context about money left this account or was added to it is
                    // provided by the name of the balance
                    .abs()
                CurrentBalance(
                    name = accountBalanceConfig.displayName,
                    balance = balance,
                    groupName = accountBalanceConfig.groupName,
                )
            }
    }

    private fun isCumulative(accountNames: List<String>): Boolean {
        if (accountNames.isEmpty()) {
            error("No accounts provided")
        }
        accountNames
            .map {
                cumulativeDeterminer.isAccountCumulative(it)
            }
            .let { areAccountsCumulative ->
                // Only if all accounts are cumulative or non cumulative, it's a valid result
                if (areAccountsCumulative.all { it } || areAccountsCumulative.all { !it }) {
                    return areAccountsCumulative.first()
                }
                error(
                    "All accounts must be cumulative or not cumulative - " +
                        accountNames.joinToString(","),
                )
            }
    }
}
