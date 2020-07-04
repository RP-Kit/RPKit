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
import com.rpkit.experience.bukkit.experience.RPKExperienceProvider
import com.rpkit.skills.bukkit.skills.RPKSkillPointProvider
import com.rpkit.skills.bukkit.skills.RPKSkillType
import com.rpkit.statbuilds.bukkit.RPKStatBuildsBukkit
import com.rpkit.statbuilds.bukkit.statattribute.RPKStatAttributeProvider
import com.rpkit.statbuilds.bukkit.statbuild.RPKStatBuildProvider
import org.nfunk.jep.JEP
import kotlin.math.roundToInt

class RPKSkillPointProviderImpl(private val plugin: RPKStatBuildsBukkit): RPKSkillPointProvider {
    override fun getSkillPoints(character: RPKCharacter, skillType: RPKSkillType): Int {
        val expression = plugin.config.getString("skill-points.${skillType.name}")
        val parser = JEP()
        parser.addStandardConstants()
        parser.addStandardFunctions()
        val statBuildProvider = plugin.core.serviceManager.getServiceProvider(RPKStatBuildProvider::class)
        plugin.core.serviceManager.getServiceProvider(RPKStatAttributeProvider::class)
                .statAttributes
                .forEach { statAttribute ->
                    parser.addVariable(statAttribute.name, statBuildProvider.getStatPoints(character, statAttribute).toDouble())
                }
        parser.addVariable("level", plugin.core.serviceManager.getServiceProvider(RPKExperienceProvider::class).getLevel(character).toDouble())
        parser.parseExpression(expression)
        return parser.value.roundToInt()
    }
}