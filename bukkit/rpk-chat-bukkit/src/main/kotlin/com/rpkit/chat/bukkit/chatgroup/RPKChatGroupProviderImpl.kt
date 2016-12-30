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

package com.rpkit.chat.bukkit.chatgroup

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.database.table.RPKChatGroupTable
import com.rpkit.chat.bukkit.database.table.LastUsedChatGroupTable
import com.rpkit.players.bukkit.player.RPKPlayer

/**
 * Chat group provider implementation.
 */
class RPKChatGroupProviderImpl(private val plugin: RPKChatBukkit): RPKChatGroupProvider {

    override fun getChatGroup(id: Int): RPKChatGroup? {
        return plugin.core.database.getTable(RPKChatGroupTable::class)[id]
    }

    override fun getChatGroup(name: String): RPKChatGroup? {
        return plugin.core.database.getTable(RPKChatGroupTable::class).get(name)
    }

    override fun addChatGroup(chatGroup: RPKChatGroup) {
        plugin.core.database.getTable(RPKChatGroupTable::class).insert(chatGroup)
    }

    override fun removeChatGroup(chatGroup: RPKChatGroup) {
        plugin.core.database.getTable(RPKChatGroupTable::class).delete(chatGroup)
    }

    override fun updateChatGroup(chatGroup: RPKChatGroup) {
        plugin.core.database.getTable(RPKChatGroupTable::class).update(chatGroup)
    }

    override fun getLastUsedChatGroup(player: RPKPlayer): RPKChatGroup? {
        return plugin.core.database.getTable(LastUsedChatGroupTable::class).get(player)?.chatGroup?:null
    }

    override fun setLastUsedChatGroup(player: RPKPlayer, chatGroup: RPKChatGroup) {
        val lastUsedChatGroupTable = plugin.core.database.getTable(LastUsedChatGroupTable::class)
        val lastUsedChatGroup = lastUsedChatGroupTable.get(player)
        if (lastUsedChatGroup != null) {
            lastUsedChatGroup.chatGroup = chatGroup
            lastUsedChatGroupTable.update(lastUsedChatGroup)
        } else {
            lastUsedChatGroupTable.insert(LastUsedChatGroup(player = player, chatGroup = chatGroup))
        }
    }

}
