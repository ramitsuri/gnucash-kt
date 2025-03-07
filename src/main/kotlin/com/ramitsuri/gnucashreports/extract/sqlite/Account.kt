package com.ramitsuri.gnucashreports.extract.sqlite

import com.ramitsuri.gnucashreports.model.Account
import com.ramitsuri.gnucashreports.model.AccountType

fun Account.Companion.getResultSetMatcher() = ResultSetMatcher(
    listOf(
        Field.GUID,
        Field.NAME,
        Field.TYPE,
        Field.PARENT,
        Field.DESCRIPTION,
    ),
)

fun Account.Companion.fromRow(row: Map<Field, String>) = Account(
    id = row.getOrError(Field.GUID),
    name = row.getOrError(Field.NAME),
    type = AccountType.from(row.getOrError(Field.TYPE)),
    parentId = row.getOrError(Field.PARENT),
)
