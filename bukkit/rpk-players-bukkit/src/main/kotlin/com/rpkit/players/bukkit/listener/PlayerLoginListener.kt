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

import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.profile.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent


class PlayerLoginListener(private val plugin: RPKPlayersBukkit): Listener {

    @EventHandler
    fun onPlayerLogin(event: PlayerLoginEvent) {
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        var minecraftProfile = minecraftProfileProvider.getMinecraftProfile(event.player)
        if (minecraftProfile == null) { // Player hasn't logged in while profile generation is active
            minecraftProfile = RPKMinecraftProfileImpl(
                    profile = RPKThinProfileImpl(event.player.name),
                    minecraftUUID = event.player.uniqueId
            )
            minecraftProfileProvider.addMinecraftProfile(minecraftProfile)
        } else if (minecraftProfileProvider.getMinecraftProfileLinkRequests(minecraftProfile).isNotEmpty()) { // Minecraft profile has a link request, so skip and let them know on join.
            return
        }
        val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
        var profile = minecraftProfile.profile
        if (profile !is RPKProfile) {
            profile = RPKProfileImpl(
                    event.player.name,
                    ""
            )
            profileProvider.addProfile(profile)
            minecraftProfile.profile = profile
            minecraftProfileProvider.updateMinecraftProfile(minecraftProfile)
        }
    }
}