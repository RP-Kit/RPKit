package com.seventh_root.elysium.core.database

import com.google.common.base.CaseFormat.LOWER_UNDERSCORE
import com.google.common.base.CaseFormat.UPPER_CAMEL
import com.seventh_root.elysium.core.database.table.TableVersionTable
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*

class Database @JvmOverloads constructor(val url: String, val userName: String? = null, val password: String? = null) {

    private val tables: MutableMap<String, Table<*>>

    init {
        tables = HashMap<String, Table<*>>()
        addTable(TableVersionTable(this))
    }

    @Throws(SQLException::class)
    fun createConnection(): Connection {
        if (userName == null && password == null) {
            return DriverManager.getConnection(url)
        } else {
            return DriverManager.getConnection(url, userName, password)
        }
    }

    fun addTable(table: Table<*>) {
        tables.put(table.name, table)
        table.create()
        table.applyMigrations()
    }

    fun getTable(name: String): Table<*>? {
        val table = tables[name]
        return table
    }

    @Suppress("UNCHECKED_CAST")
    fun <T: TableRow> getTable(type: Class<T>): Table<T>? {
        val table = tables[UPPER_CAMEL.to(LOWER_UNDERSCORE, type.simpleName)]
        return table as Table<T>?
    }

    fun getTableVersion(table: Table<*>): String? {
        return (getTable(TableVersion::class.java) as TableVersionTable).get(table.name)?.version
    }

    fun setTableVersion(table: Table<*>, version: String) {
        val tableVersionTable: TableVersionTable? = getTable(TableVersion::class.java) as TableVersionTable
        if (tableVersionTable != null) {
            val tableVersion = tableVersionTable.get(table.name)
            if (tableVersion == null) {
                tableVersionTable.insert(TableVersion(table = table.name, version = version))
            } else {
                tableVersion.version = version
                tableVersionTable.update(tableVersion)
            }
        }
    }

}
