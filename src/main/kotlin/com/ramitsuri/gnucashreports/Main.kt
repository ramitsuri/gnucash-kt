package com.ramitsuri.gnucashreports

import com.ramitsuri.gnucashreports.extract.sqlite.SqliteExtractor
import com.ramitsuri.gnucashreports.generator.CurrentBalancesGenerator
import com.ramitsuri.gnucashreports.generator.NetWorthReportGenerator
import com.ramitsuri.gnucashreports.generator.NormalReportGenerator
import com.ramitsuri.gnucashreports.generator.SavingsRateReportGenerator
import com.ramitsuri.gnucashreports.model.Account
import com.ramitsuri.gnucashreports.model.report.Config
import com.ramitsuri.gnucashreports.model.report.MonthYear
import com.ramitsuri.gnucashreports.model.report.TransactionsByMonthYear
import com.ramitsuri.gnucashreports.utils.CumulativeDeterminer
import com.ramitsuri.gnucashreports.utils.minus
import com.ramitsuri.gnucashreports.writer.TransactionsWriter
import com.ramitsuri.gnucashreports.writer.file.FileCurrentBalancesWriter
import com.ramitsuri.gnucashreports.writer.file.FileReportWriter
import com.ramitsuri.gnucashreports.writer.file.FileTransactionsWriter
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.serialization.json.Json
import java.io.File
import java.math.BigDecimal

fun main() {
    val json = getJson()
    val config = File("input/config.json")
        .readText()
        .let { Json.decodeFromString<Config>(it) }
    val cumulativeDeterminer = CumulativeDeterminer(
        expensesRootAccount = config.expenseRootAccount,
        incomeRootAccount = config.incomeRootAccount,
        assetsRootAccount = config.assetsRootAccount,
        liabilitiesRootAccount = config.liabilitiesRootAccount,
    )
    run(
        config = config,
        normalReportGenerator = NormalReportGenerator(
            reportWriter = FileReportWriter(
                basePath = "output/reports",
                json = json,
            ),
            cumulativeDeterminer = cumulativeDeterminer,
        ),
        netWorthReportGenerator = NetWorthReportGenerator(
            reportWriter = FileReportWriter(
                basePath = "output/reports",
                json = json,
            ),
        ),
        savingsRateReportGenerator = SavingsRateReportGenerator(
            reportWriter = FileReportWriter(
                basePath = "output/reports",
                json = json,
            ),
        ),
        transactionsWriter = FileTransactionsWriter(
            basePath = "output/transactions",
            json = json,
        ),
        currentBalancesGenerator = CurrentBalancesGenerator(
            writer = FileCurrentBalancesWriter(
                basePath = "output/current_balances",
                json = json,
            ),
            cumulativeDeterminer = cumulativeDeterminer,
        ),
        cumulativeDeterminer = cumulativeDeterminer,
    )
}

private fun getJson() = Json {
    prettyPrint = true
    allowStructuredMapKeys = true
}

private fun run(
    config: Config,
    transactionsWriter: TransactionsWriter,
    normalReportGenerator: NormalReportGenerator,
    netWorthReportGenerator: NetWorthReportGenerator,
    savingsRateReportGenerator: SavingsRateReportGenerator,
    currentBalancesGenerator: CurrentBalancesGenerator,
    cumulativeDeterminer: CumulativeDeterminer,
) {
    val since = getStartingMonthYear(recent = config.recentTransactionsOnly)
    SqliteExtractor()
        .extract(since = since, gnuCashFile = File(config.file))
        .let { result ->
            val leafAccounts = getLeafAccounts(result.accounts)

            val leafAccountFullNameToWithCumulativeTotals =
                mutableMapOf<String, Map<MonthYear, BigDecimal>>()
            val leafAccountFullNameToWithoutCumulativeTotals =
                mutableMapOf<String, Map<MonthYear, BigDecimal>>()
            // Should only process new transactions. Reports and transactions that are older than
            // [since], should not change. Which is the whole point of [since] config value
            // Will read transactions since [since], process them, generate new transactions by
            // month year + reports for the years that have changed and put them in the folder
            // where they belong
            result.transactions
                .groupBy { it.date.year }
                .forEach { (year, transactionsForYear) ->
                    transactionsForYear
                        .groupBy { it.date.month }
                        .forEach { (month, transactionsForMonth) ->
                            // Transaction splits for leaf accounts
                            val splitsForMonth = transactionsForMonth.flatMap { it.splits }
                            leafAccounts.map { leafAccount ->
                                val accountTotalForMonth = splitsForMonth
                                    .filter { it.accountId == leafAccount.id }
                                    .sumOf { it.amount }

                                val monthYear = MonthYear(month, year)
                                // Put cumulative total
                                val accountTotalsCumulative =
                                    (leafAccountFullNameToWithCumulativeTotals[leafAccount.fullName]
                                        ?: mapOf()).toMutableMap()
                                val cumulativeTotalForMonth =
                                    accountTotalsCumulative.getPreviousTotal(monthYear, since)
                                        .plus(accountTotalForMonth)
                                accountTotalsCumulative[monthYear] = cumulativeTotalForMonth
                                leafAccountFullNameToWithCumulativeTotals[leafAccount.fullName] =
                                    accountTotalsCumulative

                                // Put without cumulative total
                                val accountTotals =
                                    (leafAccountFullNameToWithoutCumulativeTotals[leafAccount.fullName]
                                        ?: mapOf()).toMutableMap()
                                accountTotals[monthYear] = accountTotalForMonth
                                leafAccountFullNameToWithoutCumulativeTotals[leafAccount.fullName] =
                                    accountTotals
                            }

                            TransactionsByMonthYear(
                                monthYear = MonthYear(month, year),
                                transactions = transactionsForMonth,
                            ).let {
                                transactionsWriter.write(it)
                            }
                        }
                    config.reports
                        .map { reportConfig ->
                            when (reportConfig) {
                                is Config.Report.Normal -> {
                                    val isCumulative = reportConfig.withCumulativeBalance
                                        ?: cumulativeDeterminer.isAccountTypeCumulative(reportConfig.accountType)
                                    normalReportGenerator.generate(
                                        accounts = result.accounts,
                                        leafAccountFullNameToTotalsMap = if (isCumulative) {
                                            leafAccountFullNameToWithCumulativeTotals
                                        } else {
                                            leafAccountFullNameToWithoutCumulativeTotals
                                        },
                                        reportConfig = reportConfig,
                                        year = year,
                                    )
                                }

                                is Config.Report.NetWorth -> {
                                    netWorthReportGenerator.generate(
                                        leafAccountFullNameToTotalsMap = leafAccountFullNameToWithCumulativeTotals,
                                        assetsRootAccount = config.assetsRootAccount,
                                        liabilitiesRootAccount = config.liabilitiesRootAccount,
                                        year = year,
                                    )
                                }

                                is Config.Report.SavingsRate -> {
                                    savingsRateReportGenerator.generate(
                                        leafAccountFullNameToTotalsMap = leafAccountFullNameToWithoutCumulativeTotals,
                                        expensesWithoutTaxesFilter = reportConfig.expensesFilter,
                                        taxesFilter = reportConfig.taxesFilter,
                                        incomeFilter = reportConfig.incomeFilter,
                                        expensesRootAccount = config.expenseRootAccount,
                                        incomeRootAccount = config.incomeRootAccount,
                                        year = year,
                                    )
                                }
                            }
                        }
                }
            currentBalancesGenerator.generate(
                txGroupsConfig = config.txGroups,
                transactions = result.transactions,
                accountBalancesConfig = config.accountBalances,
                leafAccountFullNameToCumulativeTotalsMap = leafAccountFullNameToWithCumulativeTotals,
                leafAccountFullNameToWithoutCumulativeTotalsMap = leafAccountFullNameToWithoutCumulativeTotals,
            )
        }
}

private fun getStartingMonthYear(
    recent: Boolean,
    clock: Clock = Clock.System,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): MonthYear {
    val year = if (recent) {
        val now = MonthYear.now(clock, timeZone)
        val threeMonthsAgo = now.minus(DateTimePeriod(months = 3))
        minOf(now.year, threeMonthsAgo.year)
    } else {
        2019
    }
    return MonthYear(year = year, month = Month.JANUARY)
}

/**
 * Gets the total of previous MonthYear as long as previous MonthYear is more than [oldestMonthYear]
 *
 * If the MonthYears are not continuous in the map, then it'll iterate the map until it finds the
 * first MonthYear that is older than [monthYear] that has a total
 */
private fun Map<MonthYear, BigDecimal>.getPreviousTotal(
    monthYear: MonthYear,
    oldestMonthYear: MonthYear,
): BigDecimal {
    var previous = monthYear.previous()
    var total = BigDecimal.ZERO
    while (previous >= oldestMonthYear) {
        val previousTotal = this[previous]
        if (previousTotal != null) {
            total = total.plus(previousTotal)
            break
        }
        previous = previous.previous()
    }
    return total
}

private fun getLeafAccounts(accounts: List<Account>): List<Account> {
    val accountIdsThatAreParents = accounts
        .map { it.parentId }
        .distinct()

    return accounts
        .filter { it.id !in accountIdsThatAreParents }
}
