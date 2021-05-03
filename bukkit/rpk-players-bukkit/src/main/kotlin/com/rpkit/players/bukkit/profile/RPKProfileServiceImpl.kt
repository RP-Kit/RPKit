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

package com.rpkit.players.bukkit.profile

import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.database.table.RPKProfileTable
import com.rpkit.players.bukkit.event.profile.RPKBukkitProfileCreateEvent
import com.rpkit.players.bukkit.event.profile.RPKBukkitProfileDeleteEvent
import com.rpkit.players.bukkit.event.profile.RPKBukkitProfileUpdateEvent
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap


class RPKProfileServiceImpl(override val plugin: RPKPlayersBukkit) : RPKProfileService {

    private val profilesById = ConcurrentHashMap<Int, RPKProfile>()
    private val profilesByTag = ConcurrentHashMap<String, RPKProfile>()

    override fun getProfile(id: RPKProfileId): CompletableFuture<RPKProfile?> {
        return plugin.database.getTable(RPKProfileTable::class.java)[id]
    }

    override fun getProfile(name: RPKProfileName, discriminator: RPKProfileDiscriminator): CompletableFuture<RPKProfile?> {
        return plugin.database.getTable(RPKProfileTable::class.java).get(name, discriminator)
    }

    override fun getPreloadedProfile(id: RPKProfileId): RPKProfile? {
        return profilesById[id.value]
    }

    override fun getPreloadedProfile(name: RPKProfileName, discriminator: RPKProfileDiscriminator): RPKProfile? {
        return profilesByTag[name + discriminator]
    }

    override fun loadProfile(id: RPKProfileId): CompletableFuture<RPKProfile?> {
        val preloadedProfile = getPreloadedProfile(id)
        if (preloadedProfile != null) return CompletableFuture.completedFuture(preloadedProfile)
        plugin.logger.info("Loading profile ${id.value}...")
        val profileFuture = plugin.database.getTable(RPKProfileTable::class.java)[id]
        profileFuture.thenAccept { profile ->
            if (profile != null) {
                profilesById[id.value] = profile
                profilesByTag[profile.name + profile.discriminator] = profile
                plugin.logger.info("Loaded profile ${profile.name + profile.discriminator} (${id.value})")
            }
        }
        return profileFuture
    }

    override fun unloadProfile(profile: RPKProfile) {
        val profileId = profile.id
        if (profileId != null) {
            profilesById.remove(profileId.value)
        }
        profilesByTag.remove(profile.name + profile.discriminator)
        plugin.logger.info("Unloaded profile ${profile.name + profile.discriminator}" + if (profileId != null) " (${profileId.value})" else "")
    }

    override fun addProfile(profile: RPKProfile): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val event = RPKBukkitProfileCreateEvent(profile, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            plugin.database.getTable(RPKProfileTable::class.java).insert(event.profile).join()
        }
    }

    override fun updateProfile(profile: RPKProfile): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val event = RPKBukkitProfileUpdateEvent(profile, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            plugin.database.getTable(RPKProfileTable::class.java).update(event.profile).join()
        }
    }

    override fun removeProfile(profile: RPKProfile): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val event = RPKBukkitProfileDeleteEvent(profile, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            plugin.database.getTable(RPKProfileTable::class.java).delete(event.profile).join()
        }
    }

    override fun createProfile(name: RPKProfileName, discriminator: RPKProfileDiscriminator?, password: String?): CompletableFuture<RPKProfile> {
        return CompletableFuture.supplyAsync {
            val profile = RPKProfileImpl(name, discriminator ?: generateDiscriminatorFor(name).join(), password)
            addProfile(profile).join()
            return@supplyAsync profile
        }
    }

    override fun createThinProfile(name: RPKProfileName): RPKThinProfile {
        return RPKThinProfileImpl(name)
    }

    override fun generateDiscriminatorFor(name: RPKProfileName): CompletableFuture<RPKProfileDiscriminator> {
        return plugin.database.getTable(RPKProfileTable::class.java).generateDiscriminatorFor(name)
    }

}