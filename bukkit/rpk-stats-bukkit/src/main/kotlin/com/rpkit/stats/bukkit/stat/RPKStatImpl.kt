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

import com.rpkit.characters.bukkit.character.RPKCharacter
import org.nfunk.jep.JEP
import kotlin.math.roundToInt

/**
 * Stat implementation.
 */
class RPKStatImpl(
        override var id: Int = 0,
        override val name: String,
        override val script: String
) : RPKStat {
    override fun get(character: RPKCharacter, variables: List<RPKStatVariable>): Int {
        val parser = JEP()
        parser.addStandardConstants()
        parser.addStandardFunctions()
        for (variable in variables) {
            val value = variable.get(character)
            if (value != null) {
                parser.addVariableAsObject(variable.name, value)
            }
        }
        parser.parseExpression(script)
        return parser.value.roundToInt()
    }
}