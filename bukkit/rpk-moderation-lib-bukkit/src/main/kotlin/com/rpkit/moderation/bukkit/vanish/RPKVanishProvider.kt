/*
 * Copyright 2020 Ren Binden
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

package com.rpkit.moderation.bukkit.vanish

import com.rpkit.core.service.ServiceProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile

/**
 * Provides vanish related functionality
 */
interface RPKVanishProvider: ServiceProvider {

    /**
     * Checks if the player is currently vanished.
     *
     * @param minecraftProfile The player to check
     * @return Whether the player is currently vanished
     */
    fun isVanished(minecraftProfile: RPKMinecraftProfile): Boolean

    /**
     * Sets whether a player is vanished.
     *
     * @param minecraftProfile The player
     * @param vanished Whether the player should be vanished
     */
    fun setVanished(minecraftProfile: RPKMinecraftProfile, vanished: Boolean)

    /**
     * Checks if a player is able to see another player. This takes into account whether the player is vanished or not,
     * so if a player is not vanished, this will always return true.
     *
     * @param observer The player observing
     * @param target The player being observed
     * @return Whether the observing player can currently see the target player
     */
    fun canSee(observer: RPKMinecraftProfile, target: RPKMinecraftProfile): Boolean

}