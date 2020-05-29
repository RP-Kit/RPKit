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

package com.rpkit.moderation.bukkit.command.warn

import com.rpkit.moderation.bukkit.RPKModerationBukkit
import com.rpkit.moderation.bukkit.warning.RPKWarningProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import com.rpkit.players.bukkit.profile.RPKProfile
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class WarningRemoveCommand(private val plugin: RPKModerationBukkit): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.moderation.command.warning.remove")) {
            sender.sendMessage(plugin.messages["no-permission-warning-remove"])
            return true
        }
        if (args.size < 2) {
            sender.sendMessage(plugin.messages["warning-remove-usage"])
            return true
        }
        val targetPlayer = plugin.server.getPlayer(args[0])
        if (targetPlayer == null) {
            sender.sendMessage(plugin.messages["warning-remove-invalid-target"])
            return true
        }
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val targetMinecraftProfile = minecraftProfileProvider.getMinecraftProfile(targetPlayer)
        if (targetMinecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile"])
            return true
        }
        val targetProfile = targetMinecraftProfile.profile
        if (targetProfile !is RPKProfile) {
            sender.sendMessage(plugin.messages["no-profile"])
            return true
        }
        val warningProvider = plugin.core.serviceManager.getServiceProvider(RPKWarningProvider::class)
        val warnings = warningProvider.getWarnings(targetProfile)
        try {
            val warningIndex = args[1].toInt()
            if (warningIndex > warnings.size) {
                sender.sendMessage(plugin.messages["warning-remove-invalid-index"])
                return true
            }
            val warning = warnings[warningIndex - 1]
            warningProvider.removeWarning(warning)
            sender.sendMessage(plugin.messages["warning-remove-valid"])
            return true
        } catch (exception: NumberFormatException) {
            sender.sendMessage(plugin.messages["warning-remove-invalid-index"])
            return true
        }
    }

}
