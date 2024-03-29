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

package com.rpkit.chat.bukkit.chatgroup

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.database.table.RPKChatGroupInviteTable
import com.rpkit.chat.bukkit.database.table.RPKChatGroupMemberTable
import com.rpkit.chat.bukkit.event.chatgroup.*
import com.rpkit.chat.bukkit.prefix.RPKPrefixService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import org.bukkit.ChatColor
import java.util.concurrent.CompletableFuture
import java.util.logging.Level

/**
 * Chat group implementation.
 */
class RPKChatGroupImpl(
        private val plugin: RPKChatBukkit,
        override var id: RPKChatGroupId? = null,
        override var name: RPKChatGroupName
) : RPKChatGroup {

    override val members: CompletableFuture<List<RPKMinecraftProfile>>
        get() = plugin.database.getTable(RPKChatGroupMemberTable::class.java).get(this)
            .thenApply { members -> members.map(RPKChatGroupMember::minecraftProfile) }

    override val invited: CompletableFuture<List<RPKMinecraftProfile>>
        get() = plugin.database.getTable(RPKChatGroupInviteTable::class.java).get(this)
            .thenApply { invited -> invited.map(RPKChatGroupInvite::minecraftProfile) }

    override fun addMember(minecraftProfile: RPKMinecraftProfile): CompletableFuture<Void> {
        return members.thenAcceptAsync { members ->
            if (!members.any { memberMinecraftProfile -> memberMinecraftProfile.id == minecraftProfile.id }) {
                val event = RPKBukkitChatGroupJoinEvent(minecraftProfile, this, true)
                plugin.server.pluginManager.callEvent(event)
                if (event.isCancelled) return@thenAcceptAsync
                plugin.database.getTable(RPKChatGroupMemberTable::class.java).insert(
                    RPKChatGroupMember(
                        chatGroup = event.chatGroup,
                        minecraftProfile = event.minecraftProfile
                    )
                ).join()
            }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to add chat group member", exception)
            throw exception
        }
    }

    override fun removeMember(minecraftProfile: RPKMinecraftProfile): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val event = RPKBukkitChatGroupLeaveEvent(
                minecraftProfile,
                this,
                true
            )
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            val chatGroupMemberTable = plugin.database.getTable(RPKChatGroupMemberTable::class.java)
            chatGroupMemberTable.get(event.minecraftProfile).thenAcceptAsync { members ->
                val futures = members.filter { member -> member.chatGroup == event.chatGroup }
                    .map { member -> chatGroupMemberTable.delete(member) }
                CompletableFuture.allOf(*futures.toTypedArray()).join()
            }.join()
        }
    }

    override fun invite(minecraftProfile: RPKMinecraftProfile): CompletableFuture<Void> {
        return invited.thenAcceptAsync { invited ->
            if (!invited.any { invitedMinecraftProfile -> invitedMinecraftProfile.id == minecraftProfile.id }) {
                val event = RPKBukkitChatGroupInviteEvent(minecraftProfile, this, true)
                plugin.server.pluginManager.callEvent(event)
                if (event.isCancelled) return@thenAcceptAsync
                plugin.database.getTable(RPKChatGroupInviteTable::class.java).insert(
                    RPKChatGroupInvite(
                        chatGroup = event.chatGroup,
                        minecraftProfile = event.minecraftProfile
                    )
                ).join()
            }
        }

    }

    override fun uninvite(minecraftProfile: RPKMinecraftProfile): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val event = RPKBukkitChatGroupUninviteEvent(minecraftProfile, this, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            val chatGroupInviteTable = plugin.database.getTable(RPKChatGroupInviteTable::class.java)
            chatGroupInviteTable.get(event.minecraftProfile).thenAcceptAsync { invites ->
                val futures = invites.filter { invite -> invite.chatGroup == event.chatGroup }
                    .map { invite -> chatGroupInviteTable.delete(invite) }
                CompletableFuture.allOf(*futures.toTypedArray()).join()
            }.join()
        }
    }

    override fun sendMessage(sender: RPKMinecraftProfile, message: String): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val event = RPKBukkitChatGroupMessageEvent(sender, this, message, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            val prefixService = Services[RPKPrefixService::class.java]
            val characterService = Services[RPKCharacterService::class.java]
            val chatGroupService = Services[RPKChatGroupService::class.java]
            val senderCharacter = characterService?.getPreloadedActiveCharacter(sender)
            members.thenAcceptAsync { members ->
                members.forEach { receiver ->
                    val receiverCharacter = characterService?.getPreloadedActiveCharacter(receiver)
                    val formatString = plugin.config.getString("chat-group.format") ?: return@forEach
                    var formattedMessage = ChatColor.translateAlternateColorCodes('&', formatString)
                    if (formattedMessage.contains("\${message}")) {
                        formattedMessage = formattedMessage.replace("\${message}", message)
                    }
                    if (formattedMessage.contains("\${sender-prefix}")) {
                        val profile = sender.profile
                        formattedMessage = if (profile is RPKProfile) {
                            formattedMessage.replace(
                                "\${sender-prefix}",
                                prefixService?.getPrefix(profile)?.join() ?: ""
                            )
                        } else {
                            formattedMessage.replace("\${sender-prefix}", "")
                        }
                    }
                    if (formattedMessage.contains("\${sender-player}")) {
                        formattedMessage = formattedMessage.replace("\${sender-player}", sender.name)
                    }
                    if (formattedMessage.contains("\${sender-character}")) {
                        if (senderCharacter != null) {
                            formattedMessage = formattedMessage.replace("\${sender-character}", senderCharacter.name)
                        }
                    }
                    if (formattedMessage.contains("\${receiver-player}")) {
                        formattedMessage = formattedMessage.replace("\${receiver-player}", receiver.name)
                    }
                    if (formattedMessage.contains("\${receiver-character}")) {
                        if (receiverCharacter != null) {
                            formattedMessage =
                                formattedMessage.replace("\${receiver-character}", receiverCharacter.name)
                        }
                    }
                    if (formattedMessage.contains("\${group}")) {
                        formattedMessage = if (name.value.startsWith("_pm_")) {
                            formattedMessage.replace(
                                "\${group}",
                                sender.name + " -> " + members.first { member -> member.id != sender.id }.name
                            )
                        } else {
                            formattedMessage.replace("\${group}", name.value)
                        }
                    }
                    receiver.sendMessage(formattedMessage)
                    chatGroupService?.setLastUsedChatGroup(receiver, this)?.join()
                }
            }.join()
        }
    }

}
