package com.ramitsuri.gnucashreports.utils

import com.ramitsuri.gnucashreports.model.AccountType

class CumulativeDeterminer(
    private val expensesRootAccount: String,
    private val incomeRootAccount: String,
    private val assetsRootAccount: String,
    private val liabilitiesRootAccount: String,
) {
    fun isAccountCumulative(accountName: String): Boolean {
        if (expensesRootAccount.isParentOfOrSelf(accountName)) {
            return false
        }
        if (incomeRootAccount.isParentOfOrSelf(accountName)) {
            return false
        }
        if (assetsRootAccount.isParentOfOrSelf(accountName)) {
            return true
        }
        if (liabilitiesRootAccount.isParentOfOrSelf(accountName)) {
            return true
        }
        error("Unknown account type: $accountName")
    }

    fun isAccountTypeCumulative(accountType: AccountType): Boolean {
        return when (accountType) {
            AccountType.EXPENSE -> false
            AccountType.INCOME -> false
            AccountType.ASSET -> true
            AccountType.LIABILITY -> true
            else -> error("Should not be used for: $accountType")
        }
    }
}
