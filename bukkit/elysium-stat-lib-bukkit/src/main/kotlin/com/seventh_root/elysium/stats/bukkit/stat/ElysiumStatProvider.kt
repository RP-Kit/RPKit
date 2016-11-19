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

import com.seventh_root.elysium.core.service.ServiceProvider

/**
 * Provides stat related operations.
 */
interface ElysiumStatProvider: ServiceProvider {

    /**
     * A list containing all stats managed by this stat provider.
     * The list is immutable, in order to add and remove stats, [addStat] and [removeStat] should be used.
     */
    val stats: List<ElysiumStat>

    /**
     * Adds a stat to be managed by this stat provider.
     *
     * @param stat The stat to add
     */
    fun addStat(stat: ElysiumStat)

    /**
     * Removes a stat from being managed by this stat provider.
     *
     * @param stat The stat to remove
     */
    fun removeStat(stat: ElysiumStat)

    /**
     * Gets a stat by name.
     * If there is no stat with the given name, null is returned.
     *
     * @param name The name of the stat
     * @return The stat, or null if there is no stat with the given name
     */
    fun getStat(name: String): ElysiumStat?
}
