package com.rpkit.permissions.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.permissions.bukkit.RPKPermissionsBukkit
import com.rpkit.permissions.bukkit.database.jooq.rpkit.Tables.RPKIT_CHARACTER_GROUP
import com.rpkit.permissions.bukkit.group.RPKCharacterGroup
import com.rpkit.permissions.bukkit.group.RPKGroupProvider
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType


class RPKCharacterGroupTable(database: Database, private val plugin: RPKPermissionsBukkit): Table<RPKCharacterGroup>(database, RPKCharacterGroup::class) {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_character_group.id.enabled")) {
        database.cacheManager.createCache("rpk-permissions-bukkit.rpkit_character_group.id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKCharacterGroup::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_character_group.id.size"))))
    } else {
        null
    }

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_CHARACTER_GROUP)
                .column(RPKIT_CHARACTER_GROUP.ID, SQLDataType.INTEGER.identity(true))
                .column(RPKIT_CHARACTER_GROUP.CHARACTER_ID, SQLDataType.INTEGER)
                .column(RPKIT_CHARACTER_GROUP.GROUP_NAME, SQLDataType.VARCHAR(256))
                .constraints(
                        constraint("pk_rpkit_character_group").primaryKey(RPKIT_CHARACTER_GROUP.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.5.0")
        }
    }

    override fun insert(entity: RPKCharacterGroup): Int {
        database.create
                .insertInto(
                        RPKIT_CHARACTER_GROUP,
                        RPKIT_CHARACTER_GROUP.CHARACTER_ID,
                        RPKIT_CHARACTER_GROUP.GROUP_NAME
                )
                .values(
                        entity.character.id,
                        entity.group.name
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
        return id
    }

    override fun update(entity: RPKCharacterGroup) {
        database.create
                .update(RPKIT_CHARACTER_GROUP)
                .set(RPKIT_CHARACTER_GROUP.CHARACTER_ID, entity.character.id)
                .set(RPKIT_CHARACTER_GROUP.GROUP_NAME, entity.group.name)
                .where(RPKIT_CHARACTER_GROUP.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
    }

    override fun get(id: Int): RPKCharacterGroup? {
        if (cache?.containsKey(id) == true) {
            return cache.get(id)
        } else {
            val result = database.create
                    .select(
                            RPKIT_CHARACTER_GROUP.CHARACTER_ID,
                            RPKIT_CHARACTER_GROUP.GROUP_NAME
                    )
                    .from(RPKIT_CHARACTER_GROUP)
                    .where(RPKIT_CHARACTER_GROUP.ID.eq(id))
                    .fetchOne() ?: return null
            val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
            val characterId = result.get(RPKIT_CHARACTER_GROUP.CHARACTER_ID)
            val character = characterProvider.getCharacter(characterId)
            val groupProvider = plugin.core.serviceManager.getServiceProvider(RPKGroupProvider::class)
            val groupName = result.get(RPKIT_CHARACTER_GROUP.GROUP_NAME)
            val group = groupProvider.getGroup(groupName)
            if (character != null && group != null) {
                val characterGroup = RPKCharacterGroup(
                        id,
                        character,
                        group
                )
                cache?.put(id, characterGroup)
                return characterGroup
            } else {
                database.create
                        .deleteFrom(RPKIT_CHARACTER_GROUP)
                        .where(RPKIT_CHARACTER_GROUP.ID.eq(id))
                        .execute()
                return null
            }
        }
    }

    fun get(character: RPKCharacter): List<RPKCharacterGroup> {
        val results = database.create
                .select(RPKIT_CHARACTER_GROUP.ID)
                .from(RPKIT_CHARACTER_GROUP)
                .where(RPKIT_CHARACTER_GROUP.CHARACTER_ID.eq(character.id))
                .fetch()
        return results.map { result -> get(result.get(RPKIT_CHARACTER_GROUP.ID)) }
                .filterNotNull()
    }

    override fun delete(entity: RPKCharacterGroup) {
        database.create
                .deleteFrom(RPKIT_CHARACTER_GROUP)
                .where(RPKIT_CHARACTER_GROUP.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
    }

}