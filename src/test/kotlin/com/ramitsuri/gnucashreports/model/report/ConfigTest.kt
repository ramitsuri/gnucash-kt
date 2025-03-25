package com.ramitsuri.gnucashreports.model.report

import org.junit.jupiter.api.Test
import com.ramitsuri.gnucashreports.model.report.Config.Report.Filter
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConfigTest {

    @Test
    fun includeConfigReportFilterTest() {
        Filter.Include(
            listOf(
                "Assets:Investments:Retirement",
                "Assets:Current Assets:HSA",
            ),
        ).apply {
            assertCanInclude("Assets:Investments:Retirement")
            assertCanInclude("Assets:Investments:Retirement:Contribution")
            assertCanInclude("Assets:Investments:Retirement:Gains")

            assertCanInclude("Assets:Current Assets:HSA")
            assertCanInclude("Assets:Current Assets:HSA:Contribution")
            assertCanInclude("Assets:Current Assets:HSA:Gains")

            assertCanNotInclude("Assets:Investments:Spouse Retirement")
            assertCanNotInclude("Expenses:Taxes")
            assertCanNotInclude("Expenses")
            assertCanNotInclude("Assets:Investments")
            assertCanNotInclude("Assets")
            assertCanNotInclude("Assets:Current Assets")
            assertCanNotInclude("Assets")
        }
    }

    @Test
    fun excludeConfigReportFilterTest() {
        Filter.Exclude(
            listOf(
                "Expenses:Taxes",
            ),
        ).apply {
            assertCanInclude("Expenses:Medical")
            assertCanInclude("Expenses:Food")
            assertCanInclude("Expenses")

            assertCanInclude("Assets:Current Assets")
            assertCanInclude("Assets")

            assertCanNotInclude("Expenses:Taxes:State")
            assertCanNotInclude("Expenses:Taxes:City")
            assertCanNotInclude("Expenses:Taxes")
        }
    }

    private fun Filter.assertCanInclude(account: String) {
        assertTrue(canInclude(account))
    }

    private fun Filter.assertCanNotInclude(account: String) {
        assertFalse(canInclude(account))
    }
}
