package com.seventh_root.elysium.core.database

import com.google.common.base.CaseFormat.LOWER_UNDERSCORE
import com.google.common.base.CaseFormat.UPPER_CAMEL
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*

class Database @JvmOverloads constructor(val url: String, val userName: String? = null, val password: String? = null) {

    private val tables: MutableMap<String, Table<*>>

    init {
        tables = HashMap<String, Table<*>>()
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
    }

    fun getTable(name: String): Table<*>? {
        val table = tables[name]
        return table
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : TableRow> getTable(type: Class<T>): Table<T>? {
        val table = tables[UPPER_CAMEL.to(LOWER_UNDERSCORE, type.simpleName)]
        return table as Table<T>?
    }

}
