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

package com.rpkit.statbuilds.bukkit.skillpoint

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.expression.RPKExpressionService
import com.rpkit.core.service.Services
import com.rpkit.experience.bukkit.experience.RPKExperienceService
import com.rpkit.skills.bukkit.skills.RPKSkillPointService
import com.rpkit.skills.bukkit.skills.RPKSkillType
import com.rpkit.statbuilds.bukkit.RPKStatBuildsBukkit
import com.rpkit.statbuilds.bukkit.statattribute.RPKStatAttributeService
import com.rpkit.statbuilds.bukkit.statbuild.RPKStatBuildService

class RPKSkillPointServiceImpl(override val plugin: RPKStatBuildsBukkit) : RPKSkillPointService {
    override fun getSkillPoints(character: RPKCharacter, skillType: RPKSkillType): Int {
        val statBuildService = Services[RPKStatBuildService::class.java] ?: return 0
        val statAttributeService = Services[RPKStatAttributeService::class.java] ?: return 0
        val experienceService = Services[RPKExperienceService::class.java] ?: return 0
        val expressionService = Services[RPKExpressionService::class.java] ?: return 0
        val expression = expressionService.createExpression(plugin.config.getString("skill-points.${skillType.name}") ?: return 0)
        return expression.parseInt(mapOf(
            "level" to experienceService.getLevel(character).toDouble(),
            *statAttributeService.statAttributes.map { statAttribute ->
                statAttribute.name.value to statBuildService.getStatPoints(character, statAttribute).toDouble()
            }.toTypedArray()
        )) ?: 0
    }
}