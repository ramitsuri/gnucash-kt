package com.ramitsuri.gnucashreports.extract.sqlite

import com.ramitsuri.gnucashreports.extract.Extractor
import com.ramitsuri.gnucashreports.model.Account
import com.ramitsuri.gnucashreports.model.AccountType
import com.ramitsuri.gnucashreports.model.GnuCashData
import com.ramitsuri.gnucashreports.model.Transaction
import com.ramitsuri.gnucashreports.model.TransactionSplit
import com.ramitsuri.gnucashreports.model.report.MonthYear
import java.io.File
import java.sql.DriverManager
import java.sql.Statement

class SqliteExtractor : Extractor {
    override fun extract(since: MonthYear, gnuCashFile: File): GnuCashData {
        DriverManager
            .getConnection("jdbc:sqlite:${gnuCashFile.absolutePath}")
            .use { connection ->
                val statement = connection.createStatement()
                statement.queryTimeout = 30 // seconds

                val accounts = statement.getAccounts()
                val rootAccount = accounts.first { it.type == AccountType.ROOT }

                val accountIdNameMap = accounts
                    .associate { it.id to it.name }
                val accountIdParentIdMap = accounts
                    .associate { it.id to it.parentId }
                val accountIdFullNameMap = accounts
                    .associate { account ->
                        var name = account.name
                        var parentId = account.parentId
                        while (parentId != rootAccount.id && parentId.isNotEmpty()) {
                            name = (accountIdNameMap[parentId] ?: "")
                                .plus(":")
                                .plus(name)
                            parentId = accountIdParentIdMap[parentId] ?: ""
                        }
                        account.id to name
                    }
                return GnuCashData(
                    accounts = accounts.map {
                        it.copy(
                            fullName = accountIdFullNameMap[it.id] ?: it.name,
                        )
                    },
                    transactions = statement.getTransactions(
                        since = since,
                        accountIdFullNameMap = accountIdFullNameMap,
                    ),
                )
            }
    }

    private fun Statement.getAccounts(): List<Account> {
        val accounts = executeQuery("SELECT * FROM accounts")
            .use { accountsResultSet ->
                Account
                    .getResultSetMatcher()
                    .getRows(accountsResultSet)
                    .map { Account.fromRow(it) }
            }
        return accounts
    }

    private fun Statement.getTransactions(
        since: MonthYear,
        accountIdFullNameMap: Map<String, String>,
    ): List<Transaction> {
        val transactions = executeQuery(
            "SELECT * FROM transactions WHERE post_date > ${since.string()}",
        ).use { transactionResultSet ->
            Transaction
                .getResultSetMatcher()
                .getRows(transactionResultSet)
                .map { row ->
                    Transaction.fromRow(row)
                }
        }

        val txIds = transactions.joinToString(
            separator = "','",
            prefix = "('",
            postfix = "')",
        ) { it.id }

        val txSplitsByTxId = executeQuery(
            "SELECT * FROM splits WHERE tx_guid IN $txIds",
        ).use { txSplitsResultSet ->
            TransactionSplit
                .getResultSetMatcher()
                .getRows(txSplitsResultSet)
                .map { TransactionSplit.fromRow(it) }
                .groupBy { it.first }
                .mapValues { (_, value) -> value.map { it.second } }
        }

        return transactions.map { txWithoutSplits ->
            val splits = (txSplitsByTxId[txWithoutSplits.id] ?: listOf())
                .map {
                    it.copy(accountName = accountIdFullNameMap[it.accountId] ?: "")
                }
            txWithoutSplits.copy(
                splits = splits,
            )
        }
    }
}
