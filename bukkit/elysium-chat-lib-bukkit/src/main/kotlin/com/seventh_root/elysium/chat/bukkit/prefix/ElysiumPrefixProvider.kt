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

package com.seventh_root.elysium.chat.bukkit.prefix

import com.seventh_root.elysium.core.service.ServiceProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayer

/**
 * Provides prefix related operations.
 */
interface ElysiumPrefixProvider: ServiceProvider {

    /**
     * A list of prefixes currently managed by this prefix provider.
     * The list is immutable, so to add or remove prefixes, [addPrefix] or [removePrefix] should be used.
     */
    val prefixes: List<ElysiumPrefix>

    /**
     * Constructs a player's prefix from all of the prefixes they have.
     *
     * @param player The player
     * @return The player's full prefix
     */
    fun getPrefix(player: ElysiumPlayer): String

    /**
     * Gets a prefix by name.
     * If there is no prefix with the given name, null is returned.
     *
     * @param name The name of the prefix
     * @return The prefix, or null if no prefix is found with the given name
     */
    fun getPrefix(name: String): ElysiumPrefix?

    /**
     * Adds a prefix to be tracked by this prefix provider.
     *
     * @param prefix The prefix to add
     */
    fun addPrefix(prefix: ElysiumPrefix)

    /**
     * Updates a prefix in data storage.
     *
     * @param prefix The prefix
     */
    fun updatePrefix(prefix: ElysiumPrefix)

    /**
     * Removes a prefix from being tracked by this prefix provider.
     *
     * @param prefix The prefix
     */
    fun removePrefix(prefix: ElysiumPrefix)

}