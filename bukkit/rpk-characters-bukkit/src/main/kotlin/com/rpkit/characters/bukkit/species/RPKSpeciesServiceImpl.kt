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

package com.rpkit.characters.bukkit.species

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.race.RPKRace
import com.rpkit.characters.bukkit.race.RPKRaceName
import org.bukkit.configuration.ConfigurationSection

/**
 * Species service implementation.
 */
class RPKSpeciesServiceImpl(override val plugin: RPKCharactersBukkit) : RPKSpeciesService {

    @Deprecated("Use species", ReplaceWith("species"))
    override val races: Collection<RPKRace>
        get() = species

    override val species: Collection<RPKRace> = if (plugin.config.get("species") is ConfigurationSection) {
        plugin.config.getConfigurationSection("species")
            ?.getKeys(false)
            ?.map { speciesName ->
                RPKSpeciesImpl(
                    RPKSpeciesName(speciesName),
                    plugin.config.getInt("species.$speciesName.min-age"),
                    plugin.config.getInt("species.$speciesName.max-age")
                )
            } ?: emptyList()
    } else {
        plugin.config.getStringList("species").map { speciesName ->
            RPKSpeciesImpl(
                RPKSpeciesName(speciesName),
                plugin.config.getInt("characters.min-age"),
                plugin.config.getInt("characters.max-age")
            )
        }.also {
            plugin.config.set("species", null)
            for (species in it) {
                plugin.config.set("species.${species.name.value}.min-age", species.minAge)
                plugin.config.set("species.${species.name.value}.max-age", species.maxAge)
            }
            plugin.saveConfig()
        }
    }

    override fun getRace(name: RPKRaceName): RPKRace? = getSpecies(name)

    override fun getSpecies(name: RPKSpeciesName): RPKSpecies? = species.firstOrNull { it.name.value.equals(name.value, ignoreCase = true) }

}
