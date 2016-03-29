package com.seventh_root.elysium.characters.bukkit.database.table

import com.seventh_root.elysium.characters.bukkit.gender.BukkitGender
import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.database.Table
import com.seventh_root.elysium.core.database.use
import java.sql.SQLException
import java.sql.Statement.RETURN_GENERATED_KEYS

class BukkitGenderTable @Throws(SQLException::class)
constructor(database: Database) : Table<BukkitGender>(database, BukkitGender::class.java) {

    override fun create() {
        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS bukkit_gender(" +
                                "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                                "name VARCHAR(256)" +
                                ")").use({ statement -> statement.executeUpdate() })
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

    }

    override fun insert(`object`: BukkitGender): Int {
        try {
            var id = 0
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "INSERT INTO bukkit_gender(name) VALUES(?)",
                        RETURN_GENERATED_KEYS).use({ statement ->
                    statement.setString(1, `object`.name)
                    statement.executeUpdate()
                    val generatedKeys = statement.generatedKeys
                    if (generatedKeys.next()) {
                        id = generatedKeys.getInt(1)
                        `object`.id = id
                    }
                })
            }
            return id
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

        return 0
    }

    override fun update(`object`: BukkitGender) {
        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "UPDATE bukkit_gender SET name = ? WHERE id = ?").use({ statement ->
                    statement.setString(1, `object`.name)
                    statement.setInt(2, `object`.id)
                    statement.executeUpdate()
                })
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

    }

    override fun get(id: Int): BukkitGender? {
        try {
            var gender: BukkitGender? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, name FROM bukkit_gender WHERE id = ?").use({ statement ->
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        gender = BukkitGender(resultSet.getInt("id"), resultSet.getString("name"))
                    }
                })
            }
            return gender
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

        return null
    }

    operator fun get(name: String): BukkitGender? {
        try {
            var gender: BukkitGender? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, name FROM bukkit_gender WHERE name = ?").use({ statement ->
                    statement.setString(1, name)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        gender = BukkitGender(resultSet.getInt("id"), resultSet.getString("name"))
                    }
                })
            }
            return gender
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

        return null
    }

    override fun delete(`object`: BukkitGender) {
        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "DELETE FROM bukkit_gender WHERE id = ?").use({ statement ->
                    statement.setInt(1, `object`.id)
                    statement.executeUpdate()
                })
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

    }

}
