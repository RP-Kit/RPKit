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
import com.rpkit.core.service.Services
import com.rpkit.moderation.bukkit.RPKModerationBukkit
import com.rpkit.moderation.bukkit.database.jooq.Tables.RPKIT_TICKET
import com.rpkit.moderation.bukkit.ticket.RPKTicket
import com.rpkit.moderation.bukkit.ticket.RPKTicketImpl
import com.rpkit.players.bukkit.profile.RPKProfileService
import org.bukkit.Location
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder


class RPKTicketTable(private val database: Database, private val plugin: RPKModerationBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_ticket.id.enabled")) {
        database.cacheManager.createCache("rpk-moderation-bukkit.rpkit_ticket.id", CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Int::class.javaObjectType, RPKTicket::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_ticket.id.size"))))
    } else {
        null
    }

    fun insert(entity: RPKTicket) {
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
                        entity.openDate,
                        entity.closeDate,
                        entity.isClosed
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
    }

    fun update(entity: RPKTicket) {
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
                .set(RPKIT_TICKET.OPEN_DATE, entity.openDate)
                .set(RPKIT_TICKET.CLOSE_DATE, entity.closeDate)
                .set(RPKIT_TICKET.CLOSED, entity.isClosed)
                .where(RPKIT_TICKET.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
    }

    operator fun get(id: Int): RPKTicket? {
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
            val profileService = Services[RPKProfileService::class]
            val issuerId = result[RPKIT_TICKET.ISSUER_ID]
            val issuer = if (issuerId == null) null else profileService?.getProfile(issuerId)
            val resolverId = result[RPKIT_TICKET.RESOLVER_ID]
            val resolver = if (resolverId == null) null else profileService?.getProfile(resolverId)
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
                        result[RPKIT_TICKET.OPEN_DATE],
                        result[RPKIT_TICKET.CLOSE_DATE],
                        result[RPKIT_TICKET.CLOSED]
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

    fun delete(entity: RPKTicket) {
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
                .where(RPKIT_TICKET.CLOSED.eq(false))
                .fetch()
        return results.map { get(it[RPKIT_TICKET.ID]) }.filterNotNull()
    }

    fun getClosedTickets(): List<RPKTicket> {
        val results = database.create
                .select(RPKIT_TICKET.ID)
                .from(RPKIT_TICKET)
                .where(RPKIT_TICKET.CLOSED.eq(true))
                .fetch()
        return results.map { get(it[RPKIT_TICKET.ID]) }.filterNotNull()
    }

}