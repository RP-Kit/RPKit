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

package com.rpkit.statbuilds.bukkit.skillpoint

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.service.Services
import com.rpkit.experience.bukkit.experience.RPKExperienceService
import com.rpkit.skills.bukkit.skills.RPKSkillPointService
import com.rpkit.skills.bukkit.skills.RPKSkillType
import com.rpkit.statbuilds.bukkit.RPKStatBuildsBukkit
import com.rpkit.statbuilds.bukkit.statattribute.RPKStatAttributeService
import com.rpkit.statbuilds.bukkit.statbuild.RPKStatBuildService
import org.nfunk.jep.JEP
import kotlin.math.roundToInt

class RPKSkillPointServiceImpl(override val plugin: RPKStatBuildsBukkit) : RPKSkillPointService {
    override fun getSkillPoints(character: RPKCharacter, skillType: RPKSkillType): Int {
        val expression = plugin.config.getString("skill-points.${skillType.name}")
        val parser = JEP()
        parser.addStandardConstants()
        parser.addStandardFunctions()
        val statBuildService = Services[RPKStatBuildService::class] ?: return 0
        val statAttributeService = Services[RPKStatAttributeService::class] ?: return 0
        statAttributeService
                .statAttributes
                .forEach { statAttribute ->
                    parser.addVariable(statAttribute.name, statBuildService.getStatPoints(character, statAttribute).toDouble())
                }
        val experienceService = Services[RPKExperienceService::class] ?: return 0
        parser.addVariable("level", experienceService.getLevel(character).toDouble())
        parser.parseExpression(expression)
        return parser.value.roundToInt()
    }
}