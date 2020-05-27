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

import com.rpkit.chat.bukkit.discord.RPKDiscordProvider
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import com.rpkit.players.bukkit.profile.RPKProfile
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class AccountLinkDiscordCommand(private val plugin: RPKPlayersBukkit): CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        if (!sender.hasPermission("rpkit.players.command.account.link.discord")) {
            sender.sendMessage(plugin.messages["no-permission-account-link-discord"])
            return true
        }
        val discordUserName = args[0]
        val discordProvider = plugin.core.serviceManager.getServiceProvider(RPKDiscordProvider::class)
        val discordUser = try {
            discordProvider.getUser(discordUserName)
        } catch (exception: IllegalArgumentException) {
            sender.sendMessage(plugin.messages["account-link-discord-invalid-user-tag"])
            return true
        }
        if (discordUser == null) {
            sender.sendMessage(plugin.messages["account-link-discord-invalid-user"])
            return true
        }
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(sender)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-self"])
            return true
        }
        val profile = minecraftProfile.profile
        if (profile !is RPKProfile) {
            sender.sendMessage(plugin.messages["no-profile-self"])
            return true
        }
        discordUser.openPrivateChannel().queue { privateChannel ->
            privateChannel.sendMessage("There was a request to link this account to profile ${profile.name}. " +
                    "Press tick to accept this request.")
                    .queue { message ->
                        message.addReaction("\u2705").queue()
                        discordProvider.setMessageAsProfileLinkRequest(message, profile)
                    }
        }
        return true
    }
}