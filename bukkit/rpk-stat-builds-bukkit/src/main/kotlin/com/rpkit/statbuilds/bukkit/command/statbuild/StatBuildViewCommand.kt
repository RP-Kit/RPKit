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

package com.rpkit.statbuilds.bukkit.command.statbuild

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.statbuilds.bukkit.RPKStatBuildsBukkit
import com.rpkit.statbuilds.bukkit.statattribute.RPKStatAttributeService
import com.rpkit.statbuilds.bukkit.statbuild.RPKStatBuildService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class StatBuildViewCommand(private val plugin: RPKStatBuildsBukkit) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.statbuilds.command.statbuild.view")) {
            sender.sendMessage(plugin.messages["no-permission-stat-build-view"])
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
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(sender)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-self"])
            return true
        }
        val characterService = Services[RPKCharacterService::class.java]
        if (characterService == null) {
            sender.sendMessage(plugin.messages["no-character-service"])
            return true
        }
        val character = characterService.getActiveCharacter(minecraftProfile)
        if (character == null) {
            sender.sendMessage(plugin.messages["no-character-self"])
            return true
        }
        sender.sendMessage(plugin.messages["stat-build-view-title"])
        val statAttributeService = Services[RPKStatAttributeService::class.java]
        if (statAttributeService == null) {
            sender.sendMessage(plugin.messages["no-stat-attribute-service"])
            return true
        }
        val statBuildService = Services[RPKStatBuildService::class.java]
        if (statBuildService == null) {
            sender.sendMessage(plugin.messages["no-stat-build-service"])
            return true
        }
        sender.sendMessage(plugin.messages["stat-build-view-points-assignment-count", mapOf(
                "total" to statBuildService.getTotalStatPoints(character).toString(),
                "assigned" to statBuildService.getAssignedStatPoints(character).toString(),
                "unassigned" to statBuildService.getUnassignedStatPoints(character).toString()
        )])
        statAttributeService.statAttributes.forEach { statAttribute ->
            sender.sendMessage(plugin.messages["stat-build-view-item", mapOf(
                    "stat_attribute" to statAttribute.name.value,
                    "points" to statBuildService.getStatPoints(character, statAttribute).toString(),
                    "max_points" to statBuildService.getMaxStatPoints(character, statAttribute).toString()
            )])
        }
        return true
    }
}