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
 * Stat variable service implementation.
 */
class RPKStatVariableServiceImpl(override val plugin: RPKStatsBukkit) : RPKStatVariableService {

    override val statVariables: MutableList<RPKStatVariable> = mutableListOf()

    override fun addStatVariable(statVariable: RPKStatVariable) {
        val existingStatVariable = getStatVariable(statVariable.name)
        if (existingStatVariable != null) {
            statVariables.remove(existingStatVariable)
        }
        statVariables.add(statVariable)
    }

    override fun removeStatVariable(statVariable: RPKStatVariable) {
        statVariables.remove(statVariable)
    }

    override fun getStatVariable(name: String): RPKStatVariable? {
        return statVariables.firstOrNull { variable -> variable.name == name }
    }

}
