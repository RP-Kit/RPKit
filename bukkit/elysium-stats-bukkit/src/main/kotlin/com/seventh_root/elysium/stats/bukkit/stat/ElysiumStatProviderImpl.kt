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

package com.seventh_root.elysium.stats.bukkit.stat

import com.seventh_root.elysium.stats.bukkit.ElysiumStatsBukkit

/**
 * Stat provider implementation.
 */
class ElysiumStatProviderImpl(private val plugin: ElysiumStatsBukkit): ElysiumStatProvider {

    override val stats: List<ElysiumStat>
            get() = plugin.config.getConfigurationSection("stats").getKeys(false)
                            .mapIndexed { id, name ->
                                ElysiumStatImpl(id, name, plugin.config.getString("stats.$name"))
                            }

    override fun addStat(stat: ElysiumStat) {
        plugin.config.set("stats.${stat.name}", stat.script)
        plugin.saveConfig()
    }

    override fun removeStat(stat: ElysiumStat) {
        plugin.config.set("stats.${stat.name}", null)
        plugin.saveConfig()
    }

    override fun getStat(name: String): ElysiumStat? {
        return stats.filter { stat -> stat.name == name }.firstOrNull()
    }

}