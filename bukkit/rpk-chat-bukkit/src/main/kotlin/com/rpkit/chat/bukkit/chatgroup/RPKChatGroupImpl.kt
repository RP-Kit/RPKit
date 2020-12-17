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

package com.rpkit.chat.bukkit.chatgroup

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.database.table.RPKChatGroupInviteTable
import com.rpkit.chat.bukkit.database.table.RPKChatGroupMemberTable
import com.rpkit.chat.bukkit.event.chatgroup.*
import com.rpkit.chat.bukkit.prefix.RPKPrefixService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKProfile
import org.bukkit.ChatColor

/**
 * Chat group implementation.
 */
class RPKChatGroupImpl(
        private val plugin: RPKChatBukkit,
        override var id: Int? = null,
        override var name: String
) : RPKChatGroup {

    override val members: List<RPKMinecraftProfile>
        get() = plugin.database.getTable(RPKChatGroupMemberTable::class.java).get(this).map(RPKChatGroupMember::minecraftProfile)

    override val invited: List<RPKMinecraftProfile>
        get() = plugin.database.getTable(RPKChatGroupInviteTable::class.java).get(this).map(RPKChatGroupInvite::minecraftProfile)

    override fun addMember(minecraftProfile: RPKMinecraftProfile) {
        if (!members.any { memberMinecraftProfile -> memberMinecraftProfile.id == minecraftProfile.id }) {
            val event = RPKBukkitChatGroupJoinEvent(minecraftProfile, this)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return
            plugin.database.getTable(RPKChatGroupMemberTable::class.java).insert(
                    RPKChatGroupMember(
                            chatGroup = event.chatGroup,
                            minecraftProfile = event.minecraftProfile
                    )
            )
        }
    }

    override fun removeMember(minecraftProfile: RPKMinecraftProfile) {
        val event = RPKBukkitChatGroupLeaveEvent(
                minecraftProfile,
                this
        )
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        val chatGroupMemberTable = plugin.database.getTable(RPKChatGroupMemberTable::class.java)
        chatGroupMemberTable.get(event.minecraftProfile)
                .filter { member -> member.chatGroup == event.chatGroup }
                .forEach { member -> chatGroupMemberTable.delete(member) }
    }

    override fun invite(minecraftProfile: RPKMinecraftProfile) {
        if (!invited
                        .any { invitedMinecraftProfile -> invitedMinecraftProfile.id == minecraftProfile.id }) {
            val event = RPKBukkitChatGroupInviteEvent(minecraftProfile, this)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return
            plugin.database.getTable(RPKChatGroupInviteTable::class.java).insert(
                    RPKChatGroupInvite(
                            chatGroup = event.chatGroup,
                            minecraftProfile = event.minecraftProfile
                    )
            )
        }
    }

    override fun uninvite(minecraftProfile: RPKMinecraftProfile) {
        val event = RPKBukkitChatGroupUninviteEvent(minecraftProfile, this)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        val chatGroupInviteTable = plugin.database.getTable(RPKChatGroupInviteTable::class.java)
        chatGroupInviteTable.get(event.minecraftProfile)
                .filter { invite -> invite.chatGroup == event.chatGroup }
                .forEach { invite -> chatGroupInviteTable.delete(invite) }
    }

    override fun sendMessage(sender: RPKMinecraftProfile, message: String) {
        val event = RPKBukkitChatGroupMessageEvent(sender, this, message)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        val prefixService = Services[RPKPrefixService::class.java]
        val characterService = Services[RPKCharacterService::class.java]
        val chatGroupService = Services[RPKChatGroupService::class.java]
        val senderCharacter = characterService?.getActiveCharacter(sender)
        members.forEach { receiver ->
            val receiverCharacter = characterService?.getActiveCharacter(receiver)
            val formatString = plugin.config.getString("chat-group.format") ?: return
            var formattedMessage = ChatColor.translateAlternateColorCodes('&', formatString)
            if (formattedMessage.contains("\$message")) {
                formattedMessage = formattedMessage.replace("\$message", message)
            }
            if (formattedMessage.contains("\$sender-prefix")) {
                val profile = sender.profile
                formattedMessage = if (profile is RPKProfile) {
                    formattedMessage.replace("\$sender-prefix", prefixService?.getPrefix(profile) ?: "")
                } else {
                    formattedMessage.replace("\$sender-prefix", "")
                }
            }
            if (formattedMessage.contains("\$sender-player")) {
                formattedMessage = formattedMessage.replace("\$sender-player", sender.minecraftUsername)
            }
            if (formattedMessage.contains("\$sender-character")) {
                if (senderCharacter != null) {
                    formattedMessage = formattedMessage.replace("\$sender-character", senderCharacter.name)
                }
            }
            if (formattedMessage.contains("\$receiver-player")) {
                formattedMessage = formattedMessage.replace("\$receiver-player", receiver.minecraftUsername)
            }
            if (formattedMessage.contains("\$receiver-character")) {
                if (receiverCharacter != null) {
                    formattedMessage = formattedMessage.replace("\$receiver-character", receiverCharacter.name)
                }
            }
            if (formattedMessage.contains("\$group")) {
                formattedMessage = if (name.startsWith("_pm_")) {
                    formattedMessage.replace("\$group", sender.minecraftUsername + " -> " + members.first { member -> member.id != sender.id }.minecraftUsername)
                } else {
                    formattedMessage.replace("\$group", name)
                }
            }
            receiver.sendMessage(formattedMessage)
            chatGroupService?.setLastUsedChatGroup(receiver, this)
        }
    }

}
