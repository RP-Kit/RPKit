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

package com.rpkit.chat.bukkit.command.chatgroup

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroupService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Chat group disband command.
 * Disbands a chat group.
 */
class ChatGroupDisbandCommand(private val plugin: RPKChatBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.chat.command.chatgroup.disband")) {
            sender.sendMessage(plugin.messages["no-permission-chat-group-disband"])
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages["chat-group-disband-usage"])
            return true
        }
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-service"])
            return true
        }
        val chatGroupService = Services[RPKChatGroupService::class.java]
        if (chatGroupService == null) {
            sender.sendMessage(plugin.messages["no-chat-group-service"])
            return true
        }
        val chatGroup = chatGroupService.getChatGroup(args[0])
        if (chatGroup == null) {
            sender.sendMessage(plugin.messages["chat-group-disband-invalid-nonexistent"])
            return true
        }
        val senderMinecraftProfile = minecraftProfileService.getMinecraftProfile(sender)
        if (senderMinecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile"])
            return true
        }
        if (!chatGroup.members.any { memberMinecraftProfile ->
                    memberMinecraftProfile.id == senderMinecraftProfile.id
                }) {
            sender.sendMessage(plugin.messages["chat-group-disband-invalid-not-a-member"])
        } else {
            for (minecraftProfile in chatGroup.members) {
                minecraftProfile.sendMessage(plugin.messages["chat-group-disband-valid", mapOf(
                    "group" to chatGroup.name
                )])
            }
            chatGroupService.removeChatGroup(chatGroup)
        }
        return true
    }

}