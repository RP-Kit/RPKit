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

package com.rpkit.skills.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.skills.bukkit.RPKSkillsBukkit
import com.rpkit.skills.bukkit.skills.RPKSkillService
import com.rpkit.skills.bukkit.skills.canUse
import com.rpkit.skills.bukkit.skills.use
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent


class PlayerInteractListener(private val plugin: RPKSkillsBukkit) : Listener {

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (!event.player.hasPermission("rpkit.skills.command.skill")) return
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        val characterService = Services[RPKCharacterService::class.java] ?: return
        val skillService = Services[RPKSkillService::class.java] ?: return
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(event.player) ?: return
        val character = characterService.getPreloadedActiveCharacter(minecraftProfile) ?: return
        val item = event.item ?: return
        val skill = skillService.getSkillBinding(character, item) ?: return
        if (!character.canUse(skill)) {
            event.player.sendMessage(plugin.messages["skill-invalid-unmet-prerequisites", mapOf(
                "skill" to skill.name.value
            )])
            return
        }
        if (character.mana < skill.manaCost) {
            event.player.sendMessage(plugin.messages["skill-invalid-not-enough-mana", mapOf(
                "skill" to skill.name.value,
                "mana_cost" to skill.manaCost.toString(),
                "mana" to character.mana.toString(),
                "max_mana" to character.maxMana.toString()
            )])
            return
        }
        if (skillService.getSkillCooldown(character, skill) > 0) {
            event.player.sendMessage(plugin.messages["skill-invalid-on-cooldown", mapOf(
                "skill" to skill.name.value,
                "cooldown" to skillService.getSkillCooldown(character, skill).toString()
            )])
            return
        }
        character.use(skill)
        skillService.setSkillCooldown(character, skill, skill.cooldown)
        character.mana -= skill.manaCost
        characterService.updateCharacter(character)
        event.player.sendMessage(plugin.messages["skill-valid", mapOf(
            "skill" to skill.name.value
        )])
    }

}