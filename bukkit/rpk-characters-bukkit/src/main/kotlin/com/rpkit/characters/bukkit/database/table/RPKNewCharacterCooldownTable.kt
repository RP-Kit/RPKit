package com.rpkit.characters.bukkit.database.table

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.database.jooq.rpkit.Tables.RPKIT_NEW_CHARACTER_COOLDOWN
import com.rpkit.characters.bukkit.newcharactercooldown.RPKNewCharacterCooldown
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.SQLDialect
import org.jooq.impl.DSL.constraint
import org.jooq.impl.DSL.field
import org.jooq.impl.SQLDataType
import org.jooq.util.sqlite.SQLiteDataType
import java.sql.Date


class RPKNewCharacterCooldownTable(database: Database, private val plugin: RPKCharactersBukkit): Table<RPKNewCharacterCooldown>(database, RPKNewCharacterCooldown::class) {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_new_character_cooldown.id.enabled")) {
        database.cacheManager.createCache("rpk-characters-bukkit.rpkit_new_character_cooldown.id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKNewCharacterCooldown::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_new_character_cooldown.id.size"))).build())
    } else {
        null
    }

    private val profileCache = if (plugin.config.getBoolean("caching.rpkit_new_character_cooldown.profile_id.enabled")) {
        database.cacheManager.createCache("rpk-characters-bukkit.rpkit_new_character_cooldown.profile_id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, Int::class.javaObjectType,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_new_character_cooldown.profile_id.size"))).build())
    } else {
        null
    }


    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_NEW_CHARACTER_COOLDOWN)
                .column(RPKIT_NEW_CHARACTER_COOLDOWN.ID, if (database.dialect == SQLDialect.SQLITE) SQLiteDataType.INTEGER.identity(true) else SQLDataType.INTEGER.identity(true))
                .column(RPKIT_NEW_CHARACTER_COOLDOWN.PROFILE_ID, SQLDataType.INTEGER)
                .column(RPKIT_NEW_CHARACTER_COOLDOWN.COOLDOWN_TIMESTAMP, SQLDataType.DATE)
                .constraints(
                        constraint("pk_rpkit_new_character_cooldown").primaryKey(RPKIT_NEW_CHARACTER_COOLDOWN.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.3.0")
        }
        if (database.getTableVersion(this) == "1.1.0") {
            database.create
                    .truncate(RPKIT_NEW_CHARACTER_COOLDOWN)
                    .execute()
            database.create
                    .alterTable(RPKIT_NEW_CHARACTER_COOLDOWN)
                    .dropColumn(field("player_id"))
                    .execute()
            database.create
                    .alterTable(RPKIT_NEW_CHARACTER_COOLDOWN)
                    .addColumn(RPKIT_NEW_CHARACTER_COOLDOWN.PROFILE_ID, SQLDataType.INTEGER)
                    .execute()
            database.setTableVersion(this, "1.3.0")
        }
    }

    override fun insert(entity: RPKNewCharacterCooldown): Int {
        database.create
                .insertInto(
                        RPKIT_NEW_CHARACTER_COOLDOWN,
                        RPKIT_NEW_CHARACTER_COOLDOWN.PROFILE_ID,
                        RPKIT_NEW_CHARACTER_COOLDOWN.COOLDOWN_TIMESTAMP
                )
                .values(
                        entity.profile.id,
                        Date(entity.cooldownTimestamp)
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
        profileCache?.put(entity.profile.id, id)
        return id
    }

    override fun update(entity: RPKNewCharacterCooldown) {
        database.create
                .update(RPKIT_NEW_CHARACTER_COOLDOWN)
                .set(RPKIT_NEW_CHARACTER_COOLDOWN.PROFILE_ID, entity.profile.id)
                .set(RPKIT_NEW_CHARACTER_COOLDOWN.COOLDOWN_TIMESTAMP, Date(entity.cooldownTimestamp))
                .where(RPKIT_NEW_CHARACTER_COOLDOWN.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
        profileCache?.put(entity.profile.id, entity.id)
    }

    override fun get(id: Int): RPKNewCharacterCooldown? {
        if (cache?.containsKey(id) == true) {
            return cache[id]
        } else {
            val result = database.create
                    .select(
                            RPKIT_NEW_CHARACTER_COOLDOWN.PROFILE_ID,
                            RPKIT_NEW_CHARACTER_COOLDOWN.COOLDOWN_TIMESTAMP
                    )
                    .from(RPKIT_NEW_CHARACTER_COOLDOWN)
                    .where(RPKIT_NEW_CHARACTER_COOLDOWN.ID.eq(id))
                    .fetchOne() ?: return null
            val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
            val profile = profileProvider.getProfile(result.get(RPKIT_NEW_CHARACTER_COOLDOWN.PROFILE_ID))
            if (profile != null) {
                val newCharacterCooldown = RPKNewCharacterCooldown(
                        result.get(RPKIT_NEW_CHARACTER_COOLDOWN.ID),
                        profile,
                        result.get(RPKIT_NEW_CHARACTER_COOLDOWN.COOLDOWN_TIMESTAMP).time
                )
                cache?.put(id, newCharacterCooldown)
                profileCache?.put(newCharacterCooldown.profile.id, id)
                return newCharacterCooldown
            } else {
                database.create
                        .deleteFrom(RPKIT_NEW_CHARACTER_COOLDOWN)
                        .where(RPKIT_NEW_CHARACTER_COOLDOWN.ID.eq(id))
                        .execute()
                return null
            }
        }
    }

    fun get(profile: RPKProfile): RPKNewCharacterCooldown? {
        if (profileCache?.containsKey(profile.id) == true) {
            return get(profileCache[profile.id])
        } else {
            val result = database.create
                    .select(
                            RPKIT_NEW_CHARACTER_COOLDOWN.ID,
                            RPKIT_NEW_CHARACTER_COOLDOWN.COOLDOWN_TIMESTAMP
                    )
                    .from(RPKIT_NEW_CHARACTER_COOLDOWN)
                    .where(RPKIT_NEW_CHARACTER_COOLDOWN.PROFILE_ID.eq(profile.id))
                    .fetchOne() ?: return null
            val newCharacterCooldown = RPKNewCharacterCooldown(
                    result.get(RPKIT_NEW_CHARACTER_COOLDOWN.ID),
                    profile,
                    result.get(RPKIT_NEW_CHARACTER_COOLDOWN.COOLDOWN_TIMESTAMP).time
            )
            cache?.put(newCharacterCooldown.id, newCharacterCooldown)
            profileCache?.put(profile.id, newCharacterCooldown.id)
            return newCharacterCooldown
        }
    }

    override fun delete(entity: RPKNewCharacterCooldown) {
        database.create
                .deleteFrom(RPKIT_NEW_CHARACTER_COOLDOWN)
                .where(RPKIT_NEW_CHARACTER_COOLDOWN.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
        profileCache?.remove(entity.profile.id)
    }

}