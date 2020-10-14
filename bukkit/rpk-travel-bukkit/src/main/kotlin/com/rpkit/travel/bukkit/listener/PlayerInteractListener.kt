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

package com.rpkit.travel.bukkit.listener

import com.rpkit.core.service.Services
import com.rpkit.travel.bukkit.RPKTravelBukkit
import com.rpkit.warp.bukkit.warp.RPKWarpService
import org.bukkit.ChatColor.GREEN
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent


class PlayerInteractListener(private val plugin: RPKTravelBukkit) : Listener {

    @EventHandler
    fun onPlayerInteractListener(event: PlayerInteractEvent) {
        val clickedBlock = event.clickedBlock ?: return
        val sign = clickedBlock.state as? Sign ?: return
        if (!sign.getLine(0).equals("$GREEN[warp]", ignoreCase = true)) return
        val warpService = Services[RPKWarpService::class]
        if (warpService == null) {
            event.player.sendMessage(plugin.messages["no-warp-service"])
            return
        }
        val warp = warpService.getWarp(sign.getLine(1))
        if (warp != null) {
            event.player.teleport(warp.location)
        } else {
            event.player.sendMessage(plugin.messages["warp-invalid-warp"])
        }
    }

}