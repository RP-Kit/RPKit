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
import com.rpkit.players.bukkit.database.table.RPKMinecraftProfileLinkRequestTable
import com.rpkit.players.bukkit.database.table.RPKMinecraftProfileTable
import com.rpkit.players.bukkit.event.minecraftprofile.RPKBukkitMinecraftProfileCreateEvent
import com.rpkit.players.bukkit.event.minecraftprofile.RPKBukkitMinecraftProfileDeleteEvent
import com.rpkit.players.bukkit.event.minecraftprofile.RPKBukkitMinecraftProfileUpdateEvent
import org.bukkit.OfflinePlayer


class RPKMinecraftProfileServiceImpl(override val plugin: RPKPlayersBukkit) : RPKMinecraftProfileService {

    override fun getMinecraftProfile(id: Int): RPKMinecraftProfile? {
        return plugin.database.getTable(RPKMinecraftProfileTable::class)[id]
    }

    override fun getMinecraftProfile(player: OfflinePlayer): RPKMinecraftProfile? {
        return plugin.database.getTable(RPKMinecraftProfileTable::class).get(player)
    }

    override fun getMinecraftProfiles(profile: RPKProfile): List<RPKMinecraftProfile> {
        return plugin.database.getTable(RPKMinecraftProfileTable::class).get(profile)
    }

    override fun addMinecraftProfile(profile: RPKMinecraftProfile) {
        val event = RPKBukkitMinecraftProfileCreateEvent(profile)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.database.getTable(RPKMinecraftProfileTable::class).insert(event.minecraftProfile)
    }

    override fun updateMinecraftProfile(profile: RPKMinecraftProfile) {
        val event = RPKBukkitMinecraftProfileUpdateEvent(profile)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.database.getTable(RPKMinecraftProfileTable::class).update(event.minecraftProfile)
    }

    override fun removeMinecraftProfile(profile: RPKMinecraftProfile) {
        val event = RPKBukkitMinecraftProfileDeleteEvent(profile)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.database.getTable(RPKMinecraftProfileTable::class).delete(event.minecraftProfile)
    }

    override fun getMinecraftProfileLinkRequests(minecraftProfile: RPKMinecraftProfile): List<RPKMinecraftProfileLinkRequest> {
        return plugin.database.getTable(RPKMinecraftProfileLinkRequestTable::class).get(minecraftProfile)
    }

    override fun addMinecraftProfileLinkRequest(minecraftProfileLinkRequest: RPKMinecraftProfileLinkRequest) {
        plugin.database.getTable(RPKMinecraftProfileLinkRequestTable::class).insert(minecraftProfileLinkRequest)
    }

    override fun removeMinecraftProfileLinkRequest(minecraftProfileLinkRequest: RPKMinecraftProfileLinkRequest) {
        plugin.database.getTable(RPKMinecraftProfileLinkRequestTable::class).delete(minecraftProfileLinkRequest)
    }

}