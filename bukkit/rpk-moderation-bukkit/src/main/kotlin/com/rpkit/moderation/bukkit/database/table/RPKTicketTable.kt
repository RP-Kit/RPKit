/*
 * Copyright 2020 Ren Binden
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
import com.rpkit.moderation.bukkit.database.jooq.rpkit.Tables.RPKIT_TICKET
import com.rpkit.moderation.bukkit.ticket.RPKTicket
import com.rpkit.moderation.bukkit.ticket.RPKTicketImpl
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import org.bukkit.Location
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType
import java.sql.Timestamp


class RPKTicketTable(database: Database, private val plugin: RPKModerationBukkit): Table<RPKTicket>(database, RPKTicket::class) {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_ticket.id.enabled")) {
        database.cacheManager.createCache("rpk-moderation-bukkit.rpkit_ticket.id", CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Int::class.javaObjectType, RPKTicket::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_ticket.id.size"))))
    } else {
        null
    }

    override fun create() {
        database.create.createTableIfNotExists(RPKIT_TICKET)
                .column(RPKIT_TICKET.ID, SQLDataType.INTEGER.identity(true))
                .column(RPKIT_TICKET.REASON, SQLDataType.VARCHAR.length(1024))
                .column(RPKIT_TICKET.ISSUER_ID, SQLDataType.INTEGER)
                .column(RPKIT_TICKET.RESOLVER_ID, SQLDataType.INTEGER)
                .column(RPKIT_TICKET.WORLD, SQLDataType.VARCHAR.length(256))
                .column(RPKIT_TICKET.X, SQLDataType.DOUBLE)
                .column(RPKIT_TICKET.Y, SQLDataType.DOUBLE)
                .column(RPKIT_TICKET.Z, SQLDataType.DOUBLE)
                .column(RPKIT_TICKET.YAW, SQLDataType.DOUBLE)
                .column(RPKIT_TICKET.PITCH, SQLDataType.DOUBLE)
                .column(RPKIT_TICKET.OPEN_DATE, SQLDataType.TIMESTAMP)
                .column(RPKIT_TICKET.CLOSE_DATE, SQLDataType.TIMESTAMP)
                .column(RPKIT_TICKET.CLOSED, SQLDataType.TINYINT.length(1))
                .constraints(
                        constraint("pk_rpkit_ticket").primaryKey(RPKIT_TICKET.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.5.2")
        }
        if (database.getTableVersion(this) == "1.5.0") {
            database.create
                    .alterTable(RPKIT_TICKET)
                    .alterColumn(RPKIT_TICKET.ID)
                        .set(SQLDataType.INTEGER.identity(true))
                    .execute()
            database.setTableVersion(this, "1.5.2")
        }
    }

    override fun insert(entity: RPKTicket): Int {
        database.create
                .insertInto(
                        RPKIT_TICKET,
                        RPKIT_TICKET.REASON,
                        RPKIT_TICKET.ISSUER_ID,
                        RPKIT_TICKET.RESOLVER_ID,
                        RPKIT_TICKET.WORLD,
                        RPKIT_TICKET.X,
                        RPKIT_TICKET.Y,
                        RPKIT_TICKET.Z,
                        RPKIT_TICKET.YAW,
                        RPKIT_TICKET.PITCH,
                        RPKIT_TICKET.OPEN_DATE,
                        RPKIT_TICKET.CLOSE_DATE,
                        RPKIT_TICKET.CLOSED
                )
                .values(
                        entity.reason,
                        entity.issuer.id,
                        entity.resolver?.id,
                        entity.location?.world?.name,
                        entity.location?.x,
                        entity.location?.y,
                        entity.location?.z,
                        entity.location?.yaw?.toDouble(),
                        entity.location?.pitch?.toDouble(),
                        Timestamp.valueOf(entity.openDate),
                        if (entity.closeDate == null) null else Timestamp.valueOf(entity.closeDate),
                        if (entity.isClosed) 1.toByte() else 0.toByte()
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
        return id
    }

    override fun update(entity: RPKTicket) {
        database.create
                .update(RPKIT_TICKET)
                .set(RPKIT_TICKET.REASON, entity.reason)
                .set(RPKIT_TICKET.ISSUER_ID, entity.issuer.id)
                .set(RPKIT_TICKET.RESOLVER_ID, entity.resolver?.id)
                .set(RPKIT_TICKET.WORLD, entity.location?.world?.name)
                .set(RPKIT_TICKET.X, entity.location?.x)
                .set(RPKIT_TICKET.Y, entity.location?.y)
                .set(RPKIT_TICKET.Z, entity.location?.z)
                .set(RPKIT_TICKET.YAW, entity.location?.yaw?.toDouble())
                .set(RPKIT_TICKET.PITCH, entity.location?.pitch?.toDouble())
                .set(RPKIT_TICKET.OPEN_DATE, Timestamp.valueOf(entity.openDate))
                .set(RPKIT_TICKET.CLOSE_DATE, if (entity.closeDate == null) null else Timestamp.valueOf(entity.closeDate))
                .set(RPKIT_TICKET.CLOSED, if (entity.isClosed) 1.toByte() else 0.toByte())
                .where(RPKIT_TICKET.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
    }

    override fun get(id: Int): RPKTicket? {
        if (cache?.containsKey(id) == true) {
            return cache[id]
        } else {
            val result = database.create
                    .select(
                            RPKIT_TICKET.REASON,
                            RPKIT_TICKET.ISSUER_ID,
                            RPKIT_TICKET.RESOLVER_ID,
                            RPKIT_TICKET.WORLD,
                            RPKIT_TICKET.X,
                            RPKIT_TICKET.Y,
                            RPKIT_TICKET.Z,
                            RPKIT_TICKET.YAW,
                            RPKIT_TICKET.PITCH,
                            RPKIT_TICKET.OPEN_DATE,
                            RPKIT_TICKET.CLOSE_DATE,
                            RPKIT_TICKET.CLOSED
                    )
                    .from(RPKIT_TICKET)
                    .where(RPKIT_TICKET.ID.eq(id))
                    .fetchOne() ?: return null
            val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
            val issuerId = result[RPKIT_TICKET.ISSUER_ID]
            val issuer = if (issuerId == null) null else profileProvider.getProfile(issuerId)
            val resolverId = result[RPKIT_TICKET.RESOLVER_ID]
            val resolver = if (resolverId == null) null else profileProvider.getProfile(resolverId)
            val worldName = result[RPKIT_TICKET.WORLD]
            val world = if (worldName == null) null else plugin.server.getWorld(worldName)
            val x = result[RPKIT_TICKET.X]
            val y = result[RPKIT_TICKET.Y]
            val z = result[RPKIT_TICKET.Z]
            val yaw = result[RPKIT_TICKET.YAW]
            val pitch = result[RPKIT_TICKET.PITCH]
            if (issuer != null) {
                val ticket = RPKTicketImpl(
                        id,
                        result[RPKIT_TICKET.REASON],
                        issuer,
                        resolver,
                        if (world == null || x == null || y == null || z == null || yaw == null || pitch == null) {
                            null
                        } else {
                            Location(
                                    world,
                                    x,
                                    y,
                                    z,
                                    yaw.toFloat(),
                                    pitch.toFloat()
                            )
                        },
                        result[RPKIT_TICKET.OPEN_DATE].toLocalDateTime(),
                        result[RPKIT_TICKET.CLOSE_DATE]?.toLocalDateTime(),
                        result[RPKIT_TICKET.CLOSED] == 1.toByte()
                )
                cache?.put(id, ticket)
                return ticket
            } else {
                database.create
                        .deleteFrom(RPKIT_TICKET)
                        .where(RPKIT_TICKET.ID.eq(id))
                        .execute()
                cache?.remove(id)
                return null
            }
        }
    }

    override fun delete(entity: RPKTicket) {
        database.create
                .deleteFrom(RPKIT_TICKET)
                .where(RPKIT_TICKET.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
    }

    fun getOpenTickets(): List<RPKTicket> {
        val results = database.create
                .select(RPKIT_TICKET.ID)
                .from(RPKIT_TICKET)
                .where(RPKIT_TICKET.CLOSED.eq(0.toByte()))
                .fetch()
        return results.map { get(it[RPKIT_TICKET.ID]) }.filterNotNull()
    }

    fun getClosedTickets(): List<RPKTicket> {
        val results = database.create
                .select(RPKIT_TICKET.ID)
                .from(RPKIT_TICKET)
                .where(RPKIT_TICKET.CLOSED.eq(1.toByte()))
                .fetch()
        return results.map { get(it[RPKIT_TICKET.ID]) }.filterNotNull()
    }

}