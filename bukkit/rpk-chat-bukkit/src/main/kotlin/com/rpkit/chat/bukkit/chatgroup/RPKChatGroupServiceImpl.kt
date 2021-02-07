/*
 * Copyright 2021 Ren Binden
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
import com.rpkit.chat.bukkit.database.table.RPKLastUsedChatGroupTable
import com.rpkit.chat.bukkit.event.chatgroup.RPKBukkitChatGroupCreateEvent
import com.rpkit.chat.bukkit.event.chatgroup.RPKBukkitChatGroupDeleteEvent
import com.rpkit.chat.bukkit.event.chatgroup.RPKBukkitChatGroupUpdateEvent
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile

/**
 * Chat group service implementation.
 */
class RPKChatGroupServiceImpl(override val plugin: RPKChatBukkit) : RPKChatGroupService {

    override fun getChatGroup(id: RPKChatGroupId): RPKChatGroup? {
        return plugin.database.getTable(RPKChatGroupTable::class.java)[id]
    }

    override fun getChatGroup(name: RPKChatGroupName): RPKChatGroup? {
        return plugin.database.getTable(RPKChatGroupTable::class.java)[name]
    }

    override fun addChatGroup(chatGroup: RPKChatGroup) {
        val event = RPKBukkitChatGroupCreateEvent(chatGroup)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.database.getTable(RPKChatGroupTable::class.java).insert(event.chatGroup)
    }

    override fun createChatGroup(name: RPKChatGroupName): RPKChatGroup {
        val chatGroup = RPKChatGroupImpl(plugin, null, name)
        addChatGroup(chatGroup)
        return chatGroup
    }

    override fun removeChatGroup(chatGroup: RPKChatGroup) {
        val event = RPKBukkitChatGroupDeleteEvent(chatGroup)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.database.getTable(RPKChatGroupTable::class.java).delete(event.chatGroup)
    }

    override fun updateChatGroup(chatGroup: RPKChatGroup) {
        val event = RPKBukkitChatGroupUpdateEvent(chatGroup)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.database.getTable(RPKChatGroupTable::class.java).update(event.chatGroup)
    }

    override fun getLastUsedChatGroup(minecraftProfile: RPKMinecraftProfile): RPKChatGroup? {
        return plugin.database.getTable(RPKLastUsedChatGroupTable::class.java).get(minecraftProfile)?.chatGroup
    }

    override fun setLastUsedChatGroup(minecraftProfile: RPKMinecraftProfile, chatGroup: RPKChatGroup) {
        val lastUsedChatGroupTable = plugin.database.getTable(RPKLastUsedChatGroupTable::class.java)
        val lastUsedChatGroup = lastUsedChatGroupTable.get(minecraftProfile)
        if (lastUsedChatGroup != null) {
            lastUsedChatGroup.chatGroup = chatGroup
            lastUsedChatGroupTable.update(lastUsedChatGroup)
        } else {
            lastUsedChatGroupTable.insert(RPKLastUsedChatGroup(minecraftProfile = minecraftProfile, chatGroup = chatGroup))
        }
    }

}
