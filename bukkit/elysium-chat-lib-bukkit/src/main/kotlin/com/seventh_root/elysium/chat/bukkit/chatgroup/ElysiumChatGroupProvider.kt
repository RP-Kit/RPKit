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

import com.seventh_root.elysium.core.service.ServiceProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayer

/**
 * Provides chat group related operations.
 */
interface ElysiumChatGroupProvider: ServiceProvider {

    /**
     * Gets a chat group by ID.
     * If there is no chat group with the given ID, null is returned.
     *
     * @param id The ID of the chat group
     * @return The chat group, or null if no chat group is found with the given ID
     */
    fun getChatGroup(id: Int): ElysiumChatGroup?

    /**
     * Gets a chat group by name.
     * If there is no chat group with the given name, null is returned.
     *
     * @param name The name of the chat group
     * @return The chat group, or null if no chat group is found with the given ID
     */
    fun getChatGroup(name: String): ElysiumChatGroup?

    /**
     * Adds a chat group to be tracked by this chat group provider.
     *
     * @param chatGroup The chat group to add
     */
    fun addChatGroup(chatGroup: ElysiumChatGroup)

    /**
     * Removes a chat group from being tracked by this chat group provider.
     *
     * @param chatGroup The chat group to remove
     */
    fun removeChatGroup(chatGroup: ElysiumChatGroup)

    /**
     * Updates a chat group in data storage.
     *
     * @param chatGroup The chat group to update
     */
    fun updateChatGroup(chatGroup: ElysiumChatGroup)

    /**
     * Gets a player's last used chat group.
     * If the player has not used a chat group, null is returned.
     *
     * @param player The player
     * @return The last chat group used by the player, or null if the player has not used a chat group
     */
    fun getLastUsedChatGroup(player: ElysiumPlayer): ElysiumChatGroup?

    /**
     * Sets a player's last used chat group.
     *
     * @param player The player
     * @param chatGroup The chat group to set
     */
    fun setLastUsedChatGroup(player: ElysiumPlayer, chatGroup: ElysiumChatGroup)
}