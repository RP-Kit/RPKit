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

package com.rpkit.core.database

import com.rpkit.core.database.table.TableVersionTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.conf.Settings
import org.jooq.impl.DSL
import java.sql.Connection
import java.sql.SQLException
import kotlin.reflect.KClass

/**
 * Represents a database.
 * Primarily used for obtaining connections and keeping track of tables.
 *
 * @property url The URL of the database
 * @property userName The username to connect to the database with. May be null to avoid authenticating.
 * @property password The password to connect to the database with. May be null to avoid authenticating.
 * @property dialect The dialect of SQL to use
 */
class Database @JvmOverloads constructor(val url: String, val userName: String? = null, val password: String? = null, val dialect: SQLDialect) {

    private val dataSource: HikariDataSource
    private val tables: MutableMap<KClass<out Table<*>>, Table<*>> = mutableMapOf()
    private val settings = Settings().withRenderSchema(false)

    /**
     * DSL context for performing jOOQ queries. This is currently the preferred method of performing queries, introduced
     * in v1.3.0. It allows queries to be database-agnostic, so plugins will work with SQLite backends, and require no
     * porting work should support for other database backends be added in the future.
     */
    val create: DSLContext
        get() = DSL.using(dataSource, dialect, settings)

    init {
        val hikariConfig = HikariConfig()
        hikariConfig.jdbcUrl = url
        if (userName != null) {
            hikariConfig.username = userName
        }
        if (password != null) {
            hikariConfig.password = password
        }
        dataSource = HikariDataSource(hikariConfig)
        addTable(TableVersionTable(this))
    }

    /**
     * Create a new connection to the database.
     *
     * @return The connection
     */
    @Throws(SQLException::class)
    fun createConnection(): Connection {
        return dataSource.connection
    }

    /**
     * Adds a table to be tracked by the database.
     *
     * @param table The table to be tracked.
     */
    fun addTable(table: Table<*>) {
        tables.put(table.javaClass.kotlin, table)
        table.create()
        table.applyMigrations()
    }

    /**
     * Gets a table by its class
     * Easiest method of use from Java plugins, for Kotlin plugins you can pass a [KClass]
     *
     * @param type The type of the table
     * @return The table
     */
    fun <T: Table<*>> getTable(type: Class<T>): T {
        return getTable(type.kotlin)
    }

    /**
     * Gets a table by its class
     * Easiest method of use from Kotlin plugins, for Java plugins you can pass a [Class]
     *
     * @param type The type of the table
     * @return The table
     */
    @Suppress("UNCHECKED_CAST")
    fun <T: Table<*>> getTable(type: KClass<T>): T {
        return tables[type] as T
    }

    /**
     * Gets the version of a table.
     * May return null if it has not been set in a previous execution.
     *
     * @param table The table to get the version of
     * @return The version of the table. May be null.
     */
    fun getTableVersion(table: Table<*>): String? {
        return getTable(TableVersionTable::class).get(table.name)?.version
    }

    /**
     * Sets a table's version.
     * This should usually be called after performing a migration successfully.
     *
     * @param table The table to set the version of
     * @param version The version to set
     */
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
