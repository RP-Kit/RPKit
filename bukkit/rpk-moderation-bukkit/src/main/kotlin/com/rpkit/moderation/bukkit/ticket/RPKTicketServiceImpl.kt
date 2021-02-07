/*
 * Copyright 2021 Ren Binden
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

package com.rpkit.moderation.bukkit.ticket

import com.rpkit.moderation.bukkit.RPKModerationBukkit
import com.rpkit.moderation.bukkit.database.table.RPKTicketTable
import com.rpkit.moderation.bukkit.event.ticket.RPKBukkitTicketCreateEvent
import com.rpkit.moderation.bukkit.event.ticket.RPKBukkitTicketDeleteEvent
import com.rpkit.moderation.bukkit.event.ticket.RPKBukkitTicketUpdateEvent
import com.rpkit.players.bukkit.profile.RPKProfile
import org.bukkit.Location
import java.time.LocalDateTime


class RPKTicketServiceImpl(override val plugin: RPKModerationBukkit) : RPKTicketService {

    override fun getTicket(id: RPKTicketId): RPKTicket? {
        return plugin.database.getTable(RPKTicketTable::class.java)[id]
    }

    override fun getOpenTickets(): List<RPKTicket> {
        return plugin.database.getTable(RPKTicketTable::class.java).getOpenTickets()
    }

    override fun getClosedTickets(): List<RPKTicket> {
        return plugin.database.getTable(RPKTicketTable::class.java).getClosedTickets()
    }

    override fun addTicket(ticket: RPKTicket) {
        val event = RPKBukkitTicketCreateEvent(ticket)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.database.getTable(RPKTicketTable::class.java).insert(event.ticket)
    }

    override fun createTicket(
        reason: String,
        issuer: RPKProfile,
        resolver: RPKProfile?,
        location: Location?,
        openDate: LocalDateTime,
        closeDate: LocalDateTime?,
        isClosed: Boolean
    ): RPKTicket {
        val ticket = RPKTicketImpl(
            null,
            reason,
            issuer,
            resolver,
            location,
            openDate,
            closeDate,
            isClosed
        )
        addTicket(ticket)
        return ticket
    }

    override fun updateTicket(ticket: RPKTicket) {
        val event = RPKBukkitTicketUpdateEvent(ticket)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.database.getTable(RPKTicketTable::class.java).update(event.ticket)
    }

    override fun removeTicket(ticket: RPKTicket) {
        val event = RPKBukkitTicketDeleteEvent(ticket)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.database.getTable(RPKTicketTable::class.java).delete(event.ticket)
    }

}