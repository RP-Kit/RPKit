/*
 * Copyright 2022 Ren Binden
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
import org.bukkit.configuration.ConfigurationSection

/**
 * Race service implementation.
 */
class RPKRaceServiceImpl(override val plugin: RPKCharactersBukkit) : RPKRaceService {

    override val races: Collection<RPKRace> = if (plugin.config.get("races") is ConfigurationSection) {
        plugin.config.getConfigurationSection("races")
            ?.getKeys(false)
            ?.map { raceName ->
                RPKRaceImpl(
                    RPKRaceName(raceName),
                    plugin.config.getInt("races.$raceName.min-age"),
                    plugin.config.getInt("races.$raceName.max-age")
                )
            } ?: emptyList()
    } else {
        plugin.config.getStringList("races").map { raceName ->
            RPKRaceImpl(
                RPKRaceName(raceName),
                plugin.config.getInt("characters.min-age"),
                plugin.config.getInt("characters.max-age"))
        }.also {
            plugin.config.set("races", null)
            for (race in it) {
                plugin.config.set("races.${race.name.value}.min-age", race.minAge)
                plugin.config.set("races.${race.name.value}.max-age", race.maxAge)
            }
            plugin.saveConfig()
        }
    }

    override fun getRace(name: RPKRaceName): RPKRace? = races.firstOrNull { it.name.value.equals(name.value, ignoreCase = true) }

}
