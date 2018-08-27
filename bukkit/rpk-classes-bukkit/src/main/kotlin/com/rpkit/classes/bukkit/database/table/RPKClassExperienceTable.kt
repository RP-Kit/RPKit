package com.rpkit.classes.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.classes.bukkit.RPKClassesBukkit
import com.rpkit.classes.bukkit.classes.RPKClass
import com.rpkit.classes.bukkit.classes.RPKClassExperience
import com.rpkit.classes.bukkit.classes.RPKClassProvider
import com.rpkit.classes.bukkit.database.jooq.rpkit.Tables.RPKIT_CLASS_EXPERIENCE
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType


class RPKClassExperienceTable(database: Database, private val plugin: RPKClassesBukkit): Table<RPKClassExperience>(database, RPKClassExperience::class) {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_class_experience.id.enabled")) {
        database.cacheManager.createCache("rpk-classes-bukkit.rpkit_class_experience.id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKClassExperience::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_class_experience.id.size"))))
    } else {
        null
    }

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_CLASS_EXPERIENCE)
                .column(RPKIT_CLASS_EXPERIENCE.ID, SQLDataType.INTEGER.identity(true))
                .column(RPKIT_CLASS_EXPERIENCE.CHARACTER_ID, SQLDataType.INTEGER)
                .column(RPKIT_CLASS_EXPERIENCE.CLASS_NAME, SQLDataType.VARCHAR(256))
                .column(RPKIT_CLASS_EXPERIENCE.EXPERIENCE, SQLDataType.INTEGER)
                .constraints(
                        constraint("pk_rpkit_class_experience").primaryKey(RPKIT_CLASS_EXPERIENCE.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.2.0")
        }
    }

    override fun insert(entity: RPKClassExperience): Int {
        database.create
                .insertInto(
                        RPKIT_CLASS_EXPERIENCE,
                        RPKIT_CLASS_EXPERIENCE.CHARACTER_ID,
                        RPKIT_CLASS_EXPERIENCE.CLASS_NAME,
                        RPKIT_CLASS_EXPERIENCE.EXPERIENCE
                )
                .values(
                        entity.character.id,
                        entity.clazz.name,
                        entity.experience
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
        return id
    }

    override fun update(entity: RPKClassExperience) {
        database.create
                .update(RPKIT_CLASS_EXPERIENCE)
                .set(RPKIT_CLASS_EXPERIENCE.CHARACTER_ID, entity.character.id)
                .set(RPKIT_CLASS_EXPERIENCE.CLASS_NAME, entity.clazz.name)
                .set(RPKIT_CLASS_EXPERIENCE.EXPERIENCE, entity.experience)
                .where(RPKIT_CLASS_EXPERIENCE.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
    }

    override fun get(id: Int): RPKClassExperience? {
        if (cache?.containsKey(id) == true) {
            return cache.get(id)
        } else {
            val result = database.create
                    .select(
                            RPKIT_CLASS_EXPERIENCE.CHARACTER_ID,
                            RPKIT_CLASS_EXPERIENCE.CLASS_NAME,
                            RPKIT_CLASS_EXPERIENCE.EXPERIENCE
                    )
                    .from(RPKIT_CLASS_EXPERIENCE)
                    .fetchOne() ?: return null
            val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
            val characterId = result.get(RPKIT_CLASS_EXPERIENCE.CHARACTER_ID)
            val character = characterProvider.getCharacter(characterId)
            val classProvider = plugin.core.serviceManager.getServiceProvider(RPKClassProvider::class)
            val className = result.get(RPKIT_CLASS_EXPERIENCE.CLASS_NAME)
            val clazz = classProvider.getClass(className)
            if (character != null && clazz != null) {
                val classExperience = RPKClassExperience(
                        id,
                        character,
                        clazz,
                        result.get(RPKIT_CLASS_EXPERIENCE.EXPERIENCE)
                )
                cache?.put(id, classExperience)
                return classExperience
            } else {
                database.create
                        .deleteFrom(RPKIT_CLASS_EXPERIENCE)
                        .where(RPKIT_CLASS_EXPERIENCE.ID.eq(id))
                        .execute()
                return null
            }
        }
    }

    fun get(character: RPKCharacter, clazz: RPKClass): RPKClassExperience? {
        val result = database.create
                .select(RPKIT_CLASS_EXPERIENCE.ID)
                .from(RPKIT_CLASS_EXPERIENCE)
                .where(RPKIT_CLASS_EXPERIENCE.CHARACTER_ID.eq(character.id))
                .and(RPKIT_CLASS_EXPERIENCE.CLASS_NAME.eq(clazz.name))
                .fetchOne() ?: return null
        return get(result.get(RPKIT_CLASS_EXPERIENCE.ID))
    }

    override fun delete(entity: RPKClassExperience) {
        database.create
                .deleteFrom(RPKIT_CLASS_EXPERIENCE)
                .where(RPKIT_CLASS_EXPERIENCE.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
    }

}