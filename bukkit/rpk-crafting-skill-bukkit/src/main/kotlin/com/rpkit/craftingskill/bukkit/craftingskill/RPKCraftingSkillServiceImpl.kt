/*
 * Copyright 2022 Ren Binden
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

package com.rpkit.craftingskill.bukkit.craftingskill

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.service.Services
import com.rpkit.craftingskill.bukkit.RPKCraftingSkillBukkit
import com.rpkit.craftingskill.bukkit.craftingskill.RPKCraftingAction.*
import com.rpkit.craftingskill.bukkit.database.table.RPKCraftingExperienceTable
import com.rpkit.craftingskill.bukkit.event.craftingskill.RPKBukkitCraftingSkillExperienceChangeEvent
import com.rpkit.itemquality.bukkit.itemquality.RPKItemQuality
import com.rpkit.itemquality.bukkit.itemquality.RPKItemQualityName
import com.rpkit.itemquality.bukkit.itemquality.RPKItemQualityService
import org.bukkit.Material
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap


class RPKCraftingSkillServiceImpl(override val plugin: RPKCraftingSkillBukkit) : RPKCraftingSkillService {

    private data class CraftingActionKey(
        val characterId: Int,
        val action: RPKCraftingAction,
        val material: Material
    )

    private val craftingExperience = ConcurrentHashMap<CraftingActionKey, Int>()

    override fun getCraftingExperience(character: RPKCharacter, action: RPKCraftingAction, material: Material): CompletableFuture<Int> {
        return plugin.database.getTable(RPKCraftingExperienceTable::class.java)[character, action, material].thenApply { it?.experience ?: 0 }
    }

    override fun getPreloadedCraftingExperience(
        character: RPKCharacter,
        action: RPKCraftingAction,
        material: Material
    ): Int {
        val characterId = character.id ?: return 0
        return craftingExperience[CraftingActionKey(characterId.value, action, material)] ?: 0
    }

    override fun loadCraftingExperience(character: RPKCharacter): CompletableFuture<Void> {
        val characterId = character.id ?: return CompletableFuture.completedFuture(null)
        return plugin.database.getTable(RPKCraftingExperienceTable::class.java)[character].thenAccept { craftingExperienceValues ->
            craftingExperienceValues.forEach { craftingExperienceValue ->
                craftingExperience[CraftingActionKey(characterId.value, craftingExperienceValue.action, craftingExperienceValue.material)] = craftingExperienceValue.experience
            }
        }
    }

    override fun unloadCraftingExperience(character: RPKCharacter) {
        val characterId = character.id ?: return
        val iterator = craftingExperience.keys.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().characterId == characterId.value) iterator.remove()
        }
    }

    override fun setCraftingExperience(character: RPKCharacter, action: RPKCraftingAction, material: Material, experience: Int): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val actionConfigSectionName = when (action) {
                CRAFT -> "crafting"
                SMELT -> "smelting"
                MINE -> "mining"
            }
            val maxExperience = plugin.config.getConfigurationSection("$actionConfigSectionName.$material")
                ?.getKeys(false)
                ?.maxOfOrNull(String::toInt)
                ?: 0
            if (maxExperience == 0) return@runAsync
            val craftingExperienceTable = plugin.database.getTable(RPKCraftingExperienceTable::class.java)
            val event = RPKBukkitCraftingSkillExperienceChangeEvent(
                character,
                action,
                material,
                craftingExperienceTable[character, action, material].thenApply { it?.experience ?: 0 }.join(),
                experience,
                true
            )
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            var craftingExperience = craftingExperienceTable[event.character, event.action, event.material].join()
            if (craftingExperience == null) {
                craftingExperience = RPKCraftingExperienceValue(
                    character = event.character,
                    action = event.action,
                    material = event.material,
                    experience = event.experience.coerceAtMost(maxExperience)
                )
                craftingExperienceTable.insert(craftingExperience).join()
                val characterId = craftingExperience.character.id ?: return@runAsync
                this.craftingExperience[CraftingActionKey(characterId.value, craftingExperience.action, craftingExperience.material)] = craftingExperience.experience
            } else {
                craftingExperience.experience = event.experience.coerceAtMost(maxExperience)
                craftingExperienceTable.update(craftingExperience).join()
                val characterId = craftingExperience.character.id ?: return@runAsync
                this.craftingExperience[CraftingActionKey(characterId.value, craftingExperience.action, craftingExperience.material)] = craftingExperience.experience
            }
        }
    }

    override fun getQualityFor(action: RPKCraftingAction, material: Material, experience: Int): RPKItemQuality? {
        val itemQualityService = Services[RPKItemQualityService::class.java] ?: return null
        val actionConfigSectionName = when (action) {
            CRAFT -> "crafting"
            SMELT -> "smelting"
            MINE -> "mining"
        }
        val itemQualityName = plugin.config.getConfigurationSection("$actionConfigSectionName.$material")
                ?.getKeys(false)
                ?.asSequence()
                ?.map { key -> key.toInt() }
                ?.sortedDescending()
                ?.dropWhile { requiredExperience -> requiredExperience > experience }
                ?.mapNotNull { requiredExperience -> plugin.config.getString("$actionConfigSectionName.$material.$requiredExperience.quality") }
                ?.firstOrNull() ?: return null
        return itemQualityService.getItemQuality(RPKItemQualityName(itemQualityName))
    }

    override fun getAmountFor(action: RPKCraftingAction, material: Material, experience: Int): Double {
        val actionConfigSectionName = when (action) {
            CRAFT -> "crafting"
            SMELT -> "smelting"
            MINE -> "mining"
        }
        return plugin.config.getConfigurationSection("$actionConfigSectionName.$material")
                ?.getKeys(false)
                ?.asSequence()
                ?.map { key -> key.toInt() }
                ?.sortedDescending()
                ?.dropWhile { requiredExperience -> requiredExperience > experience }
                ?.map { requiredExperience -> plugin.config.getDouble("$actionConfigSectionName.$material.$requiredExperience.amount", 1.0) }
                ?.firstOrNull() ?: 1.0
    }

}