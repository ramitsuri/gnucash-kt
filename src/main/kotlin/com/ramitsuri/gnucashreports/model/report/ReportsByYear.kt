package com.ramitsuri.gnucashreports.model.report

data class ReportsByYear(
    val year: Int,
    val reports: List<Report>,
)
