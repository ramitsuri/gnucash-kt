package com.ramitsuri.gnucashreports.generator

import com.ramitsuri.gnucashreports.model.report.Config
import com.ramitsuri.gnucashreports.model.report.MonthYear
import com.ramitsuri.gnucashreports.model.report.Report
import com.ramitsuri.gnucashreports.model.report.ReportsByYear
import com.ramitsuri.gnucashreports.utils.isParentOfOrSelf
import com.ramitsuri.gnucashreports.writer.ReportWriter
import java.math.BigDecimal

class SavingsRateReportGenerator(
    private val reportWriter: ReportWriter,
) {
    fun generate(
        leafAccountFullNameToTotalsMap: Map<String, Map<MonthYear, BigDecimal>>,
        expensesWithoutTaxesFilter: Config.Report.Filter,
        taxesFilter: Config.Report.Filter,
        incomeFilter: Config.Report.Filter,
        incomeRootAccount: String,
        expensesRootAccount: String,
        year: Int,
    ) {
        fun getTotalsForAccount(
            relevantLeafAccounts: Map<String, Map<MonthYear, BigDecimal>>,
        ): Map<MonthYear, BigDecimal> {
            // Add each month to get total of that month
            val totals = mutableMapOf<MonthYear, BigDecimal>()
            relevantLeafAccounts
                .forEach { (_, leafAccountTotals) ->
                    leafAccountTotals.forEach { (monthYear, total) ->
                        totals[monthYear] = (totals[monthYear] ?: BigDecimal.ZERO).plus(total)
                    }
                }
            return totals
        }

        val relevantIncomeLeafAccounts = leafAccountFullNameToTotalsMap.filter {
            incomeRootAccount.isParentOfOrSelf(it.key) &&
                incomeFilter.canInclude(it.key)
        }
        val incomeAccount = Report.Account(
            name = "Income",
            order = 0,
            monthTotals = getTotalsForAccount(relevantIncomeLeafAccounts)
                .filter { it.key.year == year }
                // Income is generally negative on GnuCash but we want the positive value in this context
                .mapValues { it.value.negate() },
        )

        val relevantTaxesLeafAccounts = leafAccountFullNameToTotalsMap.filter {
            expensesRootAccount.isParentOfOrSelf(it.key) &&
                taxesFilter.canInclude(it.key)
        }
        val taxesAccount = Report.Account(
            name = "Taxes",
            order = 1,
            monthTotals = getTotalsForAccount(relevantTaxesLeafAccounts)
                .filter { it.key.year == year },
        )

        val relevantExpensesLeafAccounts = leafAccountFullNameToTotalsMap.filter {
            expensesRootAccount.isParentOfOrSelf(it.key) &&
                expensesWithoutTaxesFilter.canInclude(it.key)
        }
        val expensesWithoutTaxesAccount = Report.Account(
            name = "Expenses",
            order = 2,
            monthTotals = getTotalsForAccount(relevantExpensesLeafAccounts)
                .filter { it.key.year == year },
        )

        Report(
            name = "Savings Rate",
            withCumulativeBalance = false,
            accounts = listOf(incomeAccount, taxesAccount, expensesWithoutTaxesAccount),
        ).let {
            reportWriter.write(ReportsByYear(year, listOf(it)))
        }
    }
}
