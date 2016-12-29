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

import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter
import javax.script.ScriptContext.ENGINE_SCOPE
import javax.script.ScriptEngineManager

/**
 * Stat implementation.
 */
class ElysiumStatImpl(
        override var id: Int = 0,
        override val name: String,
        override val script: String
) : ElysiumStat {
    override fun get(character: ElysiumCharacter, variables: List<ElysiumStatVariable>): Int {
        val engineManager = ScriptEngineManager()
        val engine = engineManager.getEngineByName("nashorn")
        val bindings = engine.createBindings()
        for (variable in variables) {
            bindings[variable.name] = variable.get(character)
        }
        engine.setBindings(bindings, ENGINE_SCOPE)
        return Math.round(engine.eval(script) as Double).toInt()
    }
}