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

package com.rpkit.travel.bukkit.command

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.travel.bukkit.RPKTravelBukkit
import com.rpkit.warp.bukkit.event.warp.RPKBukkitWarpUseEvent
import com.rpkit.warp.bukkit.warp.RPKWarp
import com.rpkit.warp.bukkit.warp.RPKWarpName
import com.rpkit.warp.bukkit.warp.RPKWarpService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.ArrayList

class WarpCommand(private val plugin: RPKTravelBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (!sender.hasPermission("rpkit.travel.command.warp")) {
            sender.sendMessage(plugin.messages["no-permission-warp"])
            return true
        }
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        val warpService = Services[RPKWarpService::class.java]
        if (warpService == null) {
            sender.sendMessage(plugin.messages["no-warp-service"])
            return true
        }
        if (args.isNotEmpty()) {
            val warp = warpService.getWarp(RPKWarpName(args[0].toLowerCase()))
            if (warp == null) {
                sender.sendMessage(plugin.messages["warp-invalid-warp"])
                return true
            }
            val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
            if (minecraftProfileService == null) {
                sender.sendMessage(plugin.messages["no-minecraft-profile-service"])
                return true
            }
            val minecraftProfile = minecraftProfileService.getMinecraftProfile(sender)
            if (minecraftProfile == null) {
                sender.sendMessage(plugin.messages["no-minecraft-profile"])
                return true
            }
            val event = RPKBukkitWarpUseEvent(warp, minecraftProfile)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return true
            sender.teleport(event.warp.location)
            sender.sendMessage(plugin.messages["warp-valid", mapOf(
                "warp" to warp.name.value
            )])
        } else {
            if (warpService.warps.isEmpty()) {
                sender.sendMessage(plugin.messages["warp-list-invalid-empty"])
                return true
            }
            sender.sendMessage(plugin.messages["warp-list-title"])
            val warps = warpService.warps.map(RPKWarp::name)
            val warpMessages = ArrayList<String>()
            var warpsBuilder = StringBuilder()
            for (i in warps.indices) {
                warpsBuilder.append(warps[i]).append(", ")
                if ((i + 1) % 10 == 0) {
                    if (i == warps.size - 1) {
                        warpsBuilder.delete(warpsBuilder.length - 2, warpsBuilder.length)
                    }
                    warpMessages.add(warpsBuilder.toString())
                    warpsBuilder = StringBuilder()
                }
            }
            if (warpsBuilder.isNotEmpty()) {
                warpMessages.add(warpsBuilder.delete(warpsBuilder.length - 2, warpsBuilder.length).toString())
            }
            for (message in warpMessages) {
                sender.sendMessage(plugin.messages["warp-list-item", mapOf(
                    "warps" to message
                )])
            }
        }
        return true
    }

}
