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
import com.rpkit.travel.bukkit.RPKTravelBukkit
import com.rpkit.warp.bukkit.warp.RPKWarpName
import com.rpkit.warp.bukkit.warp.RPKWarpService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class DeleteWarpCommand(private val plugin: RPKTravelBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (!sender.hasPermission("rpkit.travel.command.deletewarp")) {
            sender.sendMessage(plugin.messages.noPermissionDeleteWarp)
            return true
        }
        if (sender !is Player) {
            sender.sendMessage(plugin.messages.notFromConsole)
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages.deleteWarpUsage)
            return true
        }
        val warpService = Services[RPKWarpService::class.java]
        if (warpService == null) {
            sender.sendMessage(plugin.messages.noWarpService)
            return true
        }
        warpService.getWarp(RPKWarpName(args[0].toLowerCase())).thenAccept { warp ->
            if (warp == null) {
                sender.sendMessage(plugin.messages.deleteWarpInvalidWarp)
                return@thenAccept
            }
            warpService.removeWarp(warp).thenRun {
                sender.sendMessage(
                    plugin.messages.deleteWarpValid.withParameters(
                        warp = warp
                    )
                )
            }
        }
        return true
    }
}
