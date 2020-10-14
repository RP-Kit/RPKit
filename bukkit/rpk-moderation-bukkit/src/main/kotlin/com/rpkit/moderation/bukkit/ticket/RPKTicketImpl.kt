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

package com.rpkit.moderation.bukkit.ticket

import com.rpkit.moderation.bukkit.event.ticket.RPKBukkitTicketCloseEvent
import com.rpkit.players.bukkit.profile.RPKProfile
import org.bukkit.Bukkit
import org.bukkit.Location
import java.time.LocalDateTime


class RPKTicketImpl(
        override var id: Int? = null,
        override val reason: String,
        override val issuer: RPKProfile,
        override var resolver: RPKProfile?,
        override val location: Location?,
        override val openDate: LocalDateTime,
        override var closeDate: LocalDateTime?,
        override var isClosed: Boolean
) : RPKTicket {

    constructor(reason: String, issuer: RPKProfile, location: Location) : this(
            0,
            reason,
            issuer,
            null,
            location,
            LocalDateTime.now(),
            null,
            false
    )

    override fun close(resolver: RPKProfile) {
        val event = RPKBukkitTicketCloseEvent(resolver, this)
        Bukkit.getServer().pluginManager.callEvent(event)
        if (event.isCancelled) return
        isClosed = true
        this.resolver = event.profile
        closeDate = LocalDateTime.now()
    }
}