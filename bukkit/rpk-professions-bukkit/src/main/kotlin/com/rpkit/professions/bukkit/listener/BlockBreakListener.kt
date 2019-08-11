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
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import kotlin.math.roundToInt
import kotlin.random.Random


class BlockBreakListener(private val plugin: RPKProfessionsBukkit): Listener {

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val professionProvider = plugin.core.serviceManager.getServiceProvider(RPKProfessionProvider::class)
        val bukkitPlayer = event.player
        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitPlayer)
        if (minecraftProfile == null) {
            event.isDropItems = false
            return
        }
        val character = characterProvider.getActiveCharacter(minecraftProfile)
        if (character == null) {
            event.isDropItems = false
            return
        }
        val professions = professionProvider.getProfessions(character)
        val professionLevels = professions
                .associateWith { profession -> professionProvider.getProfessionLevel(character, profession) }
        val itemsToDrop = mutableListOf<ItemStack>()
        for (item in event.block.getDrops(event.player.inventory.itemInMainHand)) {
            val material = item.type
            val amount = professionLevels.entries
                    .map { (profession, level) -> profession.getAmountFor(RPKCraftingAction.MINE, material, level) }
                    .firstOrNull() ?: plugin.config.getDouble("default.mining.$material.amount", 1.0)
            val potentialQualities = professionLevels.entries
                    .mapNotNull { (profession, level) -> profession.getQualityFor(RPKCraftingAction.MINE, material, level) }
            val quality = potentialQualities.maxBy(RPKItemQuality::durabilityModifier)
            if (quality != null) {
                item.addLore(quality.lore)
            }
            if (amount > 1) {
                item.amount = amount.roundToInt()
                itemsToDrop.add(item)
            } else if (amount < 1) {
                val random = Random.nextDouble()
                if (random <= amount) {
                    item.amount = 1
                    itemsToDrop.add(item)
                }
            } else {
                itemsToDrop.add(item)
            }
            professions.forEach { profession ->
                val receivedExperience = plugin.config.getInt("professions.${profession.name}.experience.items.mining.$material", 0) * item.amount
                if (receivedExperience > 0) {
                    professionProvider.setProfessionExperience(character, profession, professionProvider.getProfessionExperience(character, profession) + receivedExperience)
                    val level = professionProvider.getProfessionLevel(character, profession)
                    val experience = professionProvider.getProfessionExperience(character, profession)
                    event.player.sendMessage(plugin.messages["mine-experience", mapOf(
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
        for (item in itemsToDrop) {
            event.block.world.dropItemNaturally(event.block.location, item)
        }
    }

}