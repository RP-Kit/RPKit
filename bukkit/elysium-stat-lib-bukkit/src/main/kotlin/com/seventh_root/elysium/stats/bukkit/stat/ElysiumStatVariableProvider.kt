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
 * Provides stat variable related operations.
 */
interface ElysiumStatVariableProvider : ServiceProvider {

    /**
     * A list of stat variables managed by this stat variable provider.
     * The list is immutable, to add and remove stat variables, [addStatVariable] and [removeStatVariable] should be
     * used.
     */
    val statVariables: List<ElysiumStatVariable>

    /**
     * Adds a stat variable to be managed by this stat variable provider.
     *
     * @param statVariable The stat variable to add
     */
    fun addStatVariable(statVariable: ElysiumStatVariable)

    /**
     * Removes a stat variable from being managed by this stat variable provider.
     *
     * @param statVariable The stat variable to remove
     */
    fun removeStatVariable(statVariable: ElysiumStatVariable)

    /**
     * Gets a stat variable by name.
     * If there is no stat variable with the given name, null is returned.
     *
     * @param name The name of the stat variable
     * @return The stat variable, or null if there is no stat variable with the given name
     */
    fun getStatVariable(name: String): ElysiumStatVariable?
}