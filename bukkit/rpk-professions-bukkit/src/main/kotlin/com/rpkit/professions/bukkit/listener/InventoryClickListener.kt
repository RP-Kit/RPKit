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

package com.rpkit.professions.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.bukkit.extension.addLore
import com.rpkit.core.service.Services
import com.rpkit.itemquality.bukkit.itemquality.RPKItemQuality
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.professions.bukkit.RPKProfessionsBukkit
import com.rpkit.professions.bukkit.profession.RPKCraftingAction
import com.rpkit.professions.bukkit.profession.RPKProfessionService
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.FurnaceInventory
import kotlin.math.roundToInt
import kotlin.random.Random


class InventoryClickListener(private val plugin: RPKProfessionsBukkit) : Listener {

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.clickedInventory !is FurnaceInventory) return
        if (event.slotType != InventoryType.SlotType.RESULT) return
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        val characterService = Services[RPKCharacterService::class.java] ?: return
        val professionService = Services[RPKProfessionService::class.java] ?: return
        val bukkitPlayer = event.whoClicked as? Player ?: return
        if (bukkitPlayer.gameMode == GameMode.CREATIVE || bukkitPlayer.gameMode == GameMode.SPECTATOR) return
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(bukkitPlayer) ?: return
        val character = characterService.getActiveCharacter(minecraftProfile) ?: return
        val item = event.currentItem ?: return
        if (item.amount == 0 || item.type == Material.AIR) return
        val material = item.type
        val professions = professionService.getProfessions(character)
        val professionLevels = professions
                .associateWith { profession -> professionService.getProfessionLevel(character, profession) }
        val potentialQualities = professionLevels.entries
                .mapNotNull { (profession, level) -> profession.getQualityFor(RPKCraftingAction.SMELT, material, level) }
        val quality = potentialQualities.maxByOrNull(RPKItemQuality::durabilityModifier)
        val amount = professionLevels.entries
            .map { (profession, level) -> profession.getAmountFor(RPKCraftingAction.SMELT, material, level) }
            .maxOrNull() ?: plugin.config.getDouble("default.smelting.$material.amount", 1.0) * item.amount
        if (quality != null) {
            item.addLore(quality.lore)
        }
        if (amount > 1) {
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
            val receivedExperience = plugin.config.getInt("professions.${profession.name.value}.experience.items.smelting.$material", 0) * item.amount
            if (receivedExperience > 0) {
                professionService.setProfessionExperience(character, profession, professionService.getProfessionExperience(character, profession) + receivedExperience)
                val level = professionService.getProfessionLevel(character, profession)
                val experience = professionService.getProfessionExperience(character, profession)
                event.whoClicked.sendMessage(plugin.messages["smelt-experience", mapOf(
                        "profession" to profession.name.value,
                        "level" to level.toString(),
                        "received_experience" to receivedExperience.toString(),
                        "experience" to (experience - profession.getExperienceNeededForLevel(level)).toString(),
                        "next_level_experience" to (profession.getExperienceNeededForLevel(level + 1) - profession.getExperienceNeededForLevel(level)).toString(),
                        "total_experience" to experience.toString(),
                        "total_next_level_experience" to profession.getExperienceNeededForLevel(level + 1).toString(),
                        "material" to material.toString().toLowerCase().replace('_', ' ')
                )])
            }
        }
    }

}