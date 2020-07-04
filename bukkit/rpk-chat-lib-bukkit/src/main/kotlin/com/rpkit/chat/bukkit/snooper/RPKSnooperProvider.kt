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

package com.rpkit.chat.bukkit.snooper

import com.rpkit.core.service.ServiceProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile

/**
 * Provides snooper related operations.
 */
interface RPKSnooperProvider: ServiceProvider {

    /**
     * A list of all Minecraft profiles who are currently snooping.
     * THis list is immutable, so Minecraft profiles should be added or removed with [addSnooper] and [removeSnooper] respectively.
     */
    val snoopers: List<RPKMinecraftProfile>

    /**
     * Adds a snooper. This Minecraft profile is then abel to see messages they would not otherwise see.
     *
     * @param minecraftProfile The Minecraft profile to enable snooping for
     */
    fun addSnooper(minecraftProfile: RPKMinecraftProfile)

    /**
     * Removes a snooper. This Minecraft profile then no longer receives messages outside of what they normally see.
     *
     * @param minecraftProfile THe Minecraft profile to disable snooping for
     */
    fun removeSnooper(minecraftProfile: RPKMinecraftProfile)

    /**
     * Checks whether a Minecraft profile is snooping.
     *
     * @param minecraftProfile The Minecraft profile
     * @return Whether the Minecraft profile is snooping
     */
    fun isSnooping(minecraftProfile: RPKMinecraftProfile): Boolean

}
