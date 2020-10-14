/*
 * Copyright 2020 Ren Binden
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

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheManagerBuilder
import org.flywaydb.core.Flyway
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.conf.Settings
import org.jooq.impl.DSL
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
        val migrationProperties: DatabaseMigrationProperties,
        val classLoader: ClassLoader
) {

    private val dataSource: HikariDataSource
    private val tables: MutableMap<KClass<out Table>, Table> = mutableMapOf()
    private val settings = Settings().withRenderSchema(false)
    val cacheManager: CacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)

    /**
     * DSL context for performing jOOQ queries. This is currently the preferred method of performing queries, introduced
     * in v1.3.0. It allows queries to be database-agnostic, so plugins will work with SQLite backends, and require no
     * porting work should support for other database backends be added in the future.
     */
    val create: DSLContext
        get() = DSL.using(
                dataSource,
                SQLDialect.valueOf(connectionProperties.sqlDialect),
                settings
        )

    init {
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
        val oldClassLoader = Thread.currentThread().contextClassLoader
        Thread.currentThread().contextClassLoader = classLoader
        val flyway = Flyway.configure().dataSource(dataSource)
                .locations("classpath:${migrationProperties.location}")
                .table(migrationProperties.schemaHistoryTable)
                .load()
        flyway.migrate()
        Thread.currentThread().contextClassLoader = oldClassLoader
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
    fun <T: Table> getTable(type: KClass<T>): T {
        return tables[type] as T
    }

}
