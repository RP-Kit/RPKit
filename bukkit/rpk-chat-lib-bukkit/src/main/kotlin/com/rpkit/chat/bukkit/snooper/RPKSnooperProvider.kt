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

package com.rpkit.chat.bukkit.snooper

import com.rpkit.core.service.ServiceProvider
import com.rpkit.players.bukkit.player.RPKPlayer

/**
 * Provides snooper related operations.
 */
interface RPKSnooperProvider: ServiceProvider {

    /**
     * A list of all players who are currently snooping.
     * This list is immutable, so players should be added or removed with [addSnooper] and [removeSnooper] respectively.
     */
    val snoopers: List<RPKPlayer>

    /**
     * Adds a snooper. This player is then able to see messages they would not otherwise see.
     */
    fun addSnooper(player: RPKPlayer)

    /**
     * Removes a snooper. This player then no longer receives messages outside of what they normally see.
     */
    fun removeSnooper(player: RPKPlayer)

    /**
     * Checks whether a player is snooping.
     *
     * @param player The player
     * @return Whether the player is currently snooping
     */
    fun isSnooping(player: RPKPlayer): Boolean

}
