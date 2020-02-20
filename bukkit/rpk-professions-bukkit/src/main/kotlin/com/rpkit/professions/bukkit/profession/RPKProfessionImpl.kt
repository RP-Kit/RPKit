/*
 * Copyright 2019 Ren Binden
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

package com.rpkit.professions.bukkit.profession

import com.rpkit.itemquality.bukkit.itemquality.RPKItemQuality
import com.rpkit.itemquality.bukkit.itemquality.RPKItemQualityProvider
import com.rpkit.professions.bukkit.RPKProfessionsBukkit
import org.bukkit.Material
import org.nfunk.jep.JEP
import kotlin.math.roundToInt


class RPKProfessionImpl(
        override var id: Int = 0,
        override val name: String,
        override val maxLevel: Int,
        val plugin: RPKProfessionsBukkit
): RPKProfession {
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
        val itemQualityProvider = plugin.core.serviceManager.getServiceProvider(RPKItemQualityProvider::class)
        val itemQualityName = plugin.config.getString("professions.$name.$actionConfigSectionName.$level.$material.quality",
                if (level > 1)
                    return getQualityFor(action, material, level - 1)
                else
                    plugin.config.getString("default.$actionConfigSectionName.$material.quality")
        ) ?: return null
        return itemQualityProvider.getItemQuality(itemQualityName)
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