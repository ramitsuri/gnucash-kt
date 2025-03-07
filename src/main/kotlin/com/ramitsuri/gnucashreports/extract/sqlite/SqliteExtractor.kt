package com.ramitsuri.gnucashreports.extract.sqlite

import com.ramitsuri.gnucashreports.extract.Extractor
import com.ramitsuri.gnucashreports.model.Account
import com.ramitsuri.gnucashreports.model.GnuCashData
import com.ramitsuri.gnucashreports.model.Transaction
import com.ramitsuri.gnucashreports.model.TransactionSplit
import java.io.File
import java.sql.DriverManager

class SqliteExtractor : Extractor {
    override fun extract(gnuCashFile: File): GnuCashData {
        DriverManager
            .getConnection("jdbc:sqlite:${gnuCashFile.absolutePath}")
            .use { connection ->
                val statement = connection.createStatement()
                statement.queryTimeout = 30 // seconds

                val accounts = statement.executeQuery("SELECT * FROM accounts")
                    .use { accountsResultSet ->
                        Account
                            .getResultSetMatcher()
                            .getRows(accountsResultSet)
                            .map { Account.fromRow(it) }
                    }

                val txSplitsByTxId = statement.executeQuery("SELECT * FROM splits")
                    .use { txSplitsResultSet ->
                        TransactionSplit
                            .getResultSetMatcher()
                            .getRows(txSplitsResultSet)
                            .map { TransactionSplit.fromRow(it) }
                            .groupBy { it.first }
                            .mapValues { (_, value) -> value.map { it.second } }
                    }

                val transactions = statement.executeQuery("SELECT * FROM transactions")
                    .use { transactionResultSet ->
                        Transaction
                            .getResultSetMatcher()
                            .getRows(transactionResultSet)
                            .map { row ->
                                Transaction.fromRow(row).let { txWithoutSplits ->
                                    txWithoutSplits.copy(
                                        splits = txSplitsByTxId[txWithoutSplits.id] ?: listOf(),
                                    )
                                }
                            }
                    }

                return GnuCashData(
                    accounts = accounts,
                    transactions = transactions,
                )
            }
    }
}
