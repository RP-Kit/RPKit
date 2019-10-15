/*
 * Copyright 2019 Ren Binden
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

package com.rpkit.monsters.bukkit.monsterexperience

import com.rpkit.monsters.bukkit.RPKMonstersBukkit
import com.rpkit.monsters.bukkit.monsterlevel.RPKMonsterLevelProvider
import org.bukkit.entity.LivingEntity
import javax.script.ScriptContext
import javax.script.ScriptEngineManager
import kotlin.math.roundToInt


class RPKMonsterExperienceProviderImpl(private val plugin: RPKMonstersBukkit): RPKMonsterExperienceProvider {

    override fun getExperienceFor(monster: LivingEntity): Int {
        val entityType = monster.type
        val script = plugin.config.getString("monsters.$entityType.experience")
        val engineManager = ScriptEngineManager()
        val engine = engineManager.getEngineByName("nashorn")
        val bindings = engine.createBindings()
        val monsterLevelProvider = plugin.core.serviceManager.getServiceProvider(RPKMonsterLevelProvider::class)
        bindings["level"] = monsterLevelProvider.getMonsterLevel(monster)
        engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE)
        return (engine.eval(script) as Number).toDouble().roundToInt()
    }

}