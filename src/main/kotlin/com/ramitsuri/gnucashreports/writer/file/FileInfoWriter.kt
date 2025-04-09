package com.ramitsuri.gnucashreports.writer.file

import com.ramitsuri.gnucashreports.model.Info
import com.ramitsuri.gnucashreports.writer.InfoWriter
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Paths

class FileInfoWriter(
    private val basePath: String,
    private val json: Json,
) : InfoWriter {
    override fun write(info: Info) {
        val dir = Paths.get(basePath)
        Files.createDirectories(dir)
        val infoJson = json.encodeToString(Info.serializer(), info)
        val fileName =  "info.json"
        Files.writeString(dir.resolve(fileName), infoJson)
    }
}
