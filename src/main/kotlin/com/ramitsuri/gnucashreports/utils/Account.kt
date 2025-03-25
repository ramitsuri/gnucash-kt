package com.ramitsuri.gnucashreports.utils

fun String.isParentOfOrSelf(accountFullName: String): Boolean {
    return accountFullName == this ||
        accountFullName.startsWith(this.plus(":"))
}
