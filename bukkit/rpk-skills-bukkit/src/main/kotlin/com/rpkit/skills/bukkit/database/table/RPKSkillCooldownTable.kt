package com.rpkit.skills.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.database.use
import com.rpkit.skills.bukkit.RPKSkillsBukkit
import com.rpkit.skills.bukkit.skills.RPKSkill
import com.rpkit.skills.bukkit.skills.RPKSkillCooldown
import com.rpkit.skills.bukkit.skills.RPKSkillProvider
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.sql.PreparedStatement
import java.sql.Statement.RETURN_GENERATED_KEYS
import java.sql.Timestamp


class RPKSkillCooldownTable(database: Database, private val plugin: RPKSkillsBukkit): Table<RPKSkillCooldown>(database, RPKSkillCooldown::class) {

    private val cacheManager: CacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
    private val cache: Cache<Int, RPKSkillCooldown> = cacheManager.createCache("cache", CacheConfigurationBuilder
            .newCacheConfigurationBuilder(Int::class.javaObjectType, RPKSkillCooldown::class.java,
                    ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())).build())


    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS rpkit_skill_cooldown(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "character_id INTEGER," +
                            "skill_name VARCHAR(256)," +
                            "cooldown_timestamp DATETIME" +
                    ")"
            ).use(PreparedStatement::executeUpdate)
        }
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.2.0")
        }
    }

    override fun insert(entity: RPKSkillCooldown): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO rpkit_skill_cooldown(character_id, skill_name, cooldown_timestamp) VALUES(?, ?, ?)",
                    RETURN_GENERATED_KEYS
            ).use { statement ->
                statement.setInt(1, entity.character.id)
                statement.setString(2, entity.skill.name)
                statement.setTimestamp(3, Timestamp(entity.cooldownTimestamp))
                statement.executeUpdate()
                val generatedKeys = statement.generatedKeys
                if (generatedKeys.next()) {
                    entity.id = id
                    id = generatedKeys.getInt(1)
                    cache.put(id, entity)
                }
            }
        }
        return id
    }

    override fun update(entity: RPKSkillCooldown) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE rpkit_skill_cooldown SET character_id = ?, skill_name = ?, cooldown_timestamp = ? WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.character.id)
                statement.setString(2, entity.skill.name)
                statement.setTimestamp(3, Timestamp(entity.cooldownTimestamp))
                statement.setInt(4, entity.id)
                statement.executeUpdate()
                cache.put(entity.id, entity)
            }
        }
    }

    override fun get(id: Int): RPKSkillCooldown? {
        var skillCooldown: RPKSkillCooldown? = null
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id, character_id, skill_name, cooldown_timestamp FROM rpkit_skill_cooldown WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, id)
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                    val skillProvider = plugin.core.serviceManager.getServiceProvider(RPKSkillProvider::class)
                    val character = characterProvider.getCharacter(resultSet.getInt("character_id"))
                    val skill = skillProvider.getSkill(resultSet.getString("skill_name"))
                    val cooldownTimestamp = resultSet.getTimestamp("cooldown_timestamp")
                    if (character != null && skill != null) {
                        val finalSkillCooldown = RPKSkillCooldown(
                                resultSet.getInt("id"),
                                character,
                                skill,
                                cooldownTimestamp.time
                        )
                        skillCooldown = finalSkillCooldown
                    } else {
                        connection.prepareStatement(
                                "DELETE FROM rpkit_skill_cooldown WHERE id = ?"
                        ).use { statement ->
                            statement.setInt(1, resultSet.getInt("id"))
                            statement.executeUpdate()
                        }
                    }
                }
            }
        }
        return skillCooldown
    }

    fun get(character: RPKCharacter, skill: RPKSkill): RPKSkillCooldown? {
        var skillCooldown: RPKSkillCooldown? = null
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id, character_id, skill_name, cooldown_timestamp FROM rpkit_skill_cooldown WHERE character_id = ? AND skill_name = ?"
            ).use { statement ->
                statement.setInt(1, character.id)
                statement.setString(2, skill.name)
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    val cooldownTimestamp = resultSet.getTimestamp("cooldown_timestamp")
                    val finalSkillCooldown = RPKSkillCooldown(
                            resultSet.getInt("id"),
                            character,
                            skill,
                            cooldownTimestamp.time
                    )
                    skillCooldown = finalSkillCooldown
                }
            }
        }
        return skillCooldown
    }

    override fun delete(entity: RPKSkillCooldown) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM rpkit_skill_cooldown WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
            }
        }
    }

}