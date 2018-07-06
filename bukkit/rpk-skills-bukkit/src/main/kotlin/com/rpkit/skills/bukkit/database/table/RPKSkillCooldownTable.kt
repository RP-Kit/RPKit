package com.rpkit.skills.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.skills.bukkit.RPKSkillsBukkit
import com.rpkit.skills.bukkit.database.jooq.rpkit.Tables.RPKIT_SKILL_COOLDOWN
import com.rpkit.skills.bukkit.skills.RPKSkill
import com.rpkit.skills.bukkit.skills.RPKSkillCooldown
import com.rpkit.skills.bukkit.skills.RPKSkillProvider
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.SQLDialect
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType
import org.jooq.util.sqlite.SQLiteDataType
import java.sql.Timestamp


class RPKSkillCooldownTable(database: Database, private val plugin: RPKSkillsBukkit): Table<RPKSkillCooldown>(database, RPKSkillCooldown::class) {

    private val cache = database.cacheManager.createCache("rpk-skills-bukkit.rpkit_skill_cooldown.id", CacheConfigurationBuilder
            .newCacheConfigurationBuilder(Int::class.javaObjectType, RPKSkillCooldown::class.java,
                    ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())).build())


    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_SKILL_COOLDOWN)
                .column(RPKIT_SKILL_COOLDOWN.ID, if (database.dialect == SQLDialect.SQLITE) SQLiteDataType.INTEGER.identity(true) else SQLDataType.INTEGER.identity(true))
                .column(RPKIT_SKILL_COOLDOWN.CHARACTER_ID, SQLDataType.INTEGER)
                .column(RPKIT_SKILL_COOLDOWN.SKILL_NAME, SQLDataType.VARCHAR(256))
                .column(RPKIT_SKILL_COOLDOWN.COOLDOWN_TIMESTAMP, SQLDataType.TIMESTAMP)
                .constraints(
                        constraint("pk_rpkit_skill_cooldown").primaryKey(RPKIT_SKILL_COOLDOWN.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.2.0")
        }
    }

    override fun insert(entity: RPKSkillCooldown): Int {
        database.create
                .insertInto(
                        RPKIT_SKILL_COOLDOWN,
                        RPKIT_SKILL_COOLDOWN.CHARACTER_ID,
                        RPKIT_SKILL_COOLDOWN.SKILL_NAME,
                        RPKIT_SKILL_COOLDOWN.COOLDOWN_TIMESTAMP
                )
                .values(
                        entity.character.id,
                        entity.skill.name,
                        Timestamp(entity.cooldownTimestamp)
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache.put(id, entity)
        return id
    }

    override fun update(entity: RPKSkillCooldown) {
        database.create
                .update(RPKIT_SKILL_COOLDOWN)
                .set(RPKIT_SKILL_COOLDOWN.CHARACTER_ID, entity.character.id)
                .set(RPKIT_SKILL_COOLDOWN.SKILL_NAME, entity.skill.name)
                .set(RPKIT_SKILL_COOLDOWN.COOLDOWN_TIMESTAMP, Timestamp(entity.cooldownTimestamp))
                .where(RPKIT_SKILL_COOLDOWN.ID.eq(entity.id))
                .execute()
        cache.put(entity.id, entity)
    }

    override fun get(id: Int): RPKSkillCooldown? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            val result = database.create
                    .select(
                            RPKIT_SKILL_COOLDOWN.CHARACTER_ID,
                            RPKIT_SKILL_COOLDOWN.SKILL_NAME,
                            RPKIT_SKILL_COOLDOWN.COOLDOWN_TIMESTAMP
                    )
                    .from(RPKIT_SKILL_COOLDOWN)
                    .where(RPKIT_SKILL_COOLDOWN.ID.eq(id))
                    .fetchOne() ?: return null
            val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
            val characterId = result.get(RPKIT_SKILL_COOLDOWN.CHARACTER_ID)
            val character = characterProvider.getCharacter(characterId)
            val skillProvider = plugin.core.serviceManager.getServiceProvider(RPKSkillProvider::class)
            val skillName = result.get(RPKIT_SKILL_COOLDOWN.SKILL_NAME)
            val skill = skillProvider.getSkill(skillName)
            if (character != null && skill != null) {
                val skillCooldown = RPKSkillCooldown(
                        id,
                        character,
                        skill,
                        result.get(RPKIT_SKILL_COOLDOWN.COOLDOWN_TIMESTAMP).time
                )
                cache.put(id, skillCooldown)
                return skillCooldown
            } else {
                database.create
                        .deleteFrom(RPKIT_SKILL_COOLDOWN)
                        .where(RPKIT_SKILL_COOLDOWN.ID.eq(id))
                        .execute()
                cache.remove(id)
                return null
            }
        }
    }

    fun get(character: RPKCharacter, skill: RPKSkill): RPKSkillCooldown? {
        val result = database.create
                .select(RPKIT_SKILL_COOLDOWN.ID)
                .from(RPKIT_SKILL_COOLDOWN)
                .where(RPKIT_SKILL_COOLDOWN.CHARACTER_ID.eq(character.id))
                .and(RPKIT_SKILL_COOLDOWN.SKILL_NAME.eq(skill.name))
                .fetchOne() ?: return null
        return get(result.get(RPKIT_SKILL_COOLDOWN.ID))
    }

    override fun delete(entity: RPKSkillCooldown) {
        database.create
                .deleteFrom(RPKIT_SKILL_COOLDOWN)
                .where(RPKIT_SKILL_COOLDOWN.ID.eq(entity.id))
                .execute()
        cache.remove(entity.id)
    }

}