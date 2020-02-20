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

package com.rpkit.statbuilds.bukkit.command.statbuild

import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import com.rpkit.statbuilds.bukkit.RPKStatBuildsBukkit
import com.rpkit.statbuilds.bukkit.statattribute.RPKStatAttributeProvider
import com.rpkit.statbuilds.bukkit.statbuild.RPKStatBuildProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class StatBuildAssignPointCommand(private val plugin: RPKStatBuildsBukkit): CommandExecutor {

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
        val statAttributeProvicer = plugin.core.serviceManager.getServiceProvider(RPKStatAttributeProvider::class)
        val statAttribute = statAttributeProvicer.getStatAttribute(statAttributeName)
        if (statAttribute == null) {
            sender.sendMessage(plugin.messages["stat-build-assign-point-invalid-stat-attribute"])
            return true
        }
        val points = if (args.size >= 2) args[1].toIntOrNull() else 1
        if (points == null) {
            sender.sendMessage(plugin.messages["stat-build-assign-point-invalid-points-integer"])
            return true
        }
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(sender)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-self"])
            return true
        }
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val character = characterProvider.getActiveCharacter(minecraftProfile)
        if (character == null) {
            sender.sendMessage(plugin.messages["no-character-self"])
            return true
        }
        val statBuildProvider = plugin.core.serviceManager.getServiceProvider(RPKStatBuildProvider::class)
        if (points > statBuildProvider.getUnassignedStatPoints(character)) {
            sender.sendMessage(plugin.messages["stat-build-assign-point-invalid-points-not-enough"])
            return true
        }
        if (statBuildProvider.getStatPoints(character, statAttribute) + points > statBuildProvider.getMaxStatPoints(character, statAttribute)) {
            sender.sendMessage(plugin.messages["stat-build-assign-point-invalid-points-too-many-in-stat"])
            return true
        }
        statBuildProvider.setStatPoints(character, statAttribute, statBuildProvider.getStatPoints(character, statAttribute) + points)
        sender.sendMessage(plugin.messages["stat-build-assign-point-valid", mapOf(
                "character" to character.name,
                "stat-attribute" to statAttribute.name,
                "points" to points.toString(),
                "total-points" to statBuildProvider.getStatPoints(character, statAttribute).toString(),
                "max-points" to statBuildProvider.getMaxStatPoints(character, statAttribute).toString()
        )])
        return true
    }

}