package com.ramitsuri.gnucashreports.writer

import com.ramitsuri.gnucashreports.model.report.ReportsByYear

interface ReportWriter {
    fun write(reports: ReportsByYear)
}
