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

package com.seventh_root.elysium.chat.bukkit.snooper

import com.seventh_root.elysium.core.service.ServiceProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayer

/**
 * Provides snooper related operations.
 */
interface ElysiumSnooperProvider: ServiceProvider {

    /**
     * A list of all players who are currently snooping.
     * This list is immutable, so players should be added or removed with [addSnooper] and [removeSnooper] respectively.
     */
    val snoopers: List<ElysiumPlayer>

    /**
     * Adds a snooper. This player is then able to see messages they would not otherwise see.
     */
    fun addSnooper(player: ElysiumPlayer)

    /**
     * Removes a snooper. This player then no longer receives messages outside of what they normally see.
     */
    fun removeSnooper(player: ElysiumPlayer)

}
