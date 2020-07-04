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
import com.rpkit.classes.bukkit.classes.RPKClassProvider
import com.rpkit.skills.bukkit.skills.RPKSkillPointProvider
import com.rpkit.skills.bukkit.skills.RPKSkillType


class RPKSkillPointProviderImpl(private val plugin: RPKClassesBukkit): RPKSkillPointProvider {

    override fun getSkillPoints(character: RPKCharacter, skillType: RPKSkillType): Int {
        val classProvider = plugin.core.serviceManager.getServiceProvider(RPKClassProvider::class)
        val `class` = classProvider.getClass(character)
        if (`class` != null) {
            return `class`.getSkillPoints(skillType, classProvider.getLevel(character, `class`))
        }
        return 0
    }

}