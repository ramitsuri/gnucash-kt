package com.ramitsuri.gnucashreports.extract.sqlite

fun Map<Field, String>.getOrError(field: Field): String {
    return this[field] ?: error("Field $field not found in row")
}
