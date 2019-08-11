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

package com.rpkit.professions.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.core.bukkit.util.addLore
import com.rpkit.itemquality.bukkit.itemquality.RPKItemQuality
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import com.rpkit.professions.bukkit.RPKProfessionsBukkit
import com.rpkit.professions.bukkit.profession.RPKCraftingAction
import com.rpkit.professions.bukkit.profession.RPKProfessionProvider
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.FurnaceInventory
import kotlin.math.roundToInt
import kotlin.random.Random


class InventoryClickListener(private val plugin: RPKProfessionsBukkit): Listener {

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.clickedInventory !is FurnaceInventory) return
        if (event.slotType != InventoryType.SlotType.RESULT) return
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val professionProvider = plugin.core.serviceManager.getServiceProvider(RPKProfessionProvider::class)
        val bukkitPlayer = event.viewers.firstOrNull() as? Player ?: return
        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitPlayer) ?: return
        val character = characterProvider.getActiveCharacter(minecraftProfile) ?: return
        val item = event.currentItem ?: return
        if (item.amount == 0 || item.type == Material.AIR) return
        val material = item.type
        val professions = professionProvider.getProfessions(character)
        val professionLevels = professions
                .associateWith { profession -> professionProvider.getProfessionLevel(character, profession) }
        val potentialQualities = professionLevels.entries
                .mapNotNull { (profession, level) -> profession.getQualityFor(RPKCraftingAction.SMELT, material, level) }
        val quality = potentialQualities.maxBy(RPKItemQuality::durabilityModifier)
        val amount = professionLevels.entries
                .map { (profession, level) -> profession.getAmountFor(RPKCraftingAction.SMELT, material, level) }
                .firstOrNull() ?: plugin.config.getDouble("default.smelting.$material.amount", 1.0) * item.amount
        if (quality != null) {
            item.addLore(quality.lore)
        }
        if (amount > 1)  {
            item.amount = (amount * item.amount).roundToInt()
        } else if (amount < 1) {
            val random = Random.nextDouble()
            if (random <= amount) {
                item.amount = 1
            } else {
                event.currentItem = null
                return
            }
        }
        event.currentItem = item
        professions.forEach { profession ->
            val receivedExperience = plugin.config.getInt("professions.${profession.name}.experience.items.smelting.$material", 0) * item.amount
            if (receivedExperience > 0) {
                professionProvider.setProfessionExperience(character, profession, professionProvider.getProfessionExperience(character, profession) + receivedExperience)
                val level = professionProvider.getProfessionLevel(character, profession)
                val experience = professionProvider.getProfessionExperience(character, profession)
                event.whoClicked.sendMessage(plugin.messages["smelt-experience", mapOf(
                        "profession" to profession.name,
                        "level" to level.toString(),
                        "received-experience" to receivedExperience.toString(),
                        "experience" to (experience - profession.getExperienceNeededForLevel(level)).toString(),
                        "next-level-experience" to (profession.getExperienceNeededForLevel(level + 1) - profession.getExperienceNeededForLevel(level)).toString(),
                        "total-experience" to experience.toString(),
                        "total-next-level-experience" to profession.getExperienceNeededForLevel(level + 1).toString(),
                        "material" to material.toString().toLowerCase().replace('_', ' ')
                )])
            }
        }
    }

}