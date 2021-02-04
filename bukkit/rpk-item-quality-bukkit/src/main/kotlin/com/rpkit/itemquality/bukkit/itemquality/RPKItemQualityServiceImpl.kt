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

package com.rpkit.itemquality.bukkit.itemquality

import com.rpkit.core.bukkit.extension.addLore
import com.rpkit.core.bukkit.extension.removeLore
import com.rpkit.itemquality.bukkit.RPKItemQualityBukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack


class RPKItemQualityServiceImpl(override val plugin: RPKItemQualityBukkit) : RPKItemQualityService {

    override val itemQualities: List<RPKItemQuality> = plugin.config.getConfigurationSection("item-qualities")
            ?.getValues(false)
            ?.map { qualitySection ->
                val name = qualitySection.key
                val section = qualitySection.value as ConfigurationSection
                val lore = section.getStringList("lore")
                        .map { lore -> ChatColor.translateAlternateColorCodes('&', lore) }
                val durabilityModifier = section.getDouble("durability-modifier")
                val applicableItems = section.getStringList("applicable-items")
                        .mapNotNull { materialName -> Material.matchMaterial(materialName) }
                return@map RPKItemQualityImpl(
                        RPKItemQualityName(name),
                        lore,
                        durabilityModifier,
                        applicableItems
                )
            }
            ?: emptyList()

    override fun getItemQuality(name: RPKItemQualityName): RPKItemQuality? {
        return itemQualities.firstOrNull { quality -> quality.name.value == name.value }
    }

    override fun getItemQuality(item: ItemStack): RPKItemQuality? {
        return itemQualities.firstOrNull { quality -> item.itemMeta?.lore?.containsAll(quality.lore) == true }
    }

    override fun setItemQuality(item: ItemStack, quality: RPKItemQuality) {
        val oldQuality = getItemQuality(item)
        if (oldQuality != null) {
            item.removeLore(oldQuality.lore)
        }
        if (quality.isApplicableFor(item)) {
            item.addLore(quality.lore)
        }
    }
}