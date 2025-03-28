package com.ramitsuri.gnucashreports.generator

import com.ramitsuri.gnucashreports.model.report.MonthYear
import com.ramitsuri.gnucashreports.model.report.Report
import com.ramitsuri.gnucashreports.model.report.ReportsByYear
import com.ramitsuri.gnucashreports.writer.ReportWriter
import java.math.BigDecimal

class NetWorthReportGenerator(
    private val reportWriter: ReportWriter,
) {
    fun generate(
        leafAccountFullNameToTotalsMap: Map<String, Map<MonthYear, BigDecimal>>,
        assetsRootAccount: String,
        liabilitiesRootAccount: String,
        year: Int,
    ) {
        val assetTotals = mutableMapOf<MonthYear, BigDecimal>()
        leafAccountFullNameToTotalsMap
            .filter {
                it.key.startsWith(assetsRootAccount.plus(":"))
            }
            .forEach { (_, leafAccountTotals) ->
                leafAccountTotals.forEach { (monthYear, total) ->
                    assetTotals[monthYear] = (assetTotals[monthYear] ?: BigDecimal.ZERO).plus(total)
                }
            }
        val liabilitiesTotals = mutableMapOf<MonthYear, BigDecimal>()
        leafAccountFullNameToTotalsMap
            .filter {
                it.key.startsWith(liabilitiesRootAccount.plus(":"))
            }
            .forEach { (_, leafAccountTotals) ->
                leafAccountTotals.forEach { (monthYear, total) ->
                    liabilitiesTotals[monthYear] =
                        (liabilitiesTotals[monthYear] ?: BigDecimal.ZERO).plus(total)
                }
            }
        val netWorthTotals = assetTotals.map { (monthYear, assetTotal) ->
            // Liabilities are already negative, so add here instead of subtracting
            val netWorthTotal = assetTotal.plus(liabilitiesTotals[monthYear] ?: BigDecimal.ZERO)
            monthYear to netWorthTotal
        }.associate { it }

        val account = Report.Account(
            name = "NetWorth",
            order = 0,
            monthTotals = netWorthTotals
                .filter { it.key.year == year },
        )
        reportWriter.write(
            ReportsByYear(
                year,
                listOf(
                    Report(
                        name = "NetWorth",
                        withCumulativeBalance = true,
                        accounts = listOf(account),
                    ),
                ),
            ),
        )
    }
}
