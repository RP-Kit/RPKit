/*
 * Copyright 2021 Ren Binden
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

import com.rpkit.core.caching.RPKCacheManager
import com.rpkit.core.caching.RPKCacheManagerImpl
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import javax.sql.DataSource
import kotlin.reflect.KClass

/**
 * Represents a database.
 * Primarily used for obtaining connections and keeping track of tables.
 *
 * @property connectionProperties The database connection properties
 * @property migrationProperties The database migration properties
 */
class Database(
    val connectionProperties: DatabaseConnectionProperties,
    private val migrationProperties: DatabaseMigrationProperties,
    flywayClassLoader: ClassLoader
) {

    companion object {
        lateinit var hikariClassLoader: ClassLoader
    }

    val dataSource: DataSource
    private val tables: MutableMap<KClass<out Table>, Table> = mutableMapOf()
    val cacheManager: RPKCacheManager

    init {
        val oldClassLoader = Thread.currentThread().contextClassLoader
        Thread.currentThread().contextClassLoader = hikariClassLoader
        val hikariConfig = HikariConfig()
        hikariConfig.jdbcUrl = connectionProperties.url
        if (connectionProperties.username != null) {
            hikariConfig.username = connectionProperties.username
        }
        if (connectionProperties.password != null) {
            hikariConfig.password = connectionProperties.password
        }
        hikariConfig.maximumPoolSize = connectionProperties.maximumPoolSize
        hikariConfig.minimumIdle = connectionProperties.minimumIdle
        dataSource = HikariDataSource(hikariConfig)
        Thread.currentThread().contextClassLoader = flywayClassLoader
        val flyway = Flyway.configure().dataSource(dataSource)
                .locations("classpath:${migrationProperties.location}")
                .table(migrationProperties.schemaHistoryTable)
                .load()
        flyway.migrate()
        Thread.currentThread().contextClassLoader = oldClassLoader
        cacheManager = RPKCacheManagerImpl()
    }

    /**
     * Adds a table to be tracked by the database.
     *
     * @param table The table to be tracked.
     */
    fun addTable(table: Table) {
        tables[table.javaClass.kotlin] = table
    }

    /**
     * Gets a table by its class
     * Easiest method of use from Java plugins, for Kotlin plugins you can pass a [KClass]
     *
     * @param type The type of the table
     * @return The table
     */
    fun <T: Table> getTable(type: Class<T>): T {
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
    private fun <T: Table> getTable(type: KClass<T>): T {
        return tables[type] as T
    }

}
