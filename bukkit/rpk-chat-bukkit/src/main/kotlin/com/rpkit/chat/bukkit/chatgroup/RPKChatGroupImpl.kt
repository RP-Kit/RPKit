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

import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.database.table.ChatGroupInviteTable
import com.rpkit.chat.bukkit.database.table.ChatGroupMemberTable
import com.rpkit.chat.bukkit.event.chatgroup.*
import com.rpkit.chat.bukkit.prefix.RPKPrefixProvider
import com.rpkit.players.bukkit.player.RPKPlayer
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.bukkit.ChatColor

/**
 * Chat group implementation.
 */
class RPKChatGroupImpl(
        private val plugin: RPKChatBukkit,
        override var id: Int = 0,
        override var name: String
): RPKChatGroup {

    override val members: List<RPKPlayer>
        get() = memberMinecraftProfiles.map { minecraftProfile ->
            plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class).getPlayer(
                    plugin.server.getOfflinePlayer(minecraftProfile.minecraftUUID)
            )
        }
    override val memberMinecraftProfiles: List<RPKMinecraftProfile>
        get() = plugin.core.database.getTable(ChatGroupMemberTable::class).get(this).map(ChatGroupMember::minecraftProfile)
    override val invited: List<RPKPlayer>
        get() = invitedMinecraftProfiles.map { minecraftProfile ->
            plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class).getPlayer(
                    plugin.server.getOfflinePlayer(minecraftProfile.minecraftUUID)
            )
        }
    override val invitedMinecraftProfiles: List<RPKMinecraftProfile>
        get() = plugin.core.database.getTable(ChatGroupInviteTable::class).get(this).map(ChatGroupInvite::minecraftProfile)

    override fun addMember(player: RPKPlayer) {
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val bukkitPlayer = player.bukkitPlayer
        if (bukkitPlayer != null) {
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitPlayer)
            if (minecraftProfile != null) {
                addMember(minecraftProfile)
            }
        }
    }

    override fun addMember(minecraftProfile: RPKMinecraftProfile) {
        if (!memberMinecraftProfiles.any { memberMinecraftProfile -> memberMinecraftProfile.id == minecraftProfile.id }) {
            val event = RPKBukkitChatGroupJoinEvent(minecraftProfile, this)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return
            plugin.core.database.getTable(ChatGroupMemberTable::class).insert(
                    ChatGroupMember(
                            chatGroup = event.chatGroup,
                            minecraftProfile = event.minecraftProfile
                    )
            )
        }
    }

    override fun removeMember(player: RPKPlayer) {
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val bukkitPlayer = player.bukkitPlayer
        if (bukkitPlayer != null) {
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitPlayer)
            if (minecraftProfile != null) {
                removeMember(minecraftProfile)
            }
        }
    }

    override fun removeMember(minecraftProfile: RPKMinecraftProfile) {
        val event = RPKBukkitChatGroupLeaveEvent(
                minecraftProfile,
                this
        )
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        val chatGroupMemberTable = plugin.core.database.getTable(ChatGroupMemberTable::class)
        chatGroupMemberTable.get(event.minecraftProfile)
                .filter { member -> member.chatGroup == event.chatGroup }
                .forEach { member -> chatGroupMemberTable.delete(member) }
    }

    override fun invite(player: RPKPlayer) {
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val bukkitPlayer = player.bukkitPlayer
        if (bukkitPlayer != null) {
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitPlayer)
            if (minecraftProfile != null) {
                invite(minecraftProfile)
            }
        }
    }

    override fun invite(minecraftProfile: RPKMinecraftProfile) {
        if (!invitedMinecraftProfiles
                        .any { invitedMinecraftProfile -> invitedMinecraftProfile.id == minecraftProfile.id }) {
            val event = RPKBukkitChatGroupInviteEvent(minecraftProfile, this)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return
            plugin.core.database.getTable(ChatGroupInviteTable::class).insert(
                    ChatGroupInvite(
                            chatGroup = event.chatGroup,
                            minecraftProfile = event.minecraftProfile
                    )
            )
        }
    }

    override fun uninvite(player: RPKPlayer) {
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val bukkitPlayer = player.bukkitPlayer
        if (bukkitPlayer != null) {
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitPlayer)
            if (minecraftProfile != null) {
                uninvite(minecraftProfile)
            }
        }
    }

    override fun uninvite(minecraftProfile: RPKMinecraftProfile) {
        val event = RPKBukkitChatGroupUninviteEvent(minecraftProfile, this)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        val chatGroupInviteTable = plugin.core.database.getTable(ChatGroupInviteTable::class)
        chatGroupInviteTable.get(event.minecraftProfile)
                .filter { invite -> invite.chatGroup == event.chatGroup }
                .forEach { invite -> chatGroupInviteTable.delete(invite) }
    }

    override fun sendMessage(sender: RPKPlayer, message: String) {
        val bukkitPlayer = sender.bukkitPlayer
        if (bukkitPlayer != null) {
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitPlayer)
            if (minecraftProfile != null) {
                sendMessage(minecraftProfile, message)
            }
        }
    }

    override fun sendMessage(sender: RPKMinecraftProfile, message: String) {
        val event = RPKBukkitChatGroupMessageEvent(sender, this, message)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        val prefixProvider = plugin.core.serviceManager.getServiceProvider(RPKPrefixProvider::class)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val chatGroupProvider = plugin.core.serviceManager.getServiceProvider(RPKChatGroupProvider::class)
        val senderCharacter = characterProvider.getActiveCharacter(sender)
        memberMinecraftProfiles.forEach { receiver ->
            val receiverCharacter = characterProvider.getActiveCharacter(receiver)
            val formatString = plugin.config.getString("chat-group.format") ?: return
            var formattedMessage = ChatColor.translateAlternateColorCodes('&', formatString)
            if (formattedMessage.contains("\$message")) {
                formattedMessage = formattedMessage.replace("\$message", message)
            }
            if (formattedMessage.contains("\$sender-prefix")) {
                val profile = sender.profile
                if (profile != null) {
                    formattedMessage = formattedMessage.replace("\$sender-prefix", prefixProvider.getPrefix(profile))
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
                    formattedMessage.replace("\$group", sender.minecraftUsername + " -> " + memberMinecraftProfiles.first { member -> member.id != sender.id }.minecraftUsername)
                } else {
                    formattedMessage.replace("\$group", name)
                }
            }
            receiver.sendMessage(formattedMessage)
            chatGroupProvider.setLastUsedChatGroup(receiver, this)
        }
    }

}
