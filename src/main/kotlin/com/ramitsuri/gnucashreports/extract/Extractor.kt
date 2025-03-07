package com.ramitsuri.gnucashreports.extract

import com.ramitsuri.gnucashreports.model.GnuCashData
import java.io.File

interface Extractor {
    fun extract(gnuCashFile: File): GnuCashData
}
