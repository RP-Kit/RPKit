package com.seventh_root.elysium.characters.bukkit.database.table

import com.seventh_root.elysium.characters.bukkit.race.BukkitRace
import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.database.Table
import com.seventh_root.elysium.core.database.use
import java.sql.SQLException
import java.sql.Statement.RETURN_GENERATED_KEYS

class BukkitRaceTable @Throws(SQLException::class)
constructor(database: Database) : Table<BukkitRace>(database, BukkitRace::class.java) {

    override fun create() {
        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS bukkit_race(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "name VARCHAR(256)" +
                        ")").use({ statement -> statement.executeUpdate() })
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "0.1.0")
        }
    }

    override fun insert(`object`: BukkitRace): Int {
        try {
            var id = 0
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "INSERT INTO bukkit_race(name) VALUES(?)",
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

    override fun update(`object`: BukkitRace) {
        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "UPDATE bukkit_race SET name = ? WHERE id = ?").use({ statement ->
                    statement.setString(1, `object`.name)
                    statement.setInt(2, `object`.id)
                    statement.executeUpdate()
                })
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

    }

    override fun get(id: Int): BukkitRace? {
        try {
            var race: BukkitRace? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, name FROM bukkit_race WHERE id = ?").use({ statement ->
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        race = BukkitRace(resultSet.getInt("id"), resultSet.getString("name"))
                    }
                })
            }
            return race
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

        return null
    }

    operator fun get(name: String): BukkitRace? {
        try {
            var race: BukkitRace? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, name FROM bukkit_race WHERE name = ?").use({ statement ->
                    statement.setString(1, name)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        race = BukkitRace(resultSet.getInt("id"), resultSet.getString("name"))
                    }
                })
            }
            return race
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

        return null
    }

    override fun delete(`object`: BukkitRace) {
        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "DELETE FROM bukkit_race WHERE id = ?").use({ statement ->
                    statement.setInt(1, `object`.id)
                    statement.executeUpdate()
                })
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

    }

}
