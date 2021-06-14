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
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max


class RPKSkillServiceImpl(override val plugin: RPKSkillsBukkit) : RPKSkillService {

    override val skills: MutableList<RPKSkill> = mutableListOf()

    private val characterSkillCooldowns = ConcurrentHashMap<Int, Map<RPKSkill, LocalDateTime>>()
    private val characterSkillBindings = ConcurrentHashMap<Int, Map<ItemStack, RPKSkill>>()

    override fun getSkill(name: RPKSkillName): RPKSkill? {
        return skills.firstOrNull { it.name.value.equals(name.value, ignoreCase = true) }
    }

    override fun addSkill(skill: RPKSkill) {
        skills.add(skill)
    }

    override fun removeSkill(skill: RPKSkill) {
        skills.remove(skill)
    }

    override fun getSkillCooldown(character: RPKCharacter, skill: RPKSkill): CompletableFuture<Int> {
        val preloadedSkillCooldown = getPreloadedSkillCooldown(character, skill)
        if (preloadedSkillCooldown != null) return CompletableFuture.completedFuture(preloadedSkillCooldown)
        return CompletableFuture.supplyAsync {
            val skillCooldownTable = plugin.database.getTable(RPKSkillCooldownTable::class.java)
            val skillCooldown = skillCooldownTable[character, skill].join() ?: return@supplyAsync 0
            return@supplyAsync max(0L, SECONDS.between(LocalDateTime.now(), skillCooldown.cooldownTimestamp)).toInt()
        }
    }

    override fun setSkillCooldown(character: RPKCharacter, skill: RPKSkill, seconds: Int): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val skillCooldownTable = plugin.database.getTable(RPKSkillCooldownTable::class.java)
            var skillCooldown = skillCooldownTable[character, skill].join()
            if (skillCooldown == null) {
                skillCooldown = RPKSkillCooldown(
                    character = character,
                    skill = skill,
                    cooldownTimestamp = LocalDateTime.now().plus(seconds.toLong(), SECONDS)
                )
                skillCooldownTable.insert(skillCooldown).join()
            } else {
                skillCooldown.cooldownTimestamp = LocalDateTime.now().plus(seconds.toLong(), SECONDS)
                skillCooldownTable.update(skillCooldown).join()
            }
            if (character.minecraftProfile?.isOnline == true) {
                val characterId = character.id
                if (characterId != null) {
                    val skillCooldowns = characterSkillCooldowns[characterId.value]?.plus(skill to skillCooldown.cooldownTimestamp) ?: mapOf(skill to skillCooldown.cooldownTimestamp)
                    characterSkillCooldowns[characterId.value] = skillCooldowns
                }
            }
        }
    }

    override fun loadSkillCooldowns(character: RPKCharacter): CompletableFuture<Map<RPKSkill, LocalDateTime>> {
        val characterId = character.id ?: return CompletableFuture.completedFuture(emptyMap())
        plugin.logger.info("Loading skill cooldowns for character ${character.name} (${characterId.value})...")
        return CompletableFuture.supplyAsync {
            val skillCooldowns = plugin.database.getTable(RPKSkillCooldownTable::class.java)[character].join()
                .associate {
                    it.skill to it.cooldownTimestamp
                }
            characterSkillCooldowns[characterId.value] = skillCooldowns
            plugin.logger.info("Loaded skill cooldowns for character ${character.name} (${characterId.value})")
            return@supplyAsync skillCooldowns
        }
    }

    override fun unloadSkillCooldowns(character: RPKCharacter) {
        val characterId = character.id ?: return
        characterSkillCooldowns.remove(characterId.value)
        plugin.logger.info("Unloaded skill cooldowns for character ${character.name} (${characterId.value})")
    }

    override fun getPreloadedSkillCooldown(character: RPKCharacter, skill: RPKSkill): Int? {
        val characterId = character.id ?: return null
        val cooldownEndTime = characterSkillCooldowns[characterId.value]?.get(skill) ?: return null
        return max(0L, SECONDS.between(LocalDateTime.now(), cooldownEndTime)).toInt()
    }

    override fun getSkillBinding(character: RPKCharacter, item: ItemStack): CompletableFuture<RPKSkill?> {
        val skillBindingTable = plugin.database.getTable(RPKSkillBindingTable::class.java)
        return skillBindingTable.get(character).thenApply { skillBindings ->
            skillBindings.firstOrNull { skillBinding -> skillBinding.item.isSimilar(item) }?.skill
        }
    }

    override fun setSkillBinding(character: RPKCharacter, item: ItemStack, skill: RPKSkill?): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val skillBindingTable = plugin.database.getTable(RPKSkillBindingTable::class.java)
            val skillBindings = skillBindingTable.get(character).join()
            if (skill != null) {
                if (skillBindings.none { skillBinding -> skillBinding.item.isSimilar(item) }) {
                    skillBindingTable.insert(
                        RPKSkillBinding(
                            character = character,
                            item = item,
                            skill = skill
                        )
                    ).join()
                    if (character.minecraftProfile?.isOnline == true) {
                        val characterId = character.id
                        if (characterId != null) {
                            characterSkillBindings[characterId.value] = characterSkillBindings[characterId.value]?.plus(item to skill) ?: mapOf(item to skill)
                        }
                    }
                }
            } else {
                val skillBinding = skillBindings.firstOrNull { skillBinding -> skillBinding.item.isSimilar(item) }
                if (skillBinding != null) {
                    skillBindingTable.delete(skillBinding).join()
                    if (character.minecraftProfile?.isOnline == true) {
                        val characterId = character.id
                        if (characterId != null) {
                            characterSkillBindings[characterId.value] = characterSkillBindings[characterId.value]?.filter { !it.key.isSimilar(item) } ?: mapOf()
                        }
                    }
                }
            }
        }
    }

    override fun loadSkillBindings(character: RPKCharacter): CompletableFuture<Map<ItemStack, RPKSkill>> {
        val characterId = character.id ?: return CompletableFuture.completedFuture(emptyMap())
        plugin.logger.info("Loading skill bindings for character ${character.name} (${characterId.value})...")
        return CompletableFuture.supplyAsync {
            val skillBindings = plugin.database.getTable(RPKSkillBindingTable::class.java).get(character).join()
                .associate { binding ->
                    binding.item to binding.skill
                }
            characterSkillBindings[characterId.value] = skillBindings
            plugin.logger.info("Loaded skill bindings for character ${character.name} (${characterId.value})")
            return@supplyAsync skillBindings
        }
    }

    override fun unloadSkillBindings(character: RPKCharacter) {
        val characterId = character.id ?: return
        characterSkillBindings.remove(characterId.value)
        plugin.logger.info("Unloaded skill bindings for character ${character.name} (${characterId.value})")
    }

    override fun getPreloadedSkillBinding(character: RPKCharacter, item: ItemStack): RPKSkill? {
        val characterId = character.id ?: return null
        val bindings = characterSkillBindings[characterId.value] ?: return null
        return bindings.entries.firstOrNull { it.key.isSimilar(item) }?.value
    }

}