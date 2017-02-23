package com.rpkit.experience.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.database.use
import com.rpkit.experience.bukkit.RPKExperienceBukkit
import com.rpkit.experience.bukkit.experience.RPKExperienceValue
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.sql.PreparedStatement
import java.sql.Statement


class RPKExperienceTable(database: Database, private val plugin: RPKExperienceBukkit) : Table<RPKExperienceValue>(database, RPKExperienceValue::class) {

    private val cacheManager: CacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
    private val cache: Cache<Int, RPKExperienceValue> = cacheManager.createCache("cache", CacheConfigurationBuilder
            .newCacheConfigurationBuilder(Int::class.javaObjectType, RPKExperienceValue::class.java,
                    ResourcePoolsBuilder.heap(plugin.server.maxPlayers * 2L)).build())
    private val characterCache: Cache<Int, RPKExperienceValue> = cacheManager.createCache("characterCache", CacheConfigurationBuilder
            .newCacheConfigurationBuilder(Int::class.javaObjectType, RPKExperienceValue::class.java,
                    ResourcePoolsBuilder.heap(plugin.server.maxPlayers * 2L)).build())

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS rpkit_experience(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "character_id INTEGER," +
                            "value INTEGER" +
                    ")"
            ).use(PreparedStatement::executeUpdate)
        }
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.2.0")
        }
    }

    override fun delete(entity: RPKExperienceValue) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM rpkit_experience WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
                cache.remove(entity.id)
            }
        }
    }

    override fun get(id: Int): RPKExperienceValue? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            var experienceValue: RPKExperienceValue? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, character_id, value FROM rpkit_experience WHERE id = ?"
                ).use { statement ->
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        experienceValue = RPKExperienceValue(
                                resultSet.getInt("id"),
                                plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class.java).getCharacter(resultSet.getInt("character_id"))!!,
                                resultSet.getInt("value")
                        )
                        cache.put(id, experienceValue)
                    }
                }
            }
            return experienceValue
        }
    }

    fun get(character: RPKCharacter): RPKExperienceValue? {
        if (characterCache.containsKey(character.id)) {
            return characterCache.get(character.id)
        } else {
            var experienceValue: RPKExperienceValue? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id FROM rpkit_experience WHERE character_id = ?"
                ).use { statement ->
                    statement.setInt(1, character.id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        experienceValue = get(resultSet.getInt("id"))
                        characterCache.put(character.id, experienceValue)
                    }
                }
            }
            return experienceValue
        }
    }

    override fun insert(entity: RPKExperienceValue): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO rpkit_experience(character_id, value) VALUES(?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            ).use { statement ->
                statement.setInt(1, entity.character.id)
                statement.setInt(2, entity.value)
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

    override fun update(entity: RPKExperienceValue) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE rpkit_experience SET character_id = ?, value = ? WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.character.id)
                statement.setInt(2, entity.value)
                statement.setInt(3, entity.id)
                statement.executeUpdate()
                cache.put(entity.id, entity)
            }
        }
    }

}