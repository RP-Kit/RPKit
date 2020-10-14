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

package com.rpkit.chat.bukkit.command.reply

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroupService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Reply command.
 * Replies to the last chat group used.
 */
class ReplyCommand(private val plugin: RPKChatBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.chat.command.reply")) {
            sender.sendMessage(plugin.messages["no-permission-reply"])
            return true
        }
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-service"])
            return true
        }
        val chatGroupService = Services[RPKChatGroupService::class]
        if (chatGroupService == null) {
            sender.sendMessage(plugin.messages["no-chat-group-service"])
            return true
        }
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(sender)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile"])
            return true
        }
        val chatGroup = chatGroupService.getLastUsedChatGroup(minecraftProfile)
        if (chatGroup == null) {
            sender.sendMessage(plugin.messages["reply-invalid-chat-group"])
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages["reply-usage"])
            return true
        }
        chatGroup.sendMessage(minecraftProfile, args.joinToString(" "))
        return true
    }

}