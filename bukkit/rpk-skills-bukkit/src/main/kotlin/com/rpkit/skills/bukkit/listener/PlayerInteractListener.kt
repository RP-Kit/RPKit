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

package com.rpkit.skills.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import com.rpkit.skills.bukkit.RPKSkillsBukkit
import com.rpkit.skills.bukkit.skills.RPKSkillProvider
import com.rpkit.skills.bukkit.skills.canUse
import com.rpkit.skills.bukkit.skills.use
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent


class PlayerInteractListener(private val plugin: RPKSkillsBukkit): Listener {

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.player.hasPermission("rpkit.skills.command.skill")) {
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
            val skillProvider = plugin.core.serviceManager.getServiceProvider(RPKSkillProvider::class)
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(event.player)
            if (minecraftProfile != null) {
                val character = characterProvider.getActiveCharacter(minecraftProfile)
                if (character != null) {
                    val item = event.item
                    if (item != null) {
                        val skill = skillProvider.getSkillBinding(character, item)
                        if (skill != null) {
                            if (character.canUse(skill)) {
                                if (character.mana >= skill.manaCost) {
                                    if (skillProvider.getSkillCooldown(character, skill) <= 0) {
                                        character.use(skill)
                                        skillProvider.setSkillCooldown(character, skill, skill.cooldown)
                                        character.mana -= skill.manaCost
                                        characterProvider.updateCharacter(character)
                                        event.player.sendMessage(plugin.messages["skill-valid", mapOf(
                                                Pair("skill", skill.name)
                                        )])
                                    } else {
                                        event.player.sendMessage(plugin.messages["skill-invalid-on-cooldown", mapOf(
                                                Pair("skill", skill.name),
                                                Pair("cooldown", skillProvider.getSkillCooldown(character, skill).toString())
                                        )])
                                    }
                                } else {
                                    event.player.sendMessage(plugin.messages["skill-invalid-not-enough-mana", mapOf(
                                            Pair("skill", skill.name),
                                            Pair("mana-cost", skill.manaCost.toString()),
                                            Pair("mana", character.mana.toString()),
                                            Pair("max-mana", character.maxMana.toString())
                                    )])
                                }
                            } else {
                                event.player.sendMessage(plugin.messages["skill-invalid-unmet-prerequisites", mapOf(
                                        Pair("skill", skill.name)
                                )])
                            }
                        }
                    }
                }
            }
        }
    }

}