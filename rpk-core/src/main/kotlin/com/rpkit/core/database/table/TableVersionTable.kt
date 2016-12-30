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

package com.rpkit.core.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.database.TableVersion
import com.rpkit.core.database.use
import java.sql.SQLException
import java.sql.Statement.RETURN_GENERATED_KEYS

/**
 * Represents the database table used to record versions of tables.
 * Allows for migrations to be applied if a table is out of date.
 */
class TableVersionTable(database: Database): Table<TableVersion>(database, TableVersion::class.java) {

    override fun create() {
        try {
            database.createConnection().use {
                connection -> connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS table_version (" +
                        "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                        "table_name VARCHAR(256)," +
                        "version VARCHAR(32)" +
                    ")"
                ).use {
                    statement -> statement.executeUpdate()
                }
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
    }

    override fun applyMigrations() {

    }

    override fun insert(entity: TableVersion): Int {
        try {
            var id = 0
            database.createConnection().use {
                connection -> connection.prepareStatement(
                    "INSERT INTO table_version(table_name, version) VALUES(?, ?)",
                    RETURN_GENERATED_KEYS
                ).use {
                    statement ->
                        statement.setString(1, entity.table)
                        statement.setString(2, entity.version)
                        statement.executeUpdate()
                        val generatedKeys = statement.generatedKeys
                        if (generatedKeys.next()) {
                            id = generatedKeys.getInt(1)
                            entity.id = id
                        }
                }
            }
            return id
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
        return 0
    }

    override fun update(entity: TableVersion) {
        try {
            database.createConnection().use {
                connection -> connection.prepareStatement(
                    "UPDATE table_version SET table_name = ?, version = ? WHERE id = ?"
                ).use {
                    statement ->
                        statement.setString(1, entity.table)
                        statement.setString(2, entity.version)
                        statement.setInt(3, entity.id)
                        statement.executeUpdate()
                    }
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
    }

    override fun get(id: Int): TableVersion? {
        try {
            var tableVersion: TableVersion? = null
            database.createConnection().use {
                connection -> connection.prepareStatement(
                    "SELECT id, table_name, version FROM table_version WHERE id = ?"
                ).use {
                    statement ->
                        statement.setInt(1, id)
                        val resultSet = statement.executeQuery()
                        if (resultSet.next()) {
                            tableVersion = TableVersion(resultSet.getInt("id"), resultSet.getString("table_name"), resultSet.getString("version"))
                        }
                }
            }
            return tableVersion
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
        return null
    }

    /**
     * Gets the version of a table.
     */
    fun get(table: String): TableVersion? {
        try {
            var tableVersion: TableVersion? = null
            database.createConnection().use {
                connection -> connection.prepareStatement(
                    "SELECT id, table_name, version FROM table_version WHERE table_name = ?"
                ).use {
                    statement ->
                        statement.setString(1, table)
                        val resultSet = statement.executeQuery()
                        if (resultSet.next()) {
                            tableVersion = TableVersion(resultSet.getInt("id"), resultSet.getString("table_name"), resultSet.getString("version"))
                        }
                }
            }
            return tableVersion
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
        return null
    }

    override fun delete(entity: TableVersion) {
        try {
            database.createConnection().use {
                connection -> connection.prepareStatement(
                    "DELETE FROM table_version WHERE id = ?"
                ).use {
                    statement -> statement.executeUpdate()
                }
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
    }

}