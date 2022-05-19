/*
 * Copyright 2022 Ren Binden
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

package com.rpkit.chat.bukkit.command.message

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroupName
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroupService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

/**
 * Message command.
 * Messages a chat group or an individual.
 */
class MessageCommand(private val plugin: RPKChatBukkit) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.size < 2) {
            sender.sendMessage(plugin.messages["message-usage"])
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
        val senderMinecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(sender)
        if (senderMinecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile"])
            return true
        }
        chatGroupService.getChatGroup(RPKChatGroupName(args[0])).thenAccept { chatGroup ->
            plugin.server.scheduler.runTask(plugin, Runnable {
                if (chatGroup != null) {
                    if (!sender.hasPermission("rpkit.chat.command.chatgroup.message")) {
                        sender.sendMessage(plugin.messages["no-permission-chat-group-message"])
                        return@Runnable
                    }
                    chatGroup.members.thenAccept getMembers@{ members ->
                        if (members.none { memberMinecraftProfile ->
                                memberMinecraftProfile.id == senderMinecraftProfile.id
                            }) {
                            sender.sendMessage(plugin.messages["chat-group-message-invalid-not-a-member"])
                            return@getMembers
                        }
                        chatGroup.sendMessage(senderMinecraftProfile, args.drop(1).joinToString(" "))
                    }
                } else {
                    if (plugin.server.getPlayer(args[0]) == null) {
                        sender.sendMessage(plugin.messages["message-invalid-target"])
                        return@Runnable
                    }
                    if (!sender.hasPermission("rpkit.chat.command.message")) {
                        sender.sendMessage(plugin.messages["no-permission-message"])
                        return@Runnable
                    }
                    val receiver = plugin.server.getPlayer(args[0])
                    if (receiver == null) {
                        sender.sendMessage(plugin.messages["message-invalid-target"])
                        return@Runnable
                    }
                    val receiverMinecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(receiver)
                    if (receiverMinecraftProfile == null) {
                        sender.sendMessage(plugin.messages["no-minecraft-profile"])
                        return@Runnable
                    }
                    if (receiverMinecraftProfile == senderMinecraftProfile) {
                        sender.sendMessage(plugin.messages["message-invalid-self"])
                        return@Runnable
                    }
                    chatGroupService.getChatGroup(RPKChatGroupName("_pm_" + sender.name + "_" + receiver.name))
                        .thenAccept { chatGroup1 ->
                            chatGroupService.getChatGroup(RPKChatGroupName("_pm_" + receiver.name + "_" + sender.name))
                                .thenAccept { chatGroup2 ->
                                    when {
                                        chatGroup1 != null -> CompletableFuture.completedFuture(chatGroup1)
                                        chatGroup2 != null -> CompletableFuture.completedFuture(chatGroup2)
                                        else -> {
                                            chatGroupService.createChatGroup(RPKChatGroupName("_pm_" + sender.getName() + "_" + receiver.name))
                                                .thenApplyAsync { createdGroup ->
                                                    CompletableFuture.allOf(
                                                        createdGroup.addMember(senderMinecraftProfile),
                                                        createdGroup.addMember(receiverMinecraftProfile)
                                                    ).join()
                                                    return@thenApplyAsync createdGroup
                                                }

                                        }
                                    }.thenAccept { pmGroup ->
                                        pmGroup.sendMessage(senderMinecraftProfile, args.drop(1).joinToString(" "))
                                    }
                                }
                        }
                }
            })
        }
        return true
    }
}