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

package com.seventh_root.elysium.players.bukkit.player

import com.seventh_root.elysium.core.service.ServiceProvider
import org.bukkit.OfflinePlayer
import org.pircbotx.User
import java.net.InetAddress

/**
 * Provides player related operations.
 */
interface ElysiumPlayerProvider: ServiceProvider {

    /**
     * Gets a player by ID.
     * If there is no player with the given ID, null is returned.
     *
     * @param id The ID of the player
     * @return The player, or null if there is no player with the given ID
     */
    fun getPlayer(id: Int): ElysiumPlayer?

    /**
     * Gets a player by name.
     * If there is no player with the given name, null is returned.
     *
     * @param name The name of the player
     * @return The player, or null if there is no player with the given name
     */
    fun getPlayer(name: String): ElysiumPlayer?

    /**
     * Gets a player by the Bukkit player instance.
     * If the Bukkit player does not currently have a player instance, one is created.
     *
     * @param bukkitPlayer The Bukkit player instance
     * @return The player
     */
    fun getPlayer(bukkitPlayer: OfflinePlayer): ElysiumPlayer

    /**
     * Gets a player by the IRC user.
     * If the IRC user does not currently have a player instance, one is created.
     *
     * @param ircUser The IRC user
     * @return The player
     */
    fun getPlayer(ircUser: User): ElysiumPlayer

    /**
     * Gets a player by the last known IP.
     * If there is no player that last used the IP, null is returned.
     *
     * @param lastKnownIP The last known IP address of the player
     * @return The player, or null if no player last used the IP address given
     */
    fun getPlayer(lastKnownIP: InetAddress): ElysiumPlayer?

    /**
     * Adds a player to be tracked by this player provider.
     *
     * @param player The player to add
     */
    fun addPlayer(player: ElysiumPlayer)

    /**
     * Updates a player in data storage.
     *
     * @param player The player to update
     */
    fun updatePlayer(player: ElysiumPlayer)

    /**
     * Removes a player from being tracked by this player provider.
     *
     * @param player The player to remove
     */
    fun removePlayer(player: ElysiumPlayer)

}