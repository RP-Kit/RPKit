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
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent

class AsyncPlayerPreLoginListener(private val plugin: RPKTravelBukkit) : Listener {

    @EventHandler
    fun onAsyncPlayerPreLogin(event: AsyncPlayerPreLoginEvent) {
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, plugin.messages.noMinecraftProfileService)
            return
        }
        val untamerService = Services[RPKUntamerService::class.java] ?: return
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(event.uniqueId).join()
        val minecraftProfileId = minecraftProfile?.id
        if (minecraftProfileId != null) {
            untamerService.loadUntaming(minecraftProfileId).join()
        }
    }

}