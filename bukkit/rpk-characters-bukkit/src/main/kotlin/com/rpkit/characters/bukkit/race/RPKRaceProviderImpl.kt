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

package com.rpkit.characters.bukkit.race

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.database.table.RPKRaceTable

/**
 * Race service implementation.
 */
class RPKRaceServiceImpl(override val plugin: RPKCharactersBukkit) : RPKRaceService {

    override fun getRace(id: Int): RPKRace? = plugin.database.getTable(RPKRaceTable::class)[id]

    override fun getRace(name: String): RPKRace? = plugin.database.getTable(RPKRaceTable::class)[name]

    override val races: Collection<RPKRace>
        get() = plugin.database.getTable(RPKRaceTable::class).getAll()

    override fun addRace(race: RPKRace) {
        plugin.database.getTable(RPKRaceTable::class).insert(race)
    }

    override fun removeRace(race: RPKRace) {
        plugin.database.getTable(RPKRaceTable::class).delete(race)
    }

}
