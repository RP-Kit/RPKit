package com.rpkit.players.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.database.jooq.rpkit.Tables.RPKIT_PROFILE
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileImpl
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.SQLDialect
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType
import org.jooq.util.sqlite.SQLiteDataType


class RPKProfileTable(database: Database, private val plugin: RPKPlayersBukkit): Table<RPKProfile>(database, RPKProfile::class) {

    private val cacheManger = CacheManagerBuilder.newCacheManagerBuilder().build(true)
    private val cache = cacheManger.createCache("cache",
            CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKProfile::class.java,
                    ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())))

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_PROFILE)
                .column(RPKIT_PROFILE.ID, if (database.dialect == SQLDialect.SQLITE) SQLiteDataType.INTEGER.identity(true) else SQLDataType.INTEGER.identity(true))
                .column(RPKIT_PROFILE.NAME, SQLDataType.VARCHAR(16))
                .column(RPKIT_PROFILE.PASSWORD_HASH, SQLDataType.BLOB)
                .column(RPKIT_PROFILE.PASSWORD_SALT, SQLDataType.BLOB)
                .constraints(
                        constraint("pk_rpkit_profile").primaryKey(RPKIT_PROFILE.ID),
                        constraint("uk_rpkit_profile_name").unique(RPKIT_PROFILE.NAME)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.3.0")
        }
    }

    override fun insert(entity: RPKProfile): Int {
        database.create
                .insertInto(
                        RPKIT_PROFILE,
                        RPKIT_PROFILE.NAME,
                        RPKIT_PROFILE.PASSWORD_HASH,
                        RPKIT_PROFILE.PASSWORD_SALT
                )
                .values(
                        entity.name,
                        entity.passwordHash,
                        entity.passwordSalt
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache.put(id, entity)
        return id
    }

    override fun update(entity: RPKProfile) {
        database.create
                .update(RPKIT_PROFILE)
                .set(RPKIT_PROFILE.NAME, entity.name)
                .set(RPKIT_PROFILE.PASSWORD_HASH, entity.passwordHash)
                .set(RPKIT_PROFILE.PASSWORD_SALT, entity.passwordSalt)
                .where(RPKIT_PROFILE.ID.eq(entity.id))
                .execute()
        cache.put(entity.id, entity)
    }

    override fun get(id: Int): RPKProfile? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            val result = database.create
                    .select(
                            RPKIT_PROFILE.NAME,
                            RPKIT_PROFILE.PASSWORD_HASH,
                            RPKIT_PROFILE.PASSWORD_SALT
                    )
                    .from(RPKIT_PROFILE)
                    .where(RPKIT_PROFILE.ID.eq(id))
                    .fetchOne() ?: return null
            val profile = RPKProfileImpl(
                    id,
                    result.get(RPKIT_PROFILE.NAME),
                    result.get(RPKIT_PROFILE.PASSWORD_HASH),
                    result.get(RPKIT_PROFILE.PASSWORD_SALT)
            )
            cache.put(id, profile)
            return profile
        }
    }

    fun get(name: String): RPKProfile? {
        val result = database.create
                .select(RPKIT_PROFILE.ID)
                .from(RPKIT_PROFILE)
                .where(RPKIT_PROFILE.NAME.eq(name))
                .fetchOne() ?: return null
        return get(result.get(RPKIT_PROFILE.ID))
    }

    override fun delete(entity: RPKProfile) {
        database.create
                .deleteFrom(RPKIT_PROFILE)
                .where(RPKIT_PROFILE.ID.eq(entity.id))
                .execute()
        cache.remove(entity.id)
    }

}