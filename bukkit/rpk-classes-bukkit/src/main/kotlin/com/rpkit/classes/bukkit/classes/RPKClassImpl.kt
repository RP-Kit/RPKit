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

package com.rpkit.classes.bukkit.classes

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.expression.RPKExpressionService
import com.rpkit.core.service.Services
import com.rpkit.skills.bukkit.skills.RPKSkillType
import com.rpkit.skills.bukkit.skills.RPKSkillTypeName
import com.rpkit.skills.bukkit.skills.RPKSkillTypeService
import com.rpkit.stats.bukkit.stat.RPKStatVariable
import java.util.concurrent.CompletableFuture


class RPKClassImpl(
    override val name: RPKClassName,
    override val maxLevel: Int,
    override val restriction: RPKClassRestriction,
    private val prerequisitesByName: Map<String, Int>,
    private val baseSkillPointsByName: Map<String, Int>,
    private val levelSkillPointsByName: Map<String, Int>,
    private val statVariableFormulae: Map<String, String>,
) : RPKClass {

    val prerequisites: Map<RPKClass, Int>
        get() = prerequisitesByName
                .map { entry ->
                    Services[RPKClassService::class.java]?.getClass(RPKClassName(entry.key)) to entry.value
                }
                .mapNotNull { (`class`, level) -> if (`class` == null) null else `class` to level }
                .toMap()

    val baseSkillPoints: Map<RPKSkillType, Int>
        get() = baseSkillPointsByName
                .map { entry ->
                    Services[RPKSkillTypeService::class.java]?.getSkillType(RPKSkillTypeName(entry.key)) to entry.value
                }
                .mapNotNull { (skillType, points) -> if (skillType == null) null else skillType to points}
                .toMap()

    val levelSkillPoints: Map<RPKSkillType, Int>
        get() = levelSkillPointsByName
                .map { entry ->
                    Services[RPKSkillTypeService::class.java]?.getSkillType(RPKSkillTypeName(entry.key)) to entry.value
                }
                .mapNotNull { (skillType, points) -> if (skillType == null) null else skillType to points }
                .toMap()

    override fun hasPrerequisites(character: RPKCharacter): CompletableFuture<Boolean> {
        val classService = Services[RPKClassService::class.java] ?: return CompletableFuture.completedFuture(false)
        return CompletableFuture.supplyAsync {
            for ((`class`, level) in prerequisites) {
                if (classService.getLevel(character, `class`).join() < level) {
                    return@supplyAsync false
                }
            }
            return@supplyAsync true
        }
    }

    override fun getSkillPoints(skillType: RPKSkillType, level: Int): Int {
        return baseSkillPoints[skillType] ?: 0 + (levelSkillPoints[skillType] ?: 0 * level)
    }

    override fun getStatVariableValue(statVariable: RPKStatVariable, level: Int): Int {
        val expressionService = Services[RPKExpressionService::class.java] ?: return 0
        val statVariableFormula = statVariableFormulae[statVariable.name.value] ?: return 0
        val expression = expressionService.createExpression(statVariableFormula)
        return expression.parseInt(mapOf("level" to level)) ?: 0
    }

}
