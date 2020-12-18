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

package com.rpkit.craftingskill.bukkit.craftingskill

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.service.Services
import com.rpkit.craftingskill.bukkit.RPKCraftingSkillBukkit
import com.rpkit.craftingskill.bukkit.craftingskill.RPKCraftingAction.CRAFT
import com.rpkit.craftingskill.bukkit.craftingskill.RPKCraftingAction.MINE
import com.rpkit.craftingskill.bukkit.craftingskill.RPKCraftingAction.SMELT
import com.rpkit.craftingskill.bukkit.database.table.RPKCraftingExperienceTable
import com.rpkit.craftingskill.bukkit.event.craftingskill.RPKBukkitCraftingSkillExperienceChangeEvent
import com.rpkit.itemquality.bukkit.itemquality.RPKItemQuality
import com.rpkit.itemquality.bukkit.itemquality.RPKItemQualityService
import org.bukkit.Material
import java.lang.Math.min


class RPKCraftingSkillServiceImpl(override val plugin: RPKCraftingSkillBukkit) : RPKCraftingSkillService {

    override fun getCraftingExperience(character: RPKCharacter, action: RPKCraftingAction, material: Material): Int {
        return plugin.database.getTable(RPKCraftingExperienceTable::class.java)
                .get(character, action, material)?.experience ?: 0
    }

    override fun setCraftingExperience(character: RPKCharacter, action: RPKCraftingAction, material: Material, experience: Int) {
        val actionConfigSectionName = when (action) {
            CRAFT -> "crafting"
            SMELT -> "smelting"
            MINE -> "mining"
        }
        val maxExperience = plugin.config.getConfigurationSection("$actionConfigSectionName.$material")
            ?.getKeys(false)
            ?.map(String::toInt)
            ?.maxOrNull()
            ?: 0
        if (maxExperience == 0) return
        val craftingExperienceTable = plugin.database.getTable(RPKCraftingExperienceTable::class.java)
        val event = RPKBukkitCraftingSkillExperienceChangeEvent(
                character,
                action,
                material,
                craftingExperienceTable.get(character, action, material)?.experience ?: 0,
                experience
        )
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        var craftingExperience = craftingExperienceTable
                .get(event.character, event.action, event.material)
        if (craftingExperience == null) {
            craftingExperience = RPKCraftingExperienceValue(
                    character = event.character,
                    action = event.action,
                    material = event.material,
                    experience = min(event.experience, maxExperience)
            )
            craftingExperienceTable.insert(craftingExperience)
        } else {
            craftingExperience.experience = min(event.experience, maxExperience)
            craftingExperienceTable.update(craftingExperience)
        }
    }

    override fun getQualityFor(action: RPKCraftingAction, material: Material, experience: Int): RPKItemQuality? {
        val itemQualityService = Services[RPKItemQualityService::class.java]
        if (itemQualityService == null) return null
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
        return itemQualityService.getItemQuality(itemQualityName)
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