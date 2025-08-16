package com.ramitsuri.gnucashreports.writer.file

import com.ramitsuri.gnucashreports.model.Transaction
import com.ramitsuri.gnucashreports.model.report.TransactionsByMonthYear
import com.ramitsuri.gnucashreports.writer.TransactionsWriter
import java.nio.file.Files
import java.nio.file.Paths
import kotlinx.datetime.number
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class FileTransactionsWriter(
    private val basePath: String,
    private val json: Json,
) : TransactionsWriter {
    override fun write(transactions: TransactionsByMonthYear) {
        transactions
            .let { (monthYear, transactionsForMonthYear) ->
                val dir = Paths.get(
                    basePath,
                    monthYear.year.toString(),
                )
                Files.createDirectories(dir)
                val transactionsJson = json.encodeToString(
                    ListSerializer(Transaction.serializer()),
                    transactionsForMonthYear,
                )
                val fileName = monthYear.month.number.toString().padStart(2, '0').plus(".json")
                Files.writeString(dir.resolve(fileName), transactionsJson)
            }
    }
}
