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

package com.rpkit.players.bukkit.listener

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileName
import com.rpkit.players.bukkit.profile.RPKProfileService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent


class AsyncPlayerPreLoginListener(private val plugin: RPKPlayersBukkit) : Listener {

    @EventHandler
    fun onAsyncPlayerPreLogin(event: AsyncPlayerPreLoginEvent) {
        val profileService = Services[RPKProfileService::class.java]
        if (profileService == null) {
            plugin.logger.warning("Profile service not found so could not create profiles for ${event.uniqueId}. Did the plugin enable correctly?")
            return
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            plugin.logger.warning("Minecraft profile service not found so could not create profiles for ${event.uniqueId}. Did the plugin enable correctly?")
            return
        }
        var minecraftProfile = minecraftProfileService.loadMinecraftProfile(event.uniqueId).join()
        if (minecraftProfile == null) { // Player hasn't logged in while profile generation is active
            // Kotlin doesn't infer nullability from the type parameter on the CompletableFuture
            // We define createdMinecraftProfile as non-nullable explicitly so it can infer minecraftProfile isn't null later
            val createdMinecraftProfile: RPKMinecraftProfile = minecraftProfileService.createAndLoadMinecraftProfile(
                event.uniqueId,
                profileService.createThinProfile(RPKProfileName(event.name))
            ).join()
            minecraftProfile = createdMinecraftProfile
        } else if (minecraftProfileService.getMinecraftProfileLinkRequests(minecraftProfile).join().isNotEmpty()) {
            // Minecraft profile has a link request, so skip and let them know on join.
            return
        }
        val profile = minecraftProfile.profile
        if (profile !is RPKProfile) {
            val profileName = RPKProfileName(event.name)
            profileService.generateDiscriminatorFor(profileName).thenAcceptAsync { discriminator ->
                profileService.createAndLoadProfile(
                    profileName,
                    discriminator
                ).thenAcceptAsync { newProfile ->
                    minecraftProfile.profile = newProfile
                    minecraftProfileService.updateMinecraftProfile(minecraftProfile).join()
                }.join()
            }.join()
        } else {
            val profileId = profile.id
            if (profileId != null) {
                profileService.loadProfile(profileId).join()
            }
        }
    }
}