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

package com.rpkit.players.bukkit.profile.minecraft

import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.database.table.RPKMinecraftProfileLinkRequestTable
import com.rpkit.players.bukkit.database.table.RPKMinecraftProfileTable
import com.rpkit.players.bukkit.event.minecraftprofile.RPKBukkitMinecraftProfileCreateEvent
import com.rpkit.players.bukkit.event.minecraftprofile.RPKBukkitMinecraftProfileDeleteEvent
import com.rpkit.players.bukkit.event.minecraftprofile.RPKBukkitMinecraftProfileUpdateEvent
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileName
import com.rpkit.players.bukkit.profile.RPKThinProfile
import com.rpkit.players.bukkit.profile.RPKThinProfileImpl
import org.bukkit.OfflinePlayer
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap


class RPKMinecraftProfileServiceImpl(override val plugin: RPKPlayersBukkit) : RPKMinecraftProfileService {

    private val minecraftProfilesById = ConcurrentHashMap<Int, RPKMinecraftProfile>()
    private val minecraftProfilesByMinecraftUsername = ConcurrentHashMap<String, RPKMinecraftProfile>()
    private val minecraftProfilesByUuid = ConcurrentHashMap<UUID, RPKMinecraftProfile>()

    override fun getPreloadedMinecraftProfile(id: RPKMinecraftProfileId): RPKMinecraftProfile? {
        return minecraftProfilesById[id.value]
    }

    override fun getPreloadedMinecraftProfile(name: RPKMinecraftUsername): RPKMinecraftProfile? {
        return minecraftProfilesByMinecraftUsername[name.value]
    }

    override fun getPreloadedMinecraftProfile(minecraftUUID: UUID): RPKMinecraftProfile? {
        return minecraftProfilesByUuid[minecraftUUID]
    }

    override fun getPreloadedMinecraftProfile(player: OfflinePlayer): RPKMinecraftProfile? {
        return getPreloadedMinecraftProfile(player.uniqueId)
    }

    override fun loadMinecraftProfile(minecraftUUID: UUID): CompletableFuture<RPKMinecraftProfile?> {
        val preloadedMinecraftProfile = getPreloadedMinecraftProfile(minecraftUUID)
        if (preloadedMinecraftProfile != null) return CompletableFuture.completedFuture(preloadedMinecraftProfile)
        plugin.logger.info("Loading Minecraft profile for user $minecraftUUID...")
        val minecraftProfileFuture = plugin.database.getTable(RPKMinecraftProfileTable::class.java)[minecraftUUID]
        minecraftProfileFuture.thenAccept { minecraftProfile ->
            if (minecraftProfile != null) {
                val minecraftProfileId = minecraftProfile.id
                if (minecraftProfileId != null) {
                    minecraftProfilesById[minecraftProfileId.value] = minecraftProfile
                }
                minecraftProfilesByMinecraftUsername[minecraftProfile.minecraftUsername.value] = minecraftProfile
                minecraftProfilesByUuid[minecraftUUID] = minecraftProfile
            }
        }
        return minecraftProfileFuture
    }

    override fun unloadMinecraftProfile(minecraftProfile: RPKMinecraftProfile) {
        val minecraftProfileId = minecraftProfile.id
        if (minecraftProfileId != null) {
            minecraftProfilesById.remove(minecraftProfileId.value)
        }
        minecraftProfilesByMinecraftUsername.remove(minecraftProfile.minecraftUsername.value)
        minecraftProfilesByUuid.remove(minecraftProfile.minecraftUUID)
    }

    override fun getMinecraftProfile(id: RPKMinecraftProfileId): CompletableFuture<RPKMinecraftProfile?> {
        return plugin.database.getTable(RPKMinecraftProfileTable::class.java)[id]
    }

    override fun getMinecraftProfile(name: RPKMinecraftUsername): CompletableFuture<RPKMinecraftProfile?> {
        val bukkitPlayer = plugin.server.getOfflinePlayer(name.value)
        return getMinecraftProfile(bukkitPlayer)
    }

    override fun getMinecraftProfile(player: OfflinePlayer): CompletableFuture<RPKMinecraftProfile?> {
        return getMinecraftProfile(player.uniqueId)
    }

    override fun getMinecraftProfile(minecraftUUID: UUID): CompletableFuture<RPKMinecraftProfile?> {
        return plugin.database.getTable(RPKMinecraftProfileTable::class.java).get(minecraftUUID)
    }

    override fun getMinecraftProfiles(profile: RPKProfile): CompletableFuture<List<RPKMinecraftProfile>> {
        return plugin.database.getTable(RPKMinecraftProfileTable::class.java).get(profile)
    }

    private fun addMinecraftProfile(profile: RPKMinecraftProfile): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val event = RPKBukkitMinecraftProfileCreateEvent(profile, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            plugin.database.getTable(RPKMinecraftProfileTable::class.java).insert(event.minecraftProfile).join()
        }
    }

    override fun createMinecraftProfile(minecraftUsername: RPKMinecraftUsername, profile: RPKThinProfile?): CompletableFuture<RPKMinecraftProfile> {
        val bukkitPlayer = plugin.server.getOfflinePlayer(minecraftUsername.value)
        return CompletableFuture.supplyAsync {
            val minecraftProfile = RPKMinecraftProfileImpl(
                profile = profile ?: RPKThinProfileImpl(
                    RPKProfileName(bukkitPlayer.name ?: "Unknown Minecraft user")
                ),
                minecraftUUID = bukkitPlayer.uniqueId
            )
            addMinecraftProfile(minecraftProfile).join()
            return@supplyAsync minecraftProfile
        }
    }

    override fun createMinecraftProfile(minecraftUUID: UUID, profile: RPKThinProfile?): CompletableFuture<RPKMinecraftProfile> {
        val bukkitPlayer = plugin.server.getOfflinePlayer(minecraftUUID)
        return CompletableFuture.supplyAsync {
            val minecraftProfile = RPKMinecraftProfileImpl(
                profile = profile ?: RPKThinProfileImpl(
                    RPKProfileName(bukkitPlayer.name ?: "Unknown Minecraft user")
                ),
                minecraftUUID = minecraftUUID
            )
            addMinecraftProfile(minecraftProfile).join()
            return@supplyAsync minecraftProfile
        }
    }

    override fun createAndLoadMinecraftProfile(
        minecraftUsername: RPKMinecraftUsername,
        profile: RPKThinProfile?
    ): CompletableFuture<RPKMinecraftProfile> {
        return CompletableFuture.supplyAsync {
            val minecraftProfile = createMinecraftProfile(minecraftUsername, profile).join()
            val minecraftProfileId = minecraftProfile.id
            if (minecraftProfileId != null) {
                minecraftProfilesById[minecraftProfileId.value] = minecraftProfile
            }
            minecraftProfilesByMinecraftUsername[minecraftProfile.minecraftUsername.value] = minecraftProfile
            minecraftProfilesByUuid[minecraftProfile.minecraftUUID] = minecraftProfile
            return@supplyAsync minecraftProfile
        }
    }

    override fun createAndLoadMinecraftProfile(
        minecraftUUID: UUID,
        profile: RPKThinProfile?
    ): CompletableFuture<RPKMinecraftProfile> {
        return CompletableFuture.supplyAsync {
            val minecraftProfile = createMinecraftProfile(minecraftUUID, profile).join()
            val minecraftProfileId = minecraftProfile.id
            if (minecraftProfileId != null) {
                minecraftProfilesById[minecraftProfileId.value] = minecraftProfile
            }
            minecraftProfilesByMinecraftUsername[minecraftProfile.minecraftUsername.value] = minecraftProfile
            minecraftProfilesByUuid[minecraftUUID] = minecraftProfile
            return@supplyAsync minecraftProfile
        }
    }

    override fun updateMinecraftProfile(profile: RPKMinecraftProfile): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val event = RPKBukkitMinecraftProfileUpdateEvent(profile, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            plugin.database.getTable(RPKMinecraftProfileTable::class.java).update(event.minecraftProfile).join()
        }
    }

    override fun removeMinecraftProfile(profile: RPKMinecraftProfile): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val event = RPKBukkitMinecraftProfileDeleteEvent(profile, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            plugin.database.getTable(RPKMinecraftProfileTable::class.java).delete(event.minecraftProfile)
        }
    }

    override fun getMinecraftProfileLinkRequests(minecraftProfile: RPKMinecraftProfile): CompletableFuture<List<RPKMinecraftProfileLinkRequest>> {
        return plugin.database.getTable(RPKMinecraftProfileLinkRequestTable::class.java).get(minecraftProfile)
    }

    private fun addMinecraftProfileLinkRequest(minecraftProfileLinkRequest: RPKMinecraftProfileLinkRequest): CompletableFuture<Void> {
        return plugin.database.getTable(RPKMinecraftProfileLinkRequestTable::class.java).insert(minecraftProfileLinkRequest)
    }

    override fun createMinecraftProfileLinkRequest(
        profile: RPKProfile,
        minecraftProfile: RPKMinecraftProfile
    ): CompletableFuture<RPKMinecraftProfileLinkRequest> {
        return CompletableFuture.supplyAsync {
            val linkRequest = RPKMinecraftProfileLinkRequestImpl(profile, minecraftProfile)
            addMinecraftProfileLinkRequest(linkRequest).join()
            return@supplyAsync linkRequest
        }
    }

    override fun removeMinecraftProfileLinkRequest(minecraftProfileLinkRequest: RPKMinecraftProfileLinkRequest): CompletableFuture<Void> {
        return plugin.database.getTable(RPKMinecraftProfileLinkRequestTable::class.java).delete(minecraftProfileLinkRequest)
    }

}