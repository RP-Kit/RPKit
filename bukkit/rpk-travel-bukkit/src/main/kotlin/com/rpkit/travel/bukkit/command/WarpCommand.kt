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

import com.rpkit.core.bukkit.location.toBukkitLocation
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.travel.bukkit.RPKTravelBukkit
import com.rpkit.warp.bukkit.event.warp.RPKBukkitWarpUseEvent
import com.rpkit.warp.bukkit.warp.RPKWarpName
import com.rpkit.warp.bukkit.warp.RPKWarpService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class WarpCommand(private val plugin: RPKTravelBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (!sender.hasPermission("rpkit.travel.command.warp")) {
            sender.sendMessage(plugin.messages.noPermissionWarp)
            return true
        }
        if (sender !is Player) {
            sender.sendMessage(plugin.messages.notFromConsole)
            return true
        }
        val warpService = Services[RPKWarpService::class.java]
        if (warpService == null) {
            sender.sendMessage(plugin.messages.noWarpService)
            return true
        }
        if (args.isNotEmpty()) {
            warpService.getWarp(RPKWarpName(args[0].toLowerCase())).thenAccept { warp ->
                if (warp == null) {
                    sender.sendMessage(plugin.messages.warpInvalidWarp)
                    return@thenAccept
                }
                val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
                if (minecraftProfileService == null) {
                    sender.sendMessage(plugin.messages.noMinecraftProfileService)
                    return@thenAccept
                }
                val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(sender)
                if (minecraftProfile == null) {
                    sender.sendMessage(plugin.messages.noMinecraftProfile)
                    return@thenAccept
                }
                plugin.server.scheduler.runTask(plugin, Runnable {
                    val event = RPKBukkitWarpUseEvent(warp, minecraftProfile)
                    plugin.server.pluginManager.callEvent(event)
                    if (event.isCancelled) return@Runnable
                    event.warp.location.toBukkitLocation()?.let { sender.teleport(it) }
                    sender.sendMessage(plugin.messages.warpValid.withParameters(warp = event.warp))
                })
            }
        } else {
            warpService.warps.thenAccept { warps ->
                if (warps.isEmpty()) {
                    sender.sendMessage(plugin.messages.warpListInvalidEmpty)
                    return@thenAccept
                }
                sender.sendMessage(plugin.messages.warpListTitle)
                val warpNames = warps.map { warp -> warp.name.value }
                val warpMessages = ArrayList<String>()
                var warpsBuilder = StringBuilder()
                for (i in warpNames.indices) {
                    warpsBuilder.append(warpNames[i]).append(", ")
                    if ((i + 1) % 10 == 0) {
                        if (i == warpNames.size - 1) {
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
                    sender.sendMessage(plugin.messages.warpListItem.withParameters(warps = message))
                }
            }
        }
        return true
    }

}
