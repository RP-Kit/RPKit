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

package com.rpkit.chat.bukkit.command.snoop

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.snooper.RPKSnooperProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Snoop command.
 * Toggles snoop state.
 */
class SnoopCommand(private val plugin: RPKChatBukkit): CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val snooperProvider = plugin.core.serviceManager.getServiceProvider(RPKSnooperProvider::class)
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        if (sender is Player) {
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(sender)
            if (minecraftProfile != null) {
                if (args.isNotEmpty()) {
                    if (args[0].equals("on", ignoreCase = true)) {
                        if (sender.hasPermission("rpkit.chat.command.snoop.on")) {
                            if (!snooperProvider.snooperMinecraftProfiles.contains(minecraftProfile)) {
                                snooperProvider.addSnooper(minecraftProfile)
                                sender.sendMessage(plugin.messages["snoop-enabled"])
                            } else {
                                sender.sendMessage(plugin.messages["snoop-already-enabled"])
                            }
                        } else {
                            sender.sendMessage(plugin.messages["no-permission-snoop-on"])
                        }
                    } else if (args[0].equals("off", ignoreCase = true)) {
                        if (sender.hasPermission("rpkit.chat.command.snoop.off")) {
                            if (snooperProvider.snooperMinecraftProfiles.contains(minecraftProfile)) {
                                snooperProvider.removeSnooper(minecraftProfile)
                                sender.sendMessage(plugin.messages["snoop-disabled"])
                            } else {
                                sender.sendMessage(plugin.messages["snoop-already-disabled"])
                            }
                        } else {
                            sender.sendMessage(plugin.messages["no-permission-snoop-off"])
                        }
                    } else if (args[0].equals("check", ignoreCase = true)) {
                        if (sender.hasPermission("rpkit.chat.command.snoop.check")) {
                            if (snooperProvider.snooperMinecraftProfiles.contains(minecraftProfile)) {
                                sender.sendMessage(plugin.messages["snoop-check-on"])
                            } else {
                                sender.sendMessage(plugin.messages["snoop-check-off"])
                            }
                        } else {
                            sender.sendMessage(plugin.messages["no-permission-snoop-check"])
                        }
                    } else {
                        sender.sendMessage(plugin.messages["snoop-usage"])
                    }
                } else {
                    sender.sendMessage(plugin.messages["snoop-usage"])
                }
            } else {
                sender.sendMessage(plugin.messages["no-minecraft-profile"])
            }
        } else {
            sender.sendMessage(plugin.messages["not-from-console"])
        }
        return true
    }
}