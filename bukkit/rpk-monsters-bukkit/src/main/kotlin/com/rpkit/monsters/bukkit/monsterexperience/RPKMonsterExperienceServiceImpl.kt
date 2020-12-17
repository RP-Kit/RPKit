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

package com.rpkit.monsters.bukkit.monsterexperience

import com.rpkit.core.service.Services
import com.rpkit.monsters.bukkit.RPKMonstersBukkit
import com.rpkit.monsters.bukkit.jep.CeilFunction
import com.rpkit.monsters.bukkit.jep.FloorFunction
import com.rpkit.monsters.bukkit.monsterlevel.RPKMonsterLevelService
import org.bukkit.entity.LivingEntity
import org.nfunk.jep.JEP
import kotlin.math.roundToInt


class RPKMonsterExperienceServiceImpl(override val plugin: RPKMonstersBukkit) : RPKMonsterExperienceService {

    override fun getExperienceFor(monster: LivingEntity): Int {
        val entityType = monster.type
        val expression = plugin.config.getString("monsters.$entityType.experience")
        val parser = JEP()
        parser.addStandardConstants()
        parser.addStandardFunctions()
        parser.addFunction("ceil", CeilFunction())
        parser.addFunction("floor", FloorFunction())
        val monsterLevelService = Services[RPKMonsterLevelService::class.java] ?: return 0
        parser.addVariable("level", monsterLevelService.getMonsterLevel(monster).toDouble())
        parser.parseExpression(expression)
        return parser.value.roundToInt()
    }

}