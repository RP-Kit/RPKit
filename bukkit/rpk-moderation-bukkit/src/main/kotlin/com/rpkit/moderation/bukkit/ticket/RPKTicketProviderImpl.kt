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

package com.rpkit.moderation.bukkit.ticket

import com.rpkit.moderation.bukkit.RPKModerationBukkit
import com.rpkit.moderation.bukkit.database.table.RPKTicketTable
import com.rpkit.moderation.bukkit.event.ticket.RPKBukkitTicketCreateEvent
import com.rpkit.moderation.bukkit.event.ticket.RPKBukkitTicketDeleteEvent
import com.rpkit.moderation.bukkit.event.ticket.RPKBukkitTicketUpdateEvent


class RPKTicketProviderImpl(private val plugin: RPKModerationBukkit): RPKTicketProvider {

    override fun getTicket(id: Int): RPKTicket? {
        return plugin.core.database.getTable(RPKTicketTable::class)[id]
    }

    override fun getOpenTickets(): List<RPKTicket> {
        return plugin.core.database.getTable(RPKTicketTable::class).getOpenTickets()
    }

    override fun getClosedTickets(): List<RPKTicket> {
        return plugin.core.database.getTable(RPKTicketTable::class).getClosedTickets()
    }

    override fun addTicket(ticket: RPKTicket) {
        val event = RPKBukkitTicketCreateEvent(ticket)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.core.database.getTable(RPKTicketTable::class).insert(event.ticket)
    }

    override fun updateTicket(ticket: RPKTicket) {
        val event = RPKBukkitTicketUpdateEvent(ticket)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.core.database.getTable(RPKTicketTable::class).update(event.ticket)
    }

    override fun removeTicket(ticket: RPKTicket) {
        val event = RPKBukkitTicketDeleteEvent(ticket)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.core.database.getTable(RPKTicketTable::class).delete(event.ticket)
    }

}