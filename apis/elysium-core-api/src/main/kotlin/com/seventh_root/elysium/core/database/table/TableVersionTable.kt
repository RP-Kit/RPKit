package com.seventh_root.elysium.core.database.table

import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.database.Table
import com.seventh_root.elysium.core.database.TableVersion
import com.seventh_root.elysium.core.database.use
import java.sql.SQLException
import java.sql.Statement.RETURN_GENERATED_KEYS


class TableVersionTable(database: Database): Table<TableVersion>(database, TableVersion::class.java) {

    override fun create() {
        try {
            database.createConnection().use {
                connection -> connection.prepareStatement(
                    "CREATE TABLE table_version(" +
                        "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                        "table VARCHAR(256)," +
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

    override fun insert(`object`: TableVersion): Int {
        try {
            var id = 0
            database.createConnection().use {
                connection -> connection.prepareStatement(
                    "INSERT INTO table_version(table, version) VALUES(?, ?)",
                    RETURN_GENERATED_KEYS
                ).use {
                    statement -> {
                        statement.setString(1, `object`.table)
                        statement.setString(2, `object`.version)
                        statement.executeUpdate()
                        val generatedKeys = statement.generatedKeys
                        if (generatedKeys.next()) {
                            `object`.id = id
                            id = generatedKeys.getInt(0)
                        }
                    }
                }
            }
            return 0
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
        return 0
    }

    override fun update(`object`: TableVersion) {
        try {
            database.createConnection().use {
                connection -> connection.prepareStatement(
                    "UPDATE table_version SET table = ?, version = ? WHERE id = ?"
                ).use {
                    statement -> {
                        statement.setString(1, `object`.table)
                        statement.setString(2, `object`.version)
                        statement.setInt(3, `object`.id)
                        statement.executeUpdate()
                    }
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
                    "SELECT id, table, version FROM table_version WHERE id = ?"
                ).use {
                    statement -> {
                        statement.setInt(1, id)
                        val resultSet = statement.executeQuery()
                        if (resultSet.next()) {
                            tableVersion = TableVersion(resultSet.getInt("id"), resultSet.getString("table"), resultSet.getString("version"))
                        }
                    }
                }
            }
            return tableVersion
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
        return null
    }

    fun get(table: String): TableVersion? {
        try {
            var tableVersion: TableVersion? = null
            database.createConnection().use {
                connection -> connection.prepareStatement(
                    "SELECT id, table, version FROM table_version WHERE table = ?"
                ).use {
                    statement -> {
                        statement.setString(1, table)
                        val resultSet = statement.executeQuery()
                        if (resultSet.next()) {
                            tableVersion = TableVersion(resultSet.getInt("id"), resultSet.getString("table"), resultSet.getString("version"))
                        }
                    }
                }
            }
            return tableVersion
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
        return null
    }

    override fun delete(`object`: TableVersion) {
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