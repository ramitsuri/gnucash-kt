package com.ramitsuri.gnucashreports.writer

import com.ramitsuri.gnucashreports.model.Info

interface InfoWriter {
    fun write(info: Info)
}
