package com.ramitsuri.gnucashreports.extract

import com.ramitsuri.gnucashreports.model.GnuCashData
import com.ramitsuri.gnucashreports.model.report.MonthYear
import java.io.File

interface Extractor {
    fun extract(since: MonthYear, gnuCashFile: File): GnuCashData
}
