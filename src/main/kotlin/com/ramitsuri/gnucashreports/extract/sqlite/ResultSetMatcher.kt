package com.ramitsuri.gnucashreports.extract.sqlite

import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.SQLException
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap

class ResultSetMatcher(private val requiredFields: List<Field>) {

    @Throws(SQLException::class)
    fun getRows(resultSet: ResultSet): Iterable<Map<Field, String>> {
        val metaData = resultSet.metaData
        require(matches(metaData)) { "ResultSet does not match matcher." }

        val fieldColumnIndexMap: Map<Field, Int> = getColumnIndicesByName(metaData)
            .let { columnNameColumnIndexMap ->
                requiredFields
                    .map { field ->
                        val columnIndex = columnNameColumnIndexMap[field.columnName]
                            ?: error("Column index not found for $field")
                        field to columnIndex
                    }
                    .associate { it }
            }

        return Iterable {
            object : MutableIterator<Map<Field, String>> {
                override fun hasNext(): Boolean {
                    return try {
                        resultSet.next()
                    } catch (e: SQLException) {
                        false
                    }
                }

                override fun next(): Map<Field, String> {
                    return fieldColumnIndexMap
                        .map { (field, columnIndex) ->
                            val resultString = try {
                                resultSet.getString(columnIndex)
                            } catch (e: SQLException) {
                                throw NoSuchElementException()
                            }
                            field to (resultString ?: "")
                        }.associate { it }
                }

                override fun remove() {
                    // Not needed
                }
            }
        }
    }

    private fun matches(metaData: ResultSetMetaData): Boolean {
        val columnNames = try {
            getColumnIndicesByName(metaData)
        } catch (e: SQLException) {
            return false
        }
        return requiredFields
            .map(Field::columnName)
            .all { requiredField -> columnNames.containsKey(requiredField) }
    }

    @Throws(SQLException::class)
    private fun getColumnIndicesByName(metaData: ResultSetMetaData): ImmutableMap<String, Int> {
        val columnNamesMap = mutableMapOf<String, Int>()
        val columnCount = metaData.columnCount
        for (i in 1..columnCount) {
            columnNamesMap[metaData.getColumnName(i)] = i
        }
        return columnNamesMap.toImmutableMap()
    }
}
