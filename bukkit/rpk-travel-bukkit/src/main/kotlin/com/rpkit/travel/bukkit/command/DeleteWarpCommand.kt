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

package com.rpkit.travel.bukkit.command

import com.rpkit.core.service.Services
import com.rpkit.travel.bukkit.RPKTravelBukkit
import com.rpkit.warp.bukkit.warp.RPKWarpService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class DeleteWarpCommand(private val plugin: RPKTravelBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("rpkit.travel.command.deletewarp")) {
            if (sender is Player) {
                if (args.isNotEmpty()) {
                    val warpService = Services[RPKWarpService::class]
                    if (warpService == null) {
                        sender.sendMessage(plugin.messages["no-warp-service"])
                        return true
                    }
                    val warp = warpService.getWarp(args[0].toLowerCase())
                    if (warp != null) {
                        warpService.removeWarp(warp)
                        sender.sendMessage(plugin.messages["delete-warp-valid", mapOf(
                                Pair("warp", warp.name)
                        )])
                    }
                } else {
                    sender.sendMessage(plugin.messages["delete-warp-usage"])
                }
            } else {
                sender.sendMessage(plugin.messages["not-from-console"])
            }
        } else {
            sender.sendMessage(plugin.messages["no-permission-delete-warp"])
        }
        return true
    }
}
