package com.rpkit.permissions.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.permissions.bukkit.RPKPermissionsBukkit
import com.rpkit.permissions.bukkit.database.jooq.rpkit.Tables.RPKIT_PROFILE_GROUP
import com.rpkit.permissions.bukkit.group.RPKGroupProvider
import com.rpkit.permissions.bukkit.group.RPKProfileGroup
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType


class RPKProfileGroupTable(database: Database, private val plugin: RPKPermissionsBukkit): Table<RPKProfileGroup>(database, RPKProfileGroup::class) {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_profile_group.id.enabled")) {
        database.cacheManager.createCache("rpk-permissions-bukkit.rpkit_profile_group.id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKProfileGroup::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_profile_group.id.size"))))
    } else {
        null
    }

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_PROFILE_GROUP)
                .column(RPKIT_PROFILE_GROUP.ID, SQLDataType.INTEGER.identity(true))
                .column(RPKIT_PROFILE_GROUP.PROFILE_ID, SQLDataType.INTEGER)
                .column(RPKIT_PROFILE_GROUP.GROUP_NAME, SQLDataType.VARCHAR(256))
                .constraints(
                        constraint("pk_rpkit_profile_group").primaryKey(RPKIT_PROFILE_GROUP.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.3.0")
        }
    }

    override fun insert(entity: RPKProfileGroup): Int {
        database.create
                .insertInto(
                        RPKIT_PROFILE_GROUP,
                        RPKIT_PROFILE_GROUP.PROFILE_ID,
                        RPKIT_PROFILE_GROUP.GROUP_NAME
                )
                .values(
                        entity.profile.id,
                        entity.group.name
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
        return id
    }

    override fun update(entity: RPKProfileGroup) {
        database.create
                .update(RPKIT_PROFILE_GROUP)
                .set(RPKIT_PROFILE_GROUP.PROFILE_ID, entity.profile.id)
                .set(RPKIT_PROFILE_GROUP.GROUP_NAME, entity.group.name)
                .where(RPKIT_PROFILE_GROUP.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
    }

    override fun get(id: Int): RPKProfileGroup? {
        if (cache?.containsKey(id) == true) {
            return cache.get(id)
        } else {
            val result = database.create
                    .select(
                            RPKIT_PROFILE_GROUP.PROFILE_ID,
                            RPKIT_PROFILE_GROUP.GROUP_NAME
                    )
                    .from(RPKIT_PROFILE_GROUP)
                    .where(RPKIT_PROFILE_GROUP.ID.eq(id))
                    .fetchOne() ?: return null
            val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
            val profileId = result.get(RPKIT_PROFILE_GROUP.PROFILE_ID)
            val profile = profileProvider.getProfile(profileId)
            val groupProvider = plugin.core.serviceManager.getServiceProvider(RPKGroupProvider::class)
            val groupName = result.get(RPKIT_PROFILE_GROUP.GROUP_NAME)
            val group = groupProvider.getGroup(groupName)
            if (profile != null && group != null) {
                val profileGroup = RPKProfileGroup(
                        id,
                        profile,
                        group
                )
                cache?.put(id, profileGroup)
                return profileGroup
            } else {
                database.create
                        .deleteFrom(RPKIT_PROFILE_GROUP)
                        .where(RPKIT_PROFILE_GROUP.ID.eq(id))
                        .execute()
                return null
            }
        }
    }

    fun get(profile: RPKProfile): List<RPKProfileGroup> {
        val results = database.create
                .select(RPKIT_PROFILE_GROUP.ID)
                .from(RPKIT_PROFILE_GROUP)
                .where(RPKIT_PROFILE_GROUP.PROFILE_ID.eq(profile.id))
                .fetch()
        return results.map { result -> get(result.get(RPKIT_PROFILE_GROUP.ID)) }
                .filterNotNull()
    }

    override fun delete(entity: RPKProfileGroup) {
        database.create
                .deleteFrom(RPKIT_PROFILE_GROUP)
                .where(RPKIT_PROFILE_GROUP.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
    }

}