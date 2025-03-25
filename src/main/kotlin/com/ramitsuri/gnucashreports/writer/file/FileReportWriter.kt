package com.ramitsuri.gnucashreports.writer.file

import com.ramitsuri.gnucashreports.model.report.Report
import com.ramitsuri.gnucashreports.model.report.ReportsByYear
import com.ramitsuri.gnucashreports.writer.ReportWriter
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Paths

class FileReportWriter(
    private val basePath: String,
    private val json: Json,
) : ReportWriter {

    override fun write(reports: ReportsByYear) {
        reports.let { (year, reportsForYear) ->
            reportsForYear.forEach { report ->
                val dir = Paths.get(basePath, year.toString())
                Files.createDirectories(dir)
                val reportJson = json.encodeToString(Report.serializer(), report)
                val fileName = report.name.replace(" ", "_").plus(".json")
                Files.writeString(dir.resolve(fileName), reportJson)
            }
        }
    }
}
