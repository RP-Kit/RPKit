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

package com.rpkit.skills.bukkit.skills

import com.rpkit.skills.bukkit.RPKSkillsBukkit


class RPKSkillTypeServiceImpl(override val plugin: RPKSkillsBukkit) : RPKSkillTypeService {

    private val skillTypes: MutableList<RPKSkillType> = plugin.config.getStringList("skill-types")
        .map(::RPKSkillTypeName)
        .map(::RPKSkillTypeImpl)
        .toMutableList()

    override fun getSkillTypes(): List<RPKSkillType> {
        return skillTypes
    }

    override fun getSkillType(name: RPKSkillTypeName): RPKSkillType? {
        return skillTypes.firstOrNull { it.name.value == name.value }
    }

    override fun addSkillType(skillType: RPKSkillType) {
        skillTypes.add(skillType)
        plugin.config.set("skill-types", skillTypes.map(RPKSkillType::name))
        plugin.saveConfig()
    }

    override fun removeSkillType(skillType: RPKSkillType) {
        skillTypes.remove(skillType)
        plugin.config.set("skill-types", skillTypes.map(RPKSkillType::name))
        plugin.saveConfig()
    }

}