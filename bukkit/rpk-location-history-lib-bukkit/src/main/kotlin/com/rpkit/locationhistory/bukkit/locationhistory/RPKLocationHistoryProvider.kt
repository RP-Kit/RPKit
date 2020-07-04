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

package com.rpkit.locationhistory.bukkit.locationhistory

import com.rpkit.core.service.ServiceProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import org.bukkit.Location

/**
 * Provides history of locations of players, used with commands such as /back.
 */
interface RPKLocationHistoryProvider: ServiceProvider {

    /**
     * Gets the previous location of the Minecraft profile.
     * This is used with commands such as /back.
     * If the player does not have a previous location, null is returned.
     *
     * @param minecraftProfile The Minecraft profile
     * @return The Minecraft profile's previous location
     */
    fun getPreviousLocation(minecraftProfile: RPKMinecraftProfile): Location?

    /**
     * Sets the previous location of the Minecraft profile.
     * This is used with commands such as /back.
     *
     * @param minecraftProfile The Minecraft profile
     * @param location The location to set the previous location to
     */
    fun setPreviousLocation(minecraftProfile: RPKMinecraftProfile, location: Location)

}