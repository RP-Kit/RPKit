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

package com.rpkit.skills.bukkit.skills

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.service.Service
import org.bukkit.inventory.ItemStack


interface RPKSkillService : Service {

    val skills: List<RPKSkill>
    fun getSkill(name: String): RPKSkill?
    fun addSkill(skill: RPKSkill)
    fun removeSkill(skill: RPKSkill)
    fun getSkillCooldown(character: RPKCharacter, skill: RPKSkill): Int
    fun setSkillCooldown(character: RPKCharacter, skill: RPKSkill, seconds: Int)
    fun getSkillBinding(character: RPKCharacter, item: ItemStack): RPKSkill?
    fun setSkillBinding(character: RPKCharacter, item: ItemStack, skill: RPKSkill?)

}