/*
 * Copyright 2016 Ross Binden
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

package com.seventh_root.elysium.chat.bukkit.chatgroup

import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacterProvider
import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit
import com.seventh_root.elysium.chat.bukkit.database.table.ChatGroupInviteTable
import com.seventh_root.elysium.chat.bukkit.database.table.ChatGroupMemberTable
import com.seventh_root.elysium.chat.bukkit.prefix.ElysiumPrefixProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayer
import org.bukkit.ChatColor

/**
 * Chat group implementation.
 */
class ElysiumChatGroupImpl(
        private val plugin: ElysiumChatBukkit,
        override var id: Int = 0,
        override var name: String
): ElysiumChatGroup {

    override val members: List<ElysiumPlayer>
        get() = plugin.core.database.getTable(ChatGroupMemberTable::class).get(this).map { chatGroupMember -> chatGroupMember.player }
    override val invited: List<ElysiumPlayer>
        get() = plugin.core.database.getTable(ChatGroupInviteTable::class).get(this).map { chatGroupInvite -> chatGroupInvite.player }

    override fun addMember(player: ElysiumPlayer) {
        if (!members.contains(player)) {
            plugin.core.database.getTable(ChatGroupMemberTable::class).insert(ChatGroupMember(chatGroup = this, player = player))
        }
    }

    override fun removeMember(player: ElysiumPlayer) {
        val chatGroupMemberTable = plugin.core.database.getTable(ChatGroupMemberTable::class)
        chatGroupMemberTable.get(player).filter { member -> member.chatGroup == this }.forEach { member -> chatGroupMemberTable.delete(member) }
    }

    override fun invite(player: ElysiumPlayer) {
        if (!invited.contains(player)) {
            plugin.core.database.getTable(ChatGroupInviteTable::class).insert(ChatGroupInvite(chatGroup = this, player = player))
        }
    }

    override fun uninvite(player: ElysiumPlayer) {
        val chatGroupInviteTable = plugin.core.database.getTable(ChatGroupInviteTable::class)
        chatGroupInviteTable.get(player).filter { invite -> invite.chatGroup == this }.forEach { invite -> chatGroupInviteTable.delete(invite) }
    }

    override fun sendMessage(sender: ElysiumPlayer, message: String) {
        val prefixProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPrefixProvider::class)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
        val chatGroupProvider = plugin.core.serviceManager.getServiceProvider(ElysiumChatGroupProvider::class)
        val senderCharacter = characterProvider.getActiveCharacter(sender)
        members.forEach { receiver ->
            val receiverCharacter = characterProvider.getActiveCharacter(receiver)
            val formatString = plugin.config.getString("chat-group.format")
            var formattedMessage = ChatColor.translateAlternateColorCodes('&', formatString)
            if (formattedMessage.contains("\$message")) {
                formattedMessage = formattedMessage.replace("\$message", message)
            }
            if (formattedMessage.contains("\$sender-prefix")) {
                formattedMessage = formattedMessage.replace("\$sender-prefix", prefixProvider.getPrefix(sender))
            }
            if (formattedMessage.contains("\$sender-player")) {
                formattedMessage = formattedMessage.replace("\$sender-player", sender.name)
            }
            if (formattedMessage.contains("\$sender-character")) {
                if (senderCharacter != null) {
                    formattedMessage = formattedMessage.replace("\$sender-character", senderCharacter.name)
                }
            }
            if (formattedMessage.contains("\$receiver-prefix")) {
                formattedMessage = formattedMessage.replace("\$receiver-prefix", prefixProvider.getPrefix(receiver))
            }
            if (formattedMessage.contains("\$receiver-player")) {
                formattedMessage = formattedMessage.replace("\$receiver-player", receiver.name)
            }
            if (formattedMessage.contains("\$receiver-character")) {
                if (receiverCharacter != null) {
                    formattedMessage = formattedMessage.replace("\$receiver-character", receiverCharacter.name)
                }
            }
            if (formattedMessage.contains("\$group")) {
                if (name.startsWith("_pm_")) {
                    formattedMessage = formattedMessage.replace("\$group", sender.name + " -> " + members.filter { member -> member != sender }.first().name)
                } else {
                    formattedMessage = formattedMessage.replace("\$group", name)
                }
            }
            receiver.bukkitPlayer?.player?.sendMessage(formattedMessage)
            chatGroupProvider.setLastUsedChatGroup(receiver, this)
        }
    }

}
