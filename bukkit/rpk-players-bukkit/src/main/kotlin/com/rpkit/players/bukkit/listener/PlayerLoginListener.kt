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

package com.rpkit.players.bukkit.listener

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileImpl
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileImpl
import com.rpkit.players.bukkit.profile.RPKProfileService
import com.rpkit.players.bukkit.profile.RPKThinProfileImpl
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent


class PlayerLoginListener(private val plugin: RPKPlayersBukkit) : Listener {

    @EventHandler
    fun onPlayerLogin(event: PlayerLoginEvent) {
        val profileService = Services[RPKProfileService::class.java]
        if (profileService == null) {
            plugin.logger.warning("Profile service not found so could not create profiles for ${event.player.name}. Did the plugin enable correctly?")
            return
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            plugin.logger.warning("Minecraft profile service not found so could not create profiles for ${event.player.name}. Did the plugin enable correctly?")
            return
        }
        var minecraftProfile = minecraftProfileService.getMinecraftProfile(event.player)
        if (minecraftProfile == null) { // Player hasn't logged in while profile generation is active
            minecraftProfile = minecraftProfileService.createMinecraftProfile(
                event.player.name,
                profileService.createThinProfile(event.player.name)
            )
        } else if (minecraftProfileService.getMinecraftProfileLinkRequests(minecraftProfile).isNotEmpty()) { // Minecraft profile has a link request, so skip and let them know on join.
            return
        }
        var profile = minecraftProfile.profile
        if (profile !is RPKProfile) {
            profile = RPKProfileImpl(
                    event.player.name,
                    profileService.generateDiscriminatorFor(event.player.name),
                    ""
            )
            profileService.addProfile(profile)
            minecraftProfile.profile = profile
            minecraftProfileService.updateMinecraftProfile(minecraftProfile)
        }
    }
}