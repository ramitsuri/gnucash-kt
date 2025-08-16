package com.ramitsuri.gnucashreports.writer.file

import com.ramitsuri.gnucashreports.model.CurrentBalance
import com.ramitsuri.gnucashreports.writer.CurrentBalancesWriter
import java.nio.file.Files
import java.nio.file.Paths
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class FileCurrentBalancesWriter(
    private val basePath: String,
    private val json: Json,
) : CurrentBalancesWriter {

    override fun write(currentBalances: List<CurrentBalance>) {
        val dir = Paths.get(basePath)
        Files.createDirectories(dir)
        val currentBalancesJson = json
            .encodeToString(
                serializer = ListSerializer(elementSerializer = CurrentBalance.serializer()),
                value = currentBalances,
            )
        val fileName = "current_balances.json"
        Files.writeString(dir.resolve(fileName), currentBalancesJson)
    }
}
