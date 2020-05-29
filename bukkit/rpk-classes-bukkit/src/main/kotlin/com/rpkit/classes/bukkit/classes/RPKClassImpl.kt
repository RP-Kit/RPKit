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

package com.rpkit.classes.bukkit.classes

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.classes.bukkit.RPKClassesBukkit
import com.rpkit.skills.bukkit.skills.RPKSkillType
import com.rpkit.skills.bukkit.skills.RPKSkillTypeProvider
import com.rpkit.stats.bukkit.stat.RPKStatVariable
import org.nfunk.jep.JEP
import kotlin.math.roundToInt


class RPKClassImpl(
        private val plugin: RPKClassesBukkit,
        override val name: String,
        override val maxLevel: Int,
        private val prerequisitesByName: Map<String, Int>,
        private val baseSkillPointsByName: Map<String, Int>,
        private val levelSkillPointsByName: Map<String, Int>,
        private val statVariableFormulae: Map<String, String>
) : RPKClass {

    val prerequisites: Map<RPKClass, Int>
        get() = prerequisitesByName
                .map { entry ->
                    Pair(
                            plugin.core.serviceManager.getServiceProvider(RPKClassProvider::class).getClass(entry.key)!!,
                            entry.value
                    )
                }
                .toMap()

    val baseSkillPoints: Map<RPKSkillType, Int>
        get() = baseSkillPointsByName
                .map { entry ->
                    Pair(
                            plugin.core.serviceManager.getServiceProvider(RPKSkillTypeProvider::class).getSkillType(entry.key)!!,
                            entry.value
                    )
                }
                .toMap()

    val levelSkillPoints: Map<RPKSkillType, Int>
        get() = levelSkillPointsByName
                .map { entry ->
                    Pair(
                            plugin.core.serviceManager.getServiceProvider(RPKSkillTypeProvider::class).getSkillType(entry.key)!!,
                            entry.value
                    )
                }
                .toMap()

    override fun hasPrerequisites(character: RPKCharacter): Boolean {
        val classProvider = plugin.core.serviceManager.getServiceProvider(RPKClassProvider::class)
        for ((`class`, level) in prerequisites) {
            if (classProvider.getLevel(character, `class`) < level) {
                return false
            }
        }
        return true
    }

    override fun getSkillPoints(skillType: RPKSkillType, level: Int): Int {
        return baseSkillPoints[skillType]?:0 + (levelSkillPoints[skillType]?:0 * level)
    }

    override fun getStatVariableValue(statVariable: RPKStatVariable, level: Int): Int {
        val parser = JEP()
        parser.addStandardConstants()
        parser.addStandardFunctions()
        parser.addVariable("level", level.toDouble())
        parser.parseExpression(statVariableFormulae[statVariable.name])
        return parser.value.roundToInt()
    }

}