package com.ramitsuri.gnucashreports.writer

import com.ramitsuri.gnucashreports.model.CurrentBalance

interface CurrentBalancesWriter {
    fun write(
        currentBalances: List<CurrentBalance>,
    )
}
