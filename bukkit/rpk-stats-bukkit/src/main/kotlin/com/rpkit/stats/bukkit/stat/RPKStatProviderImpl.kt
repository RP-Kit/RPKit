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

package com.rpkit.stats.bukkit.stat

import com.rpkit.stats.bukkit.RPKStatsBukkit

/**
 * Stat provider implementation.
 */
class RPKStatProviderImpl(private val plugin: RPKStatsBukkit): RPKStatProvider {

    override val stats: List<RPKStat>
            get() = plugin.config.getConfigurationSection("stats")
                            ?.getKeys(false)
                            ?.mapIndexed { id, name ->
                                RPKStatImpl(id, name, plugin.config.getString("stats.$name") ?: "0")
                            }
                    ?: emptyList()

    override fun addStat(stat: RPKStat) {
        plugin.config.set("stats.${stat.name}", stat.script)
        plugin.saveConfig()
    }

    override fun removeStat(stat: RPKStat) {
        plugin.config.set("stats.${stat.name}", null)
        plugin.saveConfig()
    }

    override fun getStat(name: String): RPKStat? {
        return stats.firstOrNull { stat -> stat.name == name }
    }

}