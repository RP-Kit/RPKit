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
import com.rpkit.travel.bukkit.warp.RPKWarpImpl
import com.rpkit.warp.bukkit.warp.RPKWarpName
import com.rpkit.warp.bukkit.warp.RPKWarpService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SetWarpCommand(private val plugin: RPKTravelBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (!sender.hasPermission("rpkit.travel.command.setwarp")) {
            sender.sendMessage(plugin.messages.noPermissionSetWarp)
            return true
        }
        if (sender !is Player) {
            sender.sendMessage(plugin.messages.notFromConsole)
            return true
        }
        if (!args.isNotEmpty()) {
            sender.sendMessage(plugin.messages.setWarpUsage)
            return true
        }
        val warpService = Services[RPKWarpService::class.java]
        if (warpService == null) {
            sender.sendMessage(plugin.messages.noWarpService)
            return true
        }
        if (warpService.getWarp(RPKWarpName(args[0])) != null) {
            sender.sendMessage(plugin.messages.setWarpInvalidNameAlreadyInUse)
            return true
        }
        val warp = RPKWarpImpl(name = RPKWarpName(args[0]), location = sender.location)
        warpService.addWarp(warp)
        sender.sendMessage(plugin.messages.setWarpValid.withParameters(
            warp = warp,
            world = warp.location.world!!,
            x = warp.location.blockX,
            y = warp.location.blockY,
            z = warp.location.blockZ
        ))
        return true
    }

}
