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

package com.rpkit.classes.bukkit.skillpoint

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.classes.bukkit.RPKClassesBukkit
import com.rpkit.classes.bukkit.classes.RPKClassService
import com.rpkit.core.service.Services
import com.rpkit.skills.bukkit.skills.RPKSkillPointService
import com.rpkit.skills.bukkit.skills.RPKSkillType


class RPKSkillPointServiceImpl(override val plugin: RPKClassesBukkit) : RPKSkillPointService {

    override fun getSkillPoints(character: RPKCharacter, skillType: RPKSkillType): Int {
        val classService = Services[RPKClassService::class.java] ?: return 0
        val `class` = classService.getClass(character)
        if (`class` != null) {
            return `class`.getSkillPoints(skillType, classService.getLevel(character, `class`))
        }
        return 0
    }

}