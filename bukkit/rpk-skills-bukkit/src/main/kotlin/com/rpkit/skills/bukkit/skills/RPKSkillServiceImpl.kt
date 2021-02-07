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

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.skills.bukkit.RPKSkillsBukkit
import com.rpkit.skills.bukkit.database.table.RPKSkillBindingTable
import com.rpkit.skills.bukkit.database.table.RPKSkillCooldownTable
import org.bukkit.inventory.ItemStack
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit.SECONDS
import kotlin.math.max


class RPKSkillServiceImpl(override val plugin: RPKSkillsBukkit) : RPKSkillService {

    override val skills: MutableList<RPKSkill> = mutableListOf()

    override fun getSkill(name: RPKSkillName): RPKSkill? {
        return skills.firstOrNull { it.name.value.equals(name.value, ignoreCase = true) }
    }

    override fun addSkill(skill: RPKSkill) {
        skills.add(skill)
    }

    override fun removeSkill(skill: RPKSkill) {
        skills.remove(skill)
    }

    override fun getSkillCooldown(character: RPKCharacter, skill: RPKSkill): Int {
        val skillCooldownTable = plugin.database.getTable(RPKSkillCooldownTable::class.java)
        val skillCooldown = skillCooldownTable.get(character, skill) ?: return 0
        return max(0L, SECONDS.between(skillCooldown.cooldownTimestamp, LocalDateTime.now())).toInt()
    }

    override fun setSkillCooldown(character: RPKCharacter, skill: RPKSkill, seconds: Int) {
        val skillCooldownTable = plugin.database.getTable(RPKSkillCooldownTable::class.java)
        var skillCooldown = skillCooldownTable.get(character, skill)
        if (skillCooldown == null) {
            skillCooldown = RPKSkillCooldown(
                    character = character,
                    skill = skill,
                    cooldownTimestamp = LocalDateTime.now().plus(seconds.toLong(), SECONDS)
            )
            skillCooldownTable.insert(skillCooldown)
        } else {
            skillCooldown.cooldownTimestamp = LocalDateTime.now().plus(seconds.toLong(), SECONDS)
            skillCooldownTable.update(skillCooldown)
        }
    }

    override fun getSkillBinding(character: RPKCharacter, item: ItemStack): RPKSkill? {
        val skillBindingTable = plugin.database.getTable(RPKSkillBindingTable::class.java)
        val skillBindings = skillBindingTable.get(character)
        return skillBindings.firstOrNull { skillBinding -> skillBinding.item.isSimilar(item) }?.skill
    }

    override fun setSkillBinding(character: RPKCharacter, item: ItemStack, skill: RPKSkill?) {
        val skillBindingTable = plugin.database.getTable(RPKSkillBindingTable::class.java)
        val skillBindings = skillBindingTable.get(character)
        if (skill != null) {
            if (skillBindings.none { skillBinding -> skillBinding.item.isSimilar(item) }) {
                skillBindingTable.insert(RPKSkillBinding(
                        character = character,
                        item = item,
                        skill = skill
                ))
            }
        } else {
            val skillBinding = skillBindings.firstOrNull { skillBinding -> skillBinding.item.isSimilar(item) }
            if (skillBinding != null) {
                skillBindingTable.delete(skillBinding)
            }
        }
    }

}