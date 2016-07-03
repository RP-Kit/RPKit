/*
 * Copyright 2016 Ross Binden
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.seventh_root.elysium.core.database

import com.seventh_root.elysium.core.database.table.TableVersionTable
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*
import kotlin.reflect.KClass

class Database @JvmOverloads constructor(val url: String, val userName: String? = null, val password: String? = null) {

    private val tables: MutableMap<KClass<out Table<*>>, Table<*>>

    init {
        tables = HashMap<KClass<out Table<*>>, Table<*>>()
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
        tables.put(table.javaClass.kotlin, table)
        table.create()
        table.applyMigrations()
    }

    fun <T: Table<*>> getTable(type: Class<T>): T {
        return getTable(type.kotlin)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T: Table<*>> getTable(type: KClass<T>): T {
        return tables[type] as T
    }

    fun getTableVersion(table: Table<*>): String? {
        return getTable(TableVersionTable::class).get(table.name)?.version
    }

    fun setTableVersion(table: Table<*>, version: String) {
        val tableVersionTable = getTable(TableVersionTable::class)
        val tableVersion = tableVersionTable.get(table.name)
        if (tableVersion == null) {
            tableVersionTable.insert(TableVersion(table = table.name, version = version))
        } else {
            tableVersion.version = version
            tableVersionTable.update(tableVersion)
        }
    }

}
