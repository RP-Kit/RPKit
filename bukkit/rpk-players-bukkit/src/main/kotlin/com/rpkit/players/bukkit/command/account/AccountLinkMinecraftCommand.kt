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

package com.rpkit.players.bukkit.command.account

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.profile.*
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


class AccountLinkMinecraftCommand(private val plugin: RPKPlayersBukkit) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        if (!sender.hasPermission("rpkit.players.command.account.link.minecraft")) {
            sender.sendMessage(plugin.messages["no-permission-account-link-minecraft"])
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages["account-link-minecraft-usage"])
            return true
        }
        val minecraftUsername = args[0]
        val bukkitPlayer = plugin.server.getOfflinePlayer(minecraftUsername)
        val minecraftProfileService = Services[RPKMinecraftProfileService::class]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-service"])
            return true
        }
        var minecraftProfile = minecraftProfileService.getMinecraftProfile(bukkitPlayer)
        if (minecraftProfile != null) {
            sender.sendMessage(plugin.messages["account-link-minecraft-invalid-minecraft-profile"])
            return true
        }
        val senderMinecraftProfile = minecraftProfileService.getMinecraftProfile(sender)
        if (senderMinecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-self"])
            return true
        }
        val profile = senderMinecraftProfile.profile
        if (profile !is RPKProfile) {
            sender.sendMessage(plugin.messages["no-profile-self"])
            return true
        }
        minecraftProfile = RPKMinecraftProfileImpl(profile = RPKThinProfileImpl(bukkitPlayer.name
                ?: "Unknown Minecraft user"), minecraftUUID = bukkitPlayer.uniqueId)
        minecraftProfileService.addMinecraftProfile(minecraftProfile)
        val minecraftProfileLinkRequest = RPKMinecraftProfileLinkRequestImpl(profile = profile, minecraftProfile = minecraftProfile)
        minecraftProfileService.addMinecraftProfileLinkRequest(minecraftProfileLinkRequest)
        sender.sendMessage(plugin.messages["account-link-minecraft-valid"])
        return true
    }
}