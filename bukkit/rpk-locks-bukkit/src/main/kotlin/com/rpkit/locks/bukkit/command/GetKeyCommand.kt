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

package com.rpkit.locks.bukkit.command

import com.rpkit.core.service.Services
import com.rpkit.locks.bukkit.RPKLocksBukkit
import com.rpkit.locks.bukkit.lock.RPKLockService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


class GetKeyCommand(private val plugin: RPKLocksBukkit) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender.hasPermission("rpkit.locks.command.getkey")) {
            if (sender is Player) {
                val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
                if (minecraftProfileService == null) {
                    sender.sendMessage(plugin.messages["no-minecraft-profile-service"])
                    return true
                }
                val lockService = Services[RPKLockService::class.java]
                if (lockService == null) {
                    sender.sendMessage(plugin.messages["no-lock-service"])
                    return true
                }
                val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(sender)
                if (minecraftProfile != null) {
                    lockService.setGettingKey(minecraftProfile, true)
                    sender.sendMessage(plugin.messages["get-key-valid"])
                } else {
                    sender.sendMessage(plugin.messages["no-minecraft-profile"])
                }
            } else {
                sender.sendMessage(plugin.messages["not-from-console"])
            }
        } else {
            sender.sendMessage(plugin.messages["no-permission-get-key"])
        }
        return true
    }
}