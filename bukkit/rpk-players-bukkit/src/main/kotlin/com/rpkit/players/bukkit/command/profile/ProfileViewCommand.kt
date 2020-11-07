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

package com.rpkit.players.bukkit.command.profile

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileService
import com.rpkit.players.bukkit.profile.RPKProfile
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ProfileViewCommand(private val plugin: RPKPlayersBukkit) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        var target: Player? = null
        if (args.isNotEmpty() && sender.hasPermission("rpkit.players.profile.view.other")) {
            target = plugin.server.getPlayer(args[0])
        }
        if (target == null) {
            if (!sender.hasPermission("rpkit.players.profile.view.self")) {
                sender.sendMessage(plugin.messages["no-permission-profile-view-self"])
                return true
            }
            if (sender is Player) {
                target = sender
            } else {
                sender.sendMessage(plugin.messages["profile-view-invalid-target"])
                return true
            }
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-service"])
            return true
        }
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(target)
        if (minecraftProfile == null) {
            if (sender == target) {
                sender.sendMessage(plugin.messages["no-minecraft-profile-self"])
            } else {
                sender.sendMessage(plugin.messages["no-minecraft-profile-other"])
            }
            return true
        }
        val profile = minecraftProfile.profile
        if (profile !is RPKProfile) {
            if (sender == target) {
                sender.sendMessage(plugin.messages["no-profile-self"])
            } else {
                sender.sendMessage(plugin.messages["no-profile-other"])
            }
            return true
        }
        sender.sendMessage(
                plugin.messages.getList("profile-view-valid", mapOf(
                        "name" to profile.name,
                        "discriminator" to profile.discriminator.toString()
                )).toTypedArray()
        )
        return true
    }
}