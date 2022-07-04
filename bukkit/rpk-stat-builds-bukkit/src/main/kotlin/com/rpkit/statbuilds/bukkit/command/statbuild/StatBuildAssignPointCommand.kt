/*
 * Copyright 2022 Ren Binden
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

package com.rpkit.statbuilds.bukkit.command.statbuild

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.statbuilds.bukkit.RPKStatBuildsBukkit
import com.rpkit.statbuilds.bukkit.statattribute.RPKStatAttributeName
import com.rpkit.statbuilds.bukkit.statattribute.RPKStatAttributeService
import com.rpkit.statbuilds.bukkit.statbuild.RPKStatBuildService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class StatBuildAssignPointCommand(private val plugin: RPKStatBuildsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.statbuilds.command.statbuild.assignpoint")) {
            sender.sendMessage(plugin.messages["no-permission-stat-build-assign-point"])
            return true
        }
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages["stat-build-assign-point-usage"])
            return true
        }
        val statAttributeName = args[0]
        val statAttributeService = Services[RPKStatAttributeService::class.java]
        if (statAttributeService == null) {
            sender.sendMessage(plugin.messages["no-stat-attribute-service"])
            return true
        }
        val statAttribute = statAttributeService.getStatAttribute(RPKStatAttributeName(statAttributeName))
        if (statAttribute == null) {
            sender.sendMessage(plugin.messages["stat-build-assign-point-invalid-stat-attribute"])
            return true
        }
        val points = if (args.size >= 2) args[1].toIntOrNull() else 1
        if (points == null) {
            sender.sendMessage(plugin.messages["stat-build-assign-point-invalid-points-integer"])
            return true
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-service"])
            return true
        }
        val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(sender)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-self"])
            return true
        }
        val characterService = Services[RPKCharacterService::class.java]
        if (characterService == null) {
            sender.sendMessage(plugin.messages["no-character-service"])
            return true
        }
        val character = characterService.getPreloadedActiveCharacter(minecraftProfile)
        if (character == null) {
            sender.sendMessage(plugin.messages["no-character-self"])
            return true
        }
        val statBuildService = Services[RPKStatBuildService::class.java]
        if (statBuildService == null) {
            sender.sendMessage(plugin.messages["no-stat-build-service"])
            return true
        }
        statBuildService.getUnassignedStatPoints(character).thenAccept getUnassignedStatPoints@{ unasssignedStatPoints ->
            if (points > unasssignedStatPoints) {
                sender.sendMessage(plugin.messages["stat-build-assign-point-invalid-points-not-enough"])
                return@getUnassignedStatPoints
            }
            statBuildService.getStatPoints(character, statAttribute).thenAccept getStatPoints@{ assignedStatPoints ->
                val maxStatPoints = statBuildService.getMaxStatPoints(character, statAttribute)
                if (assignedStatPoints + points > maxStatPoints) {
                    sender.sendMessage(plugin.messages["stat-build-assign-point-invalid-points-too-many-in-stat"])
                    return@getStatPoints
                }
                statBuildService.setStatPoints(character, statAttribute, assignedStatPoints + points).thenRunAsync {
                    sender.sendMessage(
                        plugin.messages["stat-build-assign-point-valid", mapOf(
                            "character" to character.name,
                            "stat_attribute" to statAttribute.name.value,
                            "points" to points.toString(),
                            "total_points" to statBuildService.getStatPoints(character, statAttribute).join().toString(),
                            "max_points" to statBuildService.getMaxStatPoints(character, statAttribute).toString()
                        )]
                    )
                }
            }

        }

        return true
    }

}