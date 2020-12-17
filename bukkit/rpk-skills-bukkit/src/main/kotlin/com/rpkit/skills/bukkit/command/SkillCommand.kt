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

package com.rpkit.skills.bukkit.command

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.skills.bukkit.RPKSkillsBukkit
import com.rpkit.skills.bukkit.skills.RPKSkillService
import com.rpkit.skills.bukkit.skills.canUse
import com.rpkit.skills.bukkit.skills.use
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


class SkillCommand(private val plugin: RPKSkillsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.skills.command.skill")) {
            sender.sendMessage(plugin.messages["no-permission-skill"])
            return true
        }
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-service"])
            return true
        }
        val characterService = Services[RPKCharacterService::class.java]
        if (characterService == null) {
            sender.sendMessage(plugin.messages["no-character-service"])
            return true
        }
        val skillService = Services[RPKSkillService::class.java]
        if (skillService == null) {
            sender.sendMessage(plugin.messages["no-skill-service"])
            return true
        }
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(sender)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile"])
            return true
        }
        val character = characterService.getActiveCharacter(minecraftProfile)
        if (character == null) {
            sender.sendMessage(plugin.messages["no-character"])
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages["skill-list-title"])
            skillService.skills
                    .filter { skill -> character.canUse(skill) }
                    .forEach { skill ->
                        sender.sendMessage(plugin.messages["skill-list-item", mapOf(
                                Pair("skill", skill.name)
                        )])
                    }
            return true
        }
        val skill = skillService.getSkill(args[0])
        if (skill != null) {
            if (character.canUse(skill)) {
                if (character.mana >= skill.manaCost) {
                    if (skillService.getSkillCooldown(character, skill) <= 0) {
                        character.use(skill)
                        skillService.setSkillCooldown(character, skill, skill.cooldown)
                        character.mana -= skill.manaCost
                        characterService.updateCharacter(character)
                        sender.sendMessage(plugin.messages["skill-valid", mapOf(
                                Pair("skill", skill.name)
                        )])
                    } else {
                        sender.sendMessage(plugin.messages["skill-invalid-on-cooldown", mapOf(
                                Pair("skill", skill.name),
                                Pair("cooldown", skillService.getSkillCooldown(character, skill).toString())
                        )])
                    }
                } else {
                    sender.sendMessage(plugin.messages["skill-invalid-not-enough-mana", mapOf(
                            Pair("skill", skill.name),
                            Pair("mana-cost", skill.manaCost.toString()),
                            Pair("mana", character.mana.toString()),
                            Pair("max-mana", character.maxMana.toString())
                    )])
                }
            } else {
                sender.sendMessage(plugin.messages["skill-invalid-unmet-prerequisites", mapOf(
                        Pair("skill", skill.name)
                )])
            }
        } else {
            sender.sendMessage(plugin.messages["skill-invalid-skill"])
        }
        return true
    }

}