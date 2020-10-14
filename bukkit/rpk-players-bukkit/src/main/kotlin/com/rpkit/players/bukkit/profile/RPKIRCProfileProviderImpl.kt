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

package com.rpkit.players.bukkit.profile

import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.database.table.RPKIRCProfileTable
import com.rpkit.players.bukkit.event.ircprofile.RPKBukkitIRCProfileCreateEvent
import com.rpkit.players.bukkit.event.ircprofile.RPKBukkitIRCProfileDeleteEvent
import com.rpkit.players.bukkit.event.ircprofile.RPKBukkitIRCProfileUpdateEvent
import org.pircbotx.User


class RPKIRCProfileServiceImpl(override val plugin: RPKPlayersBukkit) : RPKIRCProfileService {

    override fun getIRCProfile(id: Int): RPKIRCProfile? {
        return plugin.database.getTable(RPKIRCProfileTable::class)[id]
    }

    override fun getIRCProfile(user: User): RPKIRCProfile? {
        return plugin.database.getTable(RPKIRCProfileTable::class).get(user)
    }

    override fun getIRCProfiles(profile: RPKProfile): List<RPKIRCProfile> {
        return plugin.database.getTable(RPKIRCProfileTable::class).get(profile)
    }

    override fun addIRCProfile(profile: RPKIRCProfile) {
        val event = RPKBukkitIRCProfileCreateEvent(profile)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.database.getTable(RPKIRCProfileTable::class).insert(event.ircProfile)
    }

    override fun updateIRCProfile(profile: RPKIRCProfile) {
        val event = RPKBukkitIRCProfileUpdateEvent(profile)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.database.getTable(RPKIRCProfileTable::class).update(event.ircProfile)
    }

    override fun removeIRCProfile(profile: RPKIRCProfile) {
        val event = RPKBukkitIRCProfileDeleteEvent(profile)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.database.getTable(RPKIRCProfileTable::class).delete(event.ircProfile)
    }

}