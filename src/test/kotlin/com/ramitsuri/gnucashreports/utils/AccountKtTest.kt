package com.ramitsuri.gnucashreports.utils

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AccountKtTest {

    @Test
    fun isParentOfOrSelfTest() {
        "Expenses".assertIsParentOfOrSelf("Expenses")
        "Expenses".assertIsParentOfOrSelf("Expenses:Taxes")
        "Expenses".assertIsParentOfOrSelf("Expenses:Taxes:Federal")
        "Expenses".assertIsParentOfOrSelf("Expenses:Taxes (Spouse)")
        "Expenses".assertIsParentOfOrSelf("Expenses:Taxes (Spouse):Federal")

        "Expenses".assertNotIsParentOfOrSelf("Assets")
        "Expenses".assertNotIsParentOfOrSelf("Income")
        "Expenses:Taxes (Spouse):Federal".assertNotIsParentOfOrSelf("Expenses")
        "Income:Salary".assertNotIsParentOfOrSelf("Income:Salary (Spouse)")
    }

    private fun String.assertIsParentOfOrSelf(accountFullName: String) {
        assertTrue(this.isParentOfOrSelf(accountFullName))
    }

    private fun String.assertNotIsParentOfOrSelf(accountFullName: String) {
        assertFalse(this.isParentOfOrSelf(accountFullName))
    }
}
