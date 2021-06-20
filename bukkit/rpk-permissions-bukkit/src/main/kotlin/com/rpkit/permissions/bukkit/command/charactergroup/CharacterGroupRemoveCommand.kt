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

package com.rpkit.permissions.bukkit.command.charactergroup

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.permissions.bukkit.RPKPermissionsBukkit
import com.rpkit.permissions.bukkit.group.RPKGroupName
import com.rpkit.permissions.bukkit.group.RPKGroupService
import com.rpkit.permissions.bukkit.group.removeGroup
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

/**
 * Group remove command.
 * Removes a group.
 */
class CharacterGroupRemoveCommand(private val plugin: RPKPermissionsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.permissions.command.charactergroup.remove")) {
            sender.sendMessage(plugin.messages.noPermissionCharacterGroupRemove)
            return true
        }
        if (args.size <= 1) {
            sender.sendMessage(plugin.messages.characterGroupRemoveUsage)
            return true
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages.noMinecraftProfileService)
            return true
        }
        val characterService = Services[RPKCharacterService::class.java]
        if (characterService == null) {
            sender.sendMessage(plugin.messages.noCharacterService)
            return true
        }
        val groupService = Services[RPKGroupService::class.java]
        if (groupService == null) {
            sender.sendMessage(plugin.messages.noGroupService)
            return true
        }
        val bukkitPlayer = plugin.server.getPlayer(args[0])
        if (bukkitPlayer == null) {
            sender.sendMessage(plugin.messages.characterGroupRemoveInvalidPlayer)
            return true
        }
        val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(bukkitPlayer)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages.noMinecraftProfileOther)
            return true
        }
        val character = characterService.getPreloadedActiveCharacter(minecraftProfile)
        if (character == null) {
            sender.sendMessage(plugin.messages.noCharacter)
            return true
        }
        val group = groupService.getGroup(RPKGroupName(args[1]))
        if (group == null) {
            sender.sendMessage(plugin.messages.characterGroupRemoveInvalidGroup)
            return true
        }
        if (!sender.hasPermission("rpkit.permissions.command.charactergroup.remove.${group.name.value}")) {
            sender.sendMessage(plugin.messages.noPermissionCharacterGroupRemoveGroup.withParameters(
                group = group
            ))
            return true
        }
        character.removeGroup(group)
        sender.sendMessage(plugin.messages.characterGroupRemoveValid.withParameters(
            group = group,
            character = character
        ))
        return true
    }

}