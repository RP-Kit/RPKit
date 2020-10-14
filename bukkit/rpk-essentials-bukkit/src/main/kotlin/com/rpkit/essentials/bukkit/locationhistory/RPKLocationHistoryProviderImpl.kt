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

package com.rpkit.essentials.bukkit.locationhistory

import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.essentials.bukkit.database.table.RPKPreviousLocationTable
import com.rpkit.locationhistory.bukkit.locationhistory.RPKLocationHistoryService
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import org.bukkit.Location


class RPKLocationHistoryServiceImpl(override val plugin: RPKEssentialsBukkit) : RPKLocationHistoryService {

    override fun getPreviousLocation(minecraftProfile: RPKMinecraftProfile): Location? {
        return plugin.database.getTable(RPKPreviousLocationTable::class).get(minecraftProfile)?.location
    }

    override fun setPreviousLocation(minecraftProfile: RPKMinecraftProfile, location: Location) {
        val previousLocationTable = plugin.database.getTable(RPKPreviousLocationTable::class)
        var previousLocation = previousLocationTable[minecraftProfile]
        if (previousLocation != null) {
            previousLocation.location = location
            previousLocationTable.update(previousLocation)
        } else {
            previousLocation = RPKPreviousLocation(minecraftProfile = minecraftProfile, location = location)
            previousLocationTable.insert(previousLocation)
        }
    }

}