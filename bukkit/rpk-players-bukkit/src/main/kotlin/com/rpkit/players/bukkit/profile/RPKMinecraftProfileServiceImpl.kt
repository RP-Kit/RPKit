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
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileLinkRequest
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.OfflinePlayer
import java.util.UUID


class RPKMinecraftProfileServiceImpl(override val plugin: RPKPlayersBukkit) : RPKMinecraftProfileService {

    override fun getMinecraftProfile(id: Int): RPKMinecraftProfile? {
        return plugin.database.getTable(RPKMinecraftProfileTable::class.java)[id]
    }

    override fun getMinecraftProfile(name: String): RPKMinecraftProfile? {
        val bukkitPlayer = plugin.server.getOfflinePlayer(name)
        return getMinecraftProfile(bukkitPlayer)
    }

    override fun getMinecraftProfile(player: OfflinePlayer): RPKMinecraftProfile? {
        return getMinecraftProfile(player.uniqueId)
    }

    override fun getMinecraftProfile(minecraftUUID: UUID): RPKMinecraftProfile? {
        return plugin.database.getTable(RPKMinecraftProfileTable::class.java).get(minecraftUUID)
    }

    override fun getMinecraftProfiles(profile: RPKProfile): List<RPKMinecraftProfile> {
        return plugin.database.getTable(RPKMinecraftProfileTable::class.java).get(profile)
    }

    private fun addMinecraftProfile(profile: RPKMinecraftProfile) {
        val event = RPKBukkitMinecraftProfileCreateEvent(profile, !plugin.server.isPrimaryThread)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.database.getTable(RPKMinecraftProfileTable::class.java).insert(event.minecraftProfile)
    }

    override fun createMinecraftProfile(minecraftUsername: String, profile: RPKThinProfile?): RPKMinecraftProfile {
        val bukkitPlayer = plugin.server.getOfflinePlayer(minecraftUsername)
        val minecraftProfile = RPKMinecraftProfileImpl(
            profile = profile ?: RPKThinProfileImpl(
                bukkitPlayer.name ?: "Unknown Minecraft user"
            ),
            minecraftUUID = bukkitPlayer.uniqueId
        )
        addMinecraftProfile(minecraftProfile)
        return minecraftProfile
    }

    override fun createMinecraftProfile(minecraftUUID: UUID, profile: RPKThinProfile?): RPKMinecraftProfile {
        val bukkitPlayer = plugin.server.getOfflinePlayer(minecraftUUID)
        val minecraftProfile = RPKMinecraftProfileImpl(
            profile = profile ?: RPKThinProfileImpl(
                bukkitPlayer.name ?: "Unknown Minecraft user"
            ),
            minecraftUUID = minecraftUUID
        )
        addMinecraftProfile(minecraftProfile)
        return minecraftProfile
    }

    override fun updateMinecraftProfile(profile: RPKMinecraftProfile) {
        val event = RPKBukkitMinecraftProfileUpdateEvent(profile, !plugin.server.isPrimaryThread)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.database.getTable(RPKMinecraftProfileTable::class.java).update(event.minecraftProfile)
    }

    override fun removeMinecraftProfile(profile: RPKMinecraftProfile) {
        val event = RPKBukkitMinecraftProfileDeleteEvent(profile, !plugin.server.isPrimaryThread)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.database.getTable(RPKMinecraftProfileTable::class.java).delete(event.minecraftProfile)
    }

    override fun getMinecraftProfileLinkRequests(minecraftProfile: RPKMinecraftProfile): List<RPKMinecraftProfileLinkRequest> {
        return plugin.database.getTable(RPKMinecraftProfileLinkRequestTable::class.java).get(minecraftProfile)
    }

    private fun addMinecraftProfileLinkRequest(minecraftProfileLinkRequest: RPKMinecraftProfileLinkRequest) {
        plugin.database.getTable(RPKMinecraftProfileLinkRequestTable::class.java).insert(minecraftProfileLinkRequest)
    }

    override fun createMinecraftProfileLinkRequest(
        profile: RPKProfile,
        minecraftProfile: RPKMinecraftProfile
    ): RPKMinecraftProfileLinkRequest {
        val linkRequest = RPKMinecraftProfileLinkRequestImpl(profile, minecraftProfile)
        addMinecraftProfileLinkRequest(linkRequest)
        return linkRequest
    }

    override fun removeMinecraftProfileLinkRequest(minecraftProfileLinkRequest: RPKMinecraftProfileLinkRequest) {
        plugin.database.getTable(RPKMinecraftProfileLinkRequestTable::class.java).delete(minecraftProfileLinkRequest)
    }

}