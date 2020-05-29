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

import com.rpkit.permissions.bukkit.RPKPermissionsBukkit
import com.rpkit.permissions.bukkit.group.RPKGroupProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import com.rpkit.players.bukkit.profile.RPKProfile
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

/**
 * Group add command.
 * Adds a player to a group.
 */
class GroupAddCommand(private val plugin: RPKPermissionsBukkit): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender.hasPermission("rpkit.permissions.command.group.add")) {
            if (args.size > 1) {
                val minecraftProfileProvider= plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
                val groupProvider = plugin.core.serviceManager.getServiceProvider(RPKGroupProvider::class)
                val bukkitPlayer = plugin.server.getPlayer(args[0])
                if (bukkitPlayer != null) {
                    val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitPlayer)
                    if (minecraftProfile != null) {
                        val profile = minecraftProfile.profile
                        if (profile is RPKProfile) {
                            val group = groupProvider.getGroup(args[1])
                            if (group != null) {
                                if (sender.hasPermission("rpkit.permissions.command.group.add.${group.name}")) {
                                    groupProvider.addGroup(profile, group)
                                    sender.sendMessage(plugin.messages["group-add-valid", mapOf(
                                            Pair("group", group.name),
                                            Pair("player", minecraftProfile.minecraftUsername)
                                    )])
                                } else {
                                    sender.sendMessage(plugin.messages["no-permission-group-add-group", mapOf(
                                            Pair("group", group.name)
                                    )])
                                }
                            } else {
                                sender.sendMessage(plugin.messages["group-add-invalid-group"])
                            }
                        } else {
                            sender.sendMessage(plugin.messages["no-profile"])
                        }
                    } else {
                        sender.sendMessage(plugin.messages["no-minecraft-profile"])
                    }
                } else {
                    sender.sendMessage(plugin.messages["group-add-invalid-player"])
                }
            } else {
                sender.sendMessage(plugin.messages["group-add-usage"])
            }
        } else {
            sender.sendMessage(plugin.messages["no-permission-group-add"])
        }
        return true
    }

}