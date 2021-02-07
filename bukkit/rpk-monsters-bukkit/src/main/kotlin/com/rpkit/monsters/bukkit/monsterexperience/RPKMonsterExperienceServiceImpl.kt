/*
 * Copyright 2021 Ren Binden
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

import com.rpkit.core.expression.RPKExpressionService
import com.rpkit.core.service.Services
import com.rpkit.monsters.bukkit.RPKMonstersBukkit
import com.rpkit.monsters.bukkit.monsterlevel.RPKMonsterLevelService
import org.bukkit.entity.LivingEntity


class RPKMonsterExperienceServiceImpl(override val plugin: RPKMonstersBukkit) : RPKMonsterExperienceService {

    override fun getExperienceFor(monster: LivingEntity): Int {
        val expressionService = Services[RPKExpressionService::class.java] ?: return 0
        val monsterLevelService = Services[RPKMonsterLevelService::class.java] ?: return 0
        val entityType = monster.type
        val expression = expressionService.createExpression(plugin.config.getString("monsters.$entityType.experience") ?: return 0)
        return expression.parseInt(mapOf(
            "level" to monsterLevelService.getMonsterLevel(monster).toDouble()
        )) ?: 0
    }

}