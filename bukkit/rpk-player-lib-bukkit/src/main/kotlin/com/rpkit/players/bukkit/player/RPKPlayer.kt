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

package com.rpkit.players.bukkit.player

import com.rpkit.core.database.Entity
import org.bukkit.OfflinePlayer

/**
 * Represents a player
 */
@Deprecated("Old players API. Please move to new profiles APIs.",
        replaceWith = ReplaceWith("RPKProfile", "com.rpkit.players.bukkit.profile.RPKProfile"))
interface RPKPlayer: Entity {

    /**
     * The name of the player.
     */
    var name: String

    /**
     * The Bukkit player instance.
     * May be null if no Bukkit player is currently linked (e.g. if the player has just spoken through IRC)
     */
    var bukkitPlayer: OfflinePlayer?

    /**
     * The IRC nickname.
     * May be null if no IRC user is currently linked (e.g. if the player has only played in Minecraft)
     */
    var ircNick: String?

    /**
     * The last known IP of the player.
     * May be null if no IP is known (e.g. if the player has just spoken through IRC)
     */
    var lastKnownIP: String?

}