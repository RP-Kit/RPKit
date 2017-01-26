package com.rpkit.essentials.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.database.use
import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.essentials.bukkit.drink.RPKDrunkenness
import java.sql.PreparedStatement
import java.sql.Statement.RETURN_GENERATED_KEYS


class RPKDrunkennessTable(database: Database, private val plugin: RPKEssentialsBukkit): Table<RPKDrunkenness>(database, RPKDrunkenness::class) {

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement("CREATE TABLE IF NOT EXISTS rpkit_drunkenness(" +
                    "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                    "character_id INTEGER," +
                    "drunkenness INTEGER" +
                    ")").use(PreparedStatement::executeUpdate)
        }
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.1.0")
        }
    }

    override fun insert(entity: RPKDrunkenness): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO rpkit_drunkenness(character_id, drunkenness) VALUES(?, ?)",
                    RETURN_GENERATED_KEYS
            ).use { statement ->
                statement.setInt(1, entity.character.id)
                statement.setInt(2, entity.drunkenness)
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

    override fun update(entity: RPKDrunkenness) {
        database.createConnection().use { connection ->
            connection.prepareStatement("UPDATE rpkit_drunkenness SET character_id = ?, drunkenness = ? WHERE id = ?")
                    .use { statement ->
                        statement.setInt(1, entity.character.id)
                        statement.setInt(2, entity.drunkenness)
                        statement.setInt(3, entity.id)
                        statement.executeUpdate()
                    }
        }
    }

    override fun get(id: Int): RPKDrunkenness? {
        var drunkenness: RPKDrunkenness? = null
        database.createConnection().use { connection ->
            connection.prepareStatement("SELECT id, character_id, drunkenness FROM rpkit_drunkenness WHERE id = ?")
                    .use { statement ->
                        statement.setInt(1, id)
                        val resultSet = statement.executeQuery()
                        if (resultSet.next()) {
                            drunkenness = RPKDrunkenness(
                                    resultSet.getInt("id"),
                                    plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class).getCharacter(resultSet.getInt("character_id"))!!,
                                    resultSet.getInt("drunkenness")
                            )
                        }
            }
        }
        return drunkenness
    }

    fun get(character: RPKCharacter): RPKDrunkenness? {
        var drunkenness: RPKDrunkenness? = null
        database.createConnection().use { connection ->
            connection.prepareStatement("SELECT id, character_id, drunkenness FROM rpkit_drunkenness WHERE character_id = ?")
                    .use { statement ->
                        statement.setInt(1, character.id)
                        val resultSet = statement.executeQuery()
                        if (resultSet.next()) {
                            drunkenness = RPKDrunkenness(
                                    resultSet.getInt("id"),
                                    plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class).getCharacter(resultSet.getInt("character_id"))!!,
                                    resultSet.getInt("drunkenness")
                            )
                        }
                    }
        }
        return drunkenness
    }

    override fun delete(entity: RPKDrunkenness) {
        database.createConnection().use { connection ->
            connection.prepareStatement("DELETE FROM rpkit_drunkenness WHERE id = ?").use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
            }
        }
    }

}