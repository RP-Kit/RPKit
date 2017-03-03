package com.rpkit.classes.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.classes.bukkit.RPKClassesBukkit
import com.rpkit.classes.bukkit.classes.RPKCharacterClass
import com.rpkit.classes.bukkit.classes.RPKClassProvider
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.database.use
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.sql.PreparedStatement
import java.sql.Statement.RETURN_GENERATED_KEYS


class RPKCharacterClassTable(database: Database, private val plugin: RPKClassesBukkit): Table<RPKCharacterClass>(database, RPKCharacterClass::class) {

    private val cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
    private val cache = cacheManager.createCache("cache",
            CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKCharacterClass::class.java,
                    ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())))

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS rpkit_character_class(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "character_id INTEGER," +
                            "class_name VARCHAR(256)" +
                    ")"
            ).use(PreparedStatement::executeUpdate)
        }
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.2.0")
        }
    }

    override fun insert(entity: RPKCharacterClass): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO rpkit_character_class(character_id, class_name) VALUES(?, ?)",
                    RETURN_GENERATED_KEYS
            ).use { statement ->
                statement.setInt(1, entity.character.id)
                statement.setString(2, entity.clazz.name)
                statement.executeUpdate()
                val generatedKeys = statement.generatedKeys
                if (generatedKeys.next()) {
                    id = generatedKeys.getInt(1)
                    entity.id = id
                    cache.put(id, entity)
                }
            }
        }
        return id
    }

    override fun update(entity: RPKCharacterClass) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE rpkit_character_class SET character_id = ?, class_name = ? WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.character.id)
                statement.setString(2, entity.clazz.name)
                statement.setInt(3, entity.id)
                statement.executeUpdate()
                cache.put(entity.id, entity)
            }
        }
    }

    override fun get(id: Int): RPKCharacterClass? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            var characterClass: RPKCharacterClass? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, character_id, class_name FROM rpkit_character_class WHERE id = ?"
                ).use { statement ->
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val character = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class).getCharacter(resultSet.getInt("character_id"))
                        val clazz = plugin.core.serviceManager.getServiceProvider(RPKClassProvider::class).getClass(resultSet.getString("class_name"))
                        if (character != null && clazz != null) {
                            val finalCharacterClass = RPKCharacterClass(
                                    resultSet.getInt("id"),
                                    character,
                                    clazz
                            )
                            cache.put(finalCharacterClass.id, finalCharacterClass)
                            characterClass = finalCharacterClass
                        } else {
                            connection.prepareStatement(
                                    "DELETE FROM rpkit_character_class WHERE id = ?"
                            ).use { statement ->
                                statement.setInt(1, resultSet.getInt("id"))
                                statement.executeUpdate()
                                cache.remove(resultSet.getInt("id"))
                            }
                        }
                    }
                }
            }
            return characterClass
        }
    }

    fun get(character: RPKCharacter): RPKCharacterClass? {
        var characterClass: RPKCharacterClass? = null
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id FROM rpkit_character_class WHERE character_id = ?"
            ).use { statement ->
                statement.setInt(1, character.id)
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    characterClass = get(resultSet.getInt("id"))
                }
            }
        }
        return characterClass
    }

    override fun delete(entity: RPKCharacterClass) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM rpkit_character_class WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
                cache.remove(entity.id)
            }
        }
    }
}