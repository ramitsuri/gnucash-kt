package com.ramitsuri.gnucashreports.generator

import com.ramitsuri.gnucashreports.model.Account
import com.ramitsuri.gnucashreports.model.AccountType
import com.ramitsuri.gnucashreports.model.report.Account
import com.ramitsuri.gnucashreports.model.report.Config
import com.ramitsuri.gnucashreports.model.report.MonthYear
import com.ramitsuri.gnucashreports.model.report.Report
import com.ramitsuri.gnucashreports.model.report.ReportsByYear
import com.ramitsuri.gnucashreports.utils.CumulativeDeterminer
import com.ramitsuri.gnucashreports.utils.isParentOfOrSelf
import com.ramitsuri.gnucashreports.writer.ReportWriter
import java.math.BigDecimal

class NormalReportGenerator(
    private val reportWriter: ReportWriter,
    private val cumulativeDeterminer: CumulativeDeterminer,
) {
    fun generate(
        accounts: List<Account>,
        leafAccountFullNameToTotalsMap: Map<String, Map<MonthYear, BigDecimal>>,
        reportConfig: Config.Report.Normal,
        year: Int,
    ) {
        val relevantLeafAccounts = leafAccountFullNameToTotalsMap.filter {
            reportConfig.filter.canInclude(it.key)
        }

        fun Account.getTotalsForAccount(): Map<MonthYear, BigDecimal> {
            // Find all leaf accounts that are children (or grandchildren) of the account
            // Add each month to get total of that month
            val totals = mutableMapOf<MonthYear, BigDecimal>()
            relevantLeafAccounts
                .filter { (leafAccountFullName, _) ->
                    // For ex: Expenses.isParentOfOrSelf(Expenses:Taxes:State)
                    fullName.isParentOfOrSelf(leafAccountFullName)
                }
                .forEach { (_, leafAccountTotals) ->
                    leafAccountTotals.forEach { (monthYear, total) ->
                        totals[monthYear] = (totals[monthYear] ?: BigDecimal.ZERO).plus(total)
                    }
                }
            return totals
        }

        accounts
            .filter {
                it.type == reportConfig.accountType
            }
            .mapNotNull { account ->
                Report.Account(
                    name = account.fullName,
                    monthTotals = account.getTotalsForAccount()
                        .filter { it.key.year == year }
                        .values
                        // Some account types just have negative balance but we want to show
                        // positive balance (most times) because it should be obvious from the
                        // report you're viewing that the money is leaving your wallet in that case
                        // (like liabilities)
                        .map {
                            if (reportConfig.shouldReverseSign) {
                                it.times(BigDecimal("-1"))
                            } else {
                                it
                            }
                        }
                        .toList(),
                    withCumulativeBalance = isCumulative(reportConfig.accountType),
                ).takeIf { reportAccount ->
                    reportAccount.monthTotals.isNotEmpty() &&
                        !reportAccount.monthTotals.all { it.compareTo(BigDecimal.ZERO) == 0 }
                }
            }.let { reportAccounts ->
                Report(
                    name = reportConfig.reportName,
                    accounts = reportAccounts,
                )
            }.let {
                reportWriter.write(ReportsByYear(year, listOf(it)))
            }
    }

    private fun isCumulative(accountType: AccountType): Boolean {
        return cumulativeDeterminer.isAccountTypeCumulative(accountType)
    }
}
