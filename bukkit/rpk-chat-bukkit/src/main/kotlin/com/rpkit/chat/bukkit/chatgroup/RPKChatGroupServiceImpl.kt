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

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.database.table.RPKChatGroupTable
import com.rpkit.chat.bukkit.database.table.RPKLastUsedChatGroupTable
import com.rpkit.chat.bukkit.event.chatgroup.RPKBukkitChatGroupCreateEvent
import com.rpkit.chat.bukkit.event.chatgroup.RPKBukkitChatGroupDeleteEvent
import com.rpkit.chat.bukkit.event.chatgroup.RPKBukkitChatGroupUpdateEvent
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import java.util.concurrent.CompletableFuture
import java.util.logging.Level

/**
 * Chat group service implementation.
 */
class RPKChatGroupServiceImpl(override val plugin: RPKChatBukkit) : RPKChatGroupService {

    override fun getChatGroup(id: RPKChatGroupId): CompletableFuture<out RPKChatGroup?> {
        return plugin.database.getTable(RPKChatGroupTable::class.java)[id]
    }

    override fun getChatGroup(name: RPKChatGroupName): CompletableFuture<out RPKChatGroup?> {
        return plugin.database.getTable(RPKChatGroupTable::class.java)[name]
    }

    override fun addChatGroup(chatGroup: RPKChatGroup): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val event = RPKBukkitChatGroupCreateEvent(chatGroup, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            plugin.database.getTable(RPKChatGroupTable::class.java).insert(event.chatGroup).join()
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to add chat group", exception)
            throw exception
        }
    }

    override fun createChatGroup(name: RPKChatGroupName): CompletableFuture<RPKChatGroup> {
        val chatGroup = RPKChatGroupImpl(plugin, null, name)
        return addChatGroup(chatGroup).thenApply { chatGroup }
    }

    override fun removeChatGroup(chatGroup: RPKChatGroup): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val event = RPKBukkitChatGroupDeleteEvent(chatGroup, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            plugin.database.getTable(RPKChatGroupTable::class.java).delete(event.chatGroup).join()
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to remove chat group", exception)
            throw exception
        }
    }

    override fun updateChatGroup(chatGroup: RPKChatGroup): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val event = RPKBukkitChatGroupUpdateEvent(chatGroup, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            plugin.database.getTable(RPKChatGroupTable::class.java).update(event.chatGroup).join()
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to update chat group", exception)
            throw exception
        }
    }

    override fun getLastUsedChatGroup(minecraftProfile: RPKMinecraftProfile): CompletableFuture<RPKChatGroup?> {
        return plugin.database.getTable(RPKLastUsedChatGroupTable::class.java).get(minecraftProfile).thenApply { it?.chatGroup }
    }

    override fun setLastUsedChatGroup(minecraftProfile: RPKMinecraftProfile, chatGroup: RPKChatGroup): CompletableFuture<Void> {
        val lastUsedChatGroupTable = plugin.database.getTable(RPKLastUsedChatGroupTable::class.java)
        return lastUsedChatGroupTable.get(minecraftProfile).thenAcceptAsync { lastUsedChatGroup ->
            if (lastUsedChatGroup != null) {
                lastUsedChatGroup.chatGroup = chatGroup
                lastUsedChatGroupTable.update(lastUsedChatGroup).join()
            } else {
                lastUsedChatGroupTable.insert(RPKLastUsedChatGroup(minecraftProfile = minecraftProfile, chatGroup = chatGroup)).join()
            }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to set last used chat group", exception)
            throw exception
        }
    }

}
