package com.rpkit.classes.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.classes.bukkit.RPKClassesBukkit
import com.rpkit.classes.bukkit.classes.RPKClass
import com.rpkit.classes.bukkit.classes.RPKClassExperience
import com.rpkit.classes.bukkit.classes.RPKClassProvider
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.database.use
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.sql.PreparedStatement
import java.sql.Statement.RETURN_GENERATED_KEYS


class RPKClassExperienceTable(database: Database, private val plugin: RPKClassesBukkit): Table<RPKClassExperience>(database, RPKClassExperience::class) {

    private val cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
    private val cache = cacheManager.createCache("cache",
            CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKClassExperience::class.java,
                    ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())))

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS rpkit_class_experience(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "character_id INTEGER," +
                            "class_name VARCHAR(256)," +
                            "experience INTEGER" +
                    ")"
            ).use(PreparedStatement::executeUpdate)
        }
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.2.0")
        }
    }

    override fun insert(entity: RPKClassExperience): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO rpkit_class_experience(character_id, class_name, experience) VALUES(?, ?, ?)",
                    RETURN_GENERATED_KEYS
            ).use { statement ->
                statement.setInt(1, entity.character.id)
                statement.setString(2, entity.clazz.name)
                statement.setInt(3, entity.experience)
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

    override fun update(entity: RPKClassExperience) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE rpkit_class_experience SET character_id = ?, class_name = ?, experience = ? WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.character.id)
                statement.setString(2, entity.clazz.name)
                statement.setInt(3, entity.experience)
                statement.setInt(4, entity.id)
                statement.executeUpdate()
                cache.put(entity.id, entity)
            }
        }
    }

    override fun get(id: Int): RPKClassExperience? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            var classExperience: RPKClassExperience? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, character_id, class_name, experience FROM rpkit_class_experience WHERE id = ?"
                ).use { statement ->
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val character = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class).getCharacter(resultSet.getInt("character_id"))
                        val clazz = plugin.core.serviceManager.getServiceProvider(RPKClassProvider::class).getClass(resultSet.getString("class_name"))
                        if (character != null && clazz != null) {
                            val finalClassExperience = RPKClassExperience(
                                    id,
                                    character,
                                    clazz,
                                    resultSet.getInt("experience")
                            )
                            cache.put(id, finalClassExperience)
                            classExperience = finalClassExperience
                        } else {
                            connection.prepareStatement(
                                    "DELETE FROM rpkit_class_experience WHERE id = ?"
                            ).use { statement ->
                                statement.setInt(1, id)
                                statement.executeUpdate()
                                cache.remove(id)
                            }
                        }
                    }
                }
            }
            return classExperience
        }
    }

    fun get(character: RPKCharacter, clazz: RPKClass): RPKClassExperience? {
        var classExperience: RPKClassExperience? = null
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id FROM rpkit_class_experience WHERE character_id = ? AND class_name = ?"
            ).use { statement ->
                statement.setInt(1, character.id)
                statement.setString(2, clazz.name)
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    classExperience = get(resultSet.getInt("id"))
                }
            }
        }
        return classExperience
    }

    override fun delete(entity: RPKClassExperience) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM rpkit_class_experience WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
                cache.remove(entity.id)
            }
        }
    }

}