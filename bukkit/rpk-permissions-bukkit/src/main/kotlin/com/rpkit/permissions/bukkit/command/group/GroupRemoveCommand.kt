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

package com.rpkit.permissions.bukkit.command.group

import com.rpkit.core.service.Services
import com.rpkit.permissions.bukkit.RPKPermissionsBukkit
import com.rpkit.permissions.bukkit.group.RPKGroupService
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileService
import com.rpkit.players.bukkit.profile.RPKProfile
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

/**
 * Group remove command.
 * Removes a group.
 */
class GroupRemoveCommand(private val plugin: RPKPermissionsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.permissions.command.group.remove")) {
            sender.sendMessage(plugin.messages["no-permission-group-remove-group"])
            return true
        }
        if (args.size <= 1) {
            sender.sendMessage(plugin.messages["group-remove-usage"])
            return true
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-service"])
            return true
        }
        val groupService = Services[RPKGroupService::class]
        if (groupService == null) {
            sender.sendMessage(plugin.messages["no-group-service"])
            return true
        }
        val bukkitPlayer = plugin.server.getPlayer(args[0])
        if (bukkitPlayer == null) {
            sender.sendMessage(plugin.messages["group-remove-invalid-player"])
            return true
        }
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(bukkitPlayer)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile"])
            return true
        }
        val profile = minecraftProfile.profile
        if (profile !is RPKProfile) {
            sender.sendMessage(plugin.messages["no-profile"])
            return true
        }
        val group = groupService.getGroup(args[1])
        if (group == null) {
            sender.sendMessage(plugin.messages["group-remove-invalid-group"])
            return true
        }
        groupService.removeGroup(profile, group)
        sender.sendMessage(plugin.messages["group-remove-valid", mapOf(
                Pair("group", group.name),
                Pair("player", minecraftProfile.minecraftUsername)
        )])
        return true
    }

}