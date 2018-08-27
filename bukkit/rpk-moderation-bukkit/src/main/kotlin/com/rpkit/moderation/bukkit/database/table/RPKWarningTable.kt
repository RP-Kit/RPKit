/*
 * Copyright 2018 Ross Binden
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rpkit.moderation.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.moderation.bukkit.RPKModerationBukkit
import com.rpkit.moderation.bukkit.database.jooq.rpkit.Tables.RPKIT_WARNING
import com.rpkit.moderation.bukkit.warning.RPKWarning
import com.rpkit.moderation.bukkit.warning.RPKWarningImpl
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.SQLDialect
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType
import org.jooq.util.sqlite.SQLiteDataType
import java.sql.Timestamp


class RPKWarningTable(database: Database, private val plugin: RPKModerationBukkit): Table<RPKWarning>(database, RPKWarning::class) {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_warning.id.enabled")) {
        database.cacheManager.createCache("rpk-moderation-bukkit.rpkit_warning.id", CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Int::class.javaObjectType, RPKWarning::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_warning.id.size"))))
    } else {
        null
    }

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_WARNING)
                .column(RPKIT_WARNING.ID, if (database.dialect == SQLDialect.SQLITE) SQLiteDataType.INTEGER.identity(true) else SQLDataType.INTEGER.identity(true))
                .column(RPKIT_WARNING.REASON, SQLDataType.VARCHAR.length(1024))
                .column(RPKIT_WARNING.PROFILE_ID, SQLDataType.INTEGER)
                .column(RPKIT_WARNING.ISSUER_ID, SQLDataType.INTEGER)
                .column(RPKIT_WARNING.TIME, SQLDataType.TIMESTAMP)
                .constraints(
                        constraint("pk_rpkit_warning").primaryKey(RPKIT_WARNING.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.5.2")
        }
        if (database.getTableVersion(this) == "1.5.0") {
            database.create
                    .alterTable(RPKIT_WARNING)
                    .alterColumn(RPKIT_WARNING.ID)
                        .set(if (database.dialect == SQLDialect.SQLITE) SQLiteDataType.INTEGER.identity(true) else SQLDataType.INTEGER.identity(true))
                    .execute()
            database.setTableVersion(this, "1.5.2")
        }
    }

    override fun insert(entity: RPKWarning): Int {
        database.create
                .insertInto(
                        RPKIT_WARNING,
                        RPKIT_WARNING.REASON,
                        RPKIT_WARNING.PROFILE_ID,
                        RPKIT_WARNING.ISSUER_ID,
                        RPKIT_WARNING.TIME
                )
                .values(
                        entity.reason,
                        entity.profile.id,
                        entity.issuer.id,
                        Timestamp.valueOf(entity.time)
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
        return id
    }

    override fun update(entity: RPKWarning) {
        database.create
                .update(RPKIT_WARNING)
                .set(RPKIT_WARNING.REASON, entity.reason)
                .set(RPKIT_WARNING.PROFILE_ID, entity.profile.id)
                .set(RPKIT_WARNING.ISSUER_ID, entity.issuer.id)
                .set(RPKIT_WARNING.TIME, Timestamp.valueOf(entity.time))
                .where(RPKIT_WARNING.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
    }

    override fun get(id: Int): RPKWarning? {
        val result = database.create
                .select(
                        RPKIT_WARNING.REASON,
                        RPKIT_WARNING.PROFILE_ID,
                        RPKIT_WARNING.ISSUER_ID,
                        RPKIT_WARNING.TIME
                )
                .from(RPKIT_WARNING)
                .where(RPKIT_WARNING.ID.eq(id))
                .fetchOne() ?: return null
        val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
        val profile = profileProvider.getProfile(result[RPKIT_WARNING.PROFILE_ID])
        val issuer = profileProvider.getProfile(result[RPKIT_WARNING.ISSUER_ID])
        if (profile != null && issuer != null) {
            val warning = RPKWarningImpl(
                    id,
                    result[RPKIT_WARNING.REASON],
                    profile,
                    issuer,
                    result[RPKIT_WARNING.TIME].toLocalDateTime()
            )
            cache?.put(id, warning)
            return warning
        } else {
            database.create
                    .deleteFrom(RPKIT_WARNING)
                    .where(RPKIT_WARNING.ID.eq(id))
                    .execute()
            cache?.remove(id)
            return null
        }
    }

    fun get(profile: RPKProfile): List<RPKWarning> {
        val results = database.create
                .select(RPKIT_WARNING.ID)
                .from(RPKIT_WARNING)
                .where(RPKIT_WARNING.PROFILE_ID.eq(profile.id))
                .fetch()
        return results.map { get(it[RPKIT_WARNING.ID]) }.filterNotNull()
    }

    override fun delete(entity: RPKWarning) {
        database.create
                .deleteFrom(RPKIT_WARNING)
                .where(RPKIT_WARNING.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
    }
}