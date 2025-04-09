package com.ramitsuri.gnucashreports.generator

import com.ramitsuri.gnucashreports.model.Info
import com.ramitsuri.gnucashreports.writer.InfoWriter
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.io.File

class InfoGenerator(
    private val infoWriter: InfoWriter,
    private val clock: Clock = Clock.System,
) {
    fun generate(gnuCashFile: String) {
        val file = File(gnuCashFile)
        if (!file.exists()) {
            error("File doesn't exist")
        }
        val fileLastModifiedAt = Instant.fromEpochMilliseconds(file.lastModified())
        val info = Info(
            fileLastModifiedTime = fileLastModifiedAt,
            lastRunTime = clock.now(),
        )
        infoWriter.write(info)
    }
}
