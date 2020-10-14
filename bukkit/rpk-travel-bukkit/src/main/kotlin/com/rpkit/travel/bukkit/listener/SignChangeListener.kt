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
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent


class SignChangeListener(private val plugin: RPKTravelBukkit) : Listener {

    @EventHandler
    fun onSignChange(event: SignChangeEvent) {
        if (!event.getLine(0).equals("[warp]", ignoreCase = true)) return
        if (!event.player.hasPermission("rpkit.travel.sign.warp.create")) {
            event.player.sendMessage(plugin.messages["no-permission-warp-sign-create"])
            return
        }
        val warpService = Services[RPKWarpService::class]
        if (warpService == null) {
            event.player.sendMessage(plugin.messages["no-warp-service"])
            return
        }
        val warp = warpService.getWarp(event.getLine(1) ?: "")
        if (warp == null) {
            event.block.breakNaturally()
            event.player.sendMessage(plugin.messages["warp-sign-invalid-warp"])
            return
        }
        event.setLine(0, "$GREEN[warp]")
        event.player.sendMessage(plugin.messages["warp-sign-valid"])
    }

}