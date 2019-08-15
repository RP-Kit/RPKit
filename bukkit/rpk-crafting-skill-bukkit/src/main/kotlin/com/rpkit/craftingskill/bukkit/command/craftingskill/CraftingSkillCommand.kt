/*
 * Copyright 2019 Ross Binden
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

package com.rpkit.craftingskill.bukkit.command.craftingskill

import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.craftingskill.bukkit.RPKCraftingSkillBukkit
import com.rpkit.craftingskill.bukkit.craftingskill.RPKCraftingAction
import com.rpkit.craftingskill.bukkit.craftingskill.RPKCraftingSkillProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


class CraftingSkillCommand(private val plugin: RPKCraftingSkillBukkit): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        if (!sender.hasPermission("rpkit.craftingskill.command.craftingskill")) {
            sender.sendMessage(plugin.messages["no-permission-crafting-skill"])
            return true
        }
        if (args.size < 2) {
            sender.sendMessage(plugin.messages["crafting-skill-usage"])
            return true
        }
        val actionName = args[0].toUpperCase()
        val action = try {
            RPKCraftingAction.valueOf(actionName.toUpperCase())
        } catch (exception: IllegalArgumentException) {
            sender.sendMessage(plugin.messages["crafting-skill-actions-title"])
            RPKCraftingAction.values().forEach { action ->
                sender.sendMessage(plugin.messages["crafting-skill-actions-item", mapOf(
                        "action" to action.name
                )])
            }
            return true
        }
        val actionConfigSectionName = when (action) {
            RPKCraftingAction.CRAFT -> "crafting"
            RPKCraftingAction.SMELT -> "smelting"
            RPKCraftingAction.MINE -> "mining"
        }
        val material = Material.matchMaterial(args.drop(1).joinToString("_").toUpperCase())
        if (material == null) {
            sender.sendMessage(plugin.messages["crafting-skill-invalid-material"])
            return true
        }
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(sender)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile"])
            return true
        }
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val character = characterProvider.getActiveCharacter(minecraftProfile)
        if (character == null) {
            sender.sendMessage(plugin.messages["no-character"])
            return true
        }
        val craftingSkillProvider = plugin.core.serviceManager.getServiceProvider(RPKCraftingSkillProvider::class)
        val totalExperience = craftingSkillProvider.getCraftingExperience(character, action, material)
        val maxExperience = plugin.config.getConfigurationSection("$actionConfigSectionName.$material")
                ?.getKeys(false)
                ?.map(String::toInt)
                ?.max()
                ?: 0
        sender.sendMessage(plugin.messages["crafting-skill-valid", mapOf(
                "action" to action.toString().toLowerCase(),
                "material" to material.toString().toLowerCase().replace('_', ' '),
                "total-experience" to totalExperience.toString(),
                "max-experience" to maxExperience.toString()
        )])
        return true
    }

}