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

package com.rpkit.professions.bukkit.profession

import com.rpkit.core.service.Services
import com.rpkit.itemquality.bukkit.itemquality.RPKItemQuality
import com.rpkit.itemquality.bukkit.itemquality.RPKItemQualityName
import com.rpkit.itemquality.bukkit.itemquality.RPKItemQualityService
import com.rpkit.professions.bukkit.RPKProfessionsBukkit
import org.bukkit.Material
import org.nfunk.jep.JEP
import kotlin.math.roundToInt


class RPKProfessionImpl(
    override val name: RPKProfessionName,
    override val maxLevel: Int,
    val plugin: RPKProfessionsBukkit
) : RPKProfession {
    override fun getAmountFor(action: RPKCraftingAction, material: Material, level: Int): Double {
        val actionConfigSectionName = when (action) {
            RPKCraftingAction.CRAFT -> "crafting"
            RPKCraftingAction.SMELT -> "smelting"
            RPKCraftingAction.MINE -> "mining"
        }
        return plugin.config.getDouble("professions.$name.$actionConfigSectionName.$level.$material.amount",
                if (level > 1)
                    getAmountFor(action, material, level - 1)
                else
                    plugin.config.getDouble("default.$actionConfigSectionName.$material.amount", 1.0)
        )
    }

    override fun getQualityFor(action: RPKCraftingAction, material: Material, level: Int): RPKItemQuality? {
        val actionConfigSectionName = when (action) {
            RPKCraftingAction.CRAFT -> "crafting"
            RPKCraftingAction.SMELT -> "smelting"
            RPKCraftingAction.MINE -> "mining"
        }
        val itemQualityService = Services[RPKItemQualityService::class.java] ?: return null
        val itemQualityName = plugin.config.getString("professions.$name.$actionConfigSectionName.$level.$material.quality")
                ?: when {
                    level > 1 -> return getQualityFor(action, material, level - 1)
                    else -> plugin.config.getString("default.$actionConfigSectionName.$material.quality")
                }
                ?: return null
        return itemQualityService.getItemQuality(RPKItemQualityName(itemQualityName))
    }

    override fun getExperienceNeededForLevel(level: Int): Int {
        val expression = plugin.config.getString("professions.$name.experience.formula")
        val parser = JEP()
        parser.addStandardConstants()
        parser.addStandardFunctions()
        parser.addVariable("level", level.toDouble())
        parser.parseExpression(expression)
        return parser.value.roundToInt()
    }

    override fun getExperienceFor(action: RPKCraftingAction, material: Material): Int {
        val actionConfigSectionName = when (action) {
            RPKCraftingAction.CRAFT -> "crafting"
            RPKCraftingAction.SMELT -> "smelting"
            RPKCraftingAction.MINE -> "mining"
        }
        return plugin.config.getInt("professions.$name.experience.items.$actionConfigSectionName.$material")
    }
}