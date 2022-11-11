/*
 * Copyright 2022 Ren Binden
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

package com.rpkit.travel.bukkit.listener

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.travel.bukkit.RPKTravelBukkit
import com.rpkit.travel.bukkit.untamer.RPKUntamerService
import org.bukkit.entity.Tameable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent

class PlayerInteractEntityListener(private val plugin: RPKTravelBukkit) : Listener {

    @EventHandler
    fun onPlayerInteractEntity(event: PlayerInteractEntityEvent) {
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        val minecraftProfileId = minecraftProfileService.getPreloadedMinecraftProfile(event.player)?.id ?: return
        val untamerService = Services[RPKUntamerService::class.java] ?: return
        if (!untamerService.isUntaming(minecraftProfileId)) return
        val creature = event.rightClicked
        if (creature !is Tameable) return
        if (creature.owner != event.player) return
        creature.isTamed = false
        untamerService.setUntaming(minecraftProfileId, false)
        event.player.sendMessage(plugin.messages.untameValid)
        event.isCancelled = true
    }

}