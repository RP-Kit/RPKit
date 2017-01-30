package com.rpkit.locks.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.core.bukkit.util.toByteArray
import com.rpkit.core.bukkit.util.toItemStackArray
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.database.use
import com.rpkit.locks.bukkit.RPKLocksBukkit
import com.rpkit.locks.bukkit.keyring.RPKKeyring
import java.sql.PreparedStatement
import java.sql.Statement.RETURN_GENERATED_KEYS


class RPKKeyringTable(database: Database, private val plugin: RPKLocksBukkit): Table<RPKKeyring>(database, RPKKeyring::class) {
    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS rpkit_keyring(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "character_id INTEGER," +
                            "items BLOB" +
                    ")"
            ).use(PreparedStatement::executeUpdate)
        }
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.1.0")
        }
    }

    override fun insert(entity: RPKKeyring): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement("INSERT INTO rpkit_keyring(character_id, items) VALUES(?, ?)",
                    RETURN_GENERATED_KEYS).use { statement ->
                statement.setInt(1, entity.character.id)
                statement.setBytes(2, entity.items.toTypedArray().toByteArray())
                statement.executeUpdate()
                val generatedKeys = statement.generatedKeys
                if (generatedKeys.next()) {
                    id = generatedKeys.getInt(1)
                    entity.id = id
                }
            }
        }
        return id
    }

    override fun update(entity: RPKKeyring) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE rpkit_keyring SET character_id = ?, items = ? WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.character.id)
                statement.setBytes(2, entity.items.toTypedArray().toByteArray())
                statement.setInt(3, entity.id)
                statement.executeUpdate()
            }
        }
    }

    override fun get(id: Int): RPKKeyring? {
        var keyring: RPKKeyring? = null
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id, character_id, items FROM rpkit_keyring WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, id)
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    keyring = RPKKeyring(
                            resultSet.getInt("id"),
                            plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class).getCharacter(resultSet.getInt("character_id"))!!,
                            resultSet.getBytes("items").toItemStackArray().toMutableList()
                    )
                }
            }
        }
        return keyring
    }

    fun get(character: RPKCharacter): RPKKeyring? {
        var keyring: RPKKeyring? = null
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id, character_id, items FROM rpkit_keyring WHERE character_id = ?"
            ).use { statement ->
                statement.setInt(1, character.id)
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    keyring = RPKKeyring(
                            resultSet.getInt("id"),
                            plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class).getCharacter(resultSet.getInt("character_id"))!!,
                            resultSet.getBytes("items").toItemStackArray().toMutableList()
                    )
                }
            }
        }
        return keyring
    }

    override fun delete(entity: RPKKeyring) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM rpkit_keyring WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
            }
        }
    }
}