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

package com.rpkit.chat.bukkit.command.snoop

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.snooper.RPKSnooperService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Snoop command.
 * Toggles snoop state.
 */
class SnoopCommand(private val plugin: RPKChatBukkit) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val snooperService = Services[RPKSnooperService::class]
        if (snooperService == null) {
            sender.sendMessage(plugin.messages["no-snooper-service"])
            return true
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-service"])
            return true
        }
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(sender)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile"])
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages["snoop-usage"])
            return true
        }
        if (args[0].equals("on", ignoreCase = true)) {
            if (!sender.hasPermission("rpkit.chat.command.snoop.on")) {
                sender.sendMessage(plugin.messages["no-permission-snoop-on"])
                return true
            }
            if (snooperService.snoopers.contains(minecraftProfile)) {
                sender.sendMessage(plugin.messages["snoop-already-enabled"])
                return true
            }
            snooperService.addSnooper(minecraftProfile)
            sender.sendMessage(plugin.messages["snoop-enabled"])
        } else if (args[0].equals("off", ignoreCase = true)) {
            if (!sender.hasPermission("rpkit.chat.command.snoop.off")) {
                sender.sendMessage(plugin.messages["no-permission-snoop-off"])
                return true
            }
            if (!snooperService.snoopers.contains(minecraftProfile)) {
                sender.sendMessage(plugin.messages["snoop-already-disabled"])
                return true
            }
            snooperService.removeSnooper(minecraftProfile)
            sender.sendMessage(plugin.messages["snoop-disabled"])
        } else if (args[0].equals("check", ignoreCase = true)) {
            if (!sender.hasPermission("rpkit.chat.command.snoop.check")) {
                sender.sendMessage(plugin.messages["no-permission-snoop-check"])
                return true
            }
            if (snooperService.snoopers.contains(minecraftProfile)) {
                sender.sendMessage(plugin.messages["snoop-check-on"])
            } else {
                sender.sendMessage(plugin.messages["snoop-check-off"])
            }
        } else {
            sender.sendMessage(plugin.messages["snoop-usage"])
        }
        return true
    }
}