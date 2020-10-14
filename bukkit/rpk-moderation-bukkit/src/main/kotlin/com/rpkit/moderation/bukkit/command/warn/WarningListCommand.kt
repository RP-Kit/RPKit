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

import com.rpkit.core.service.Services
import com.rpkit.moderation.bukkit.RPKModerationBukkit
import com.rpkit.moderation.bukkit.warning.RPKWarningService
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileService
import com.rpkit.players.bukkit.profile.RPKProfile
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.time.format.DateTimeFormatter

class WarningListCommand(private val plugin: RPKModerationBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.moderation.command.warning.list")) {
            sender.sendMessage(plugin.messages["no-permission-warning-list"])
            return true
        }
        var player: Player? = null
        if (sender is Player) {
            player = sender
        }
        if (args.isNotEmpty()) {
            val argPlayer = plugin.server.getPlayer(args[0])
            if (argPlayer != null) {
                player = argPlayer
            }
        }
        if (player == null) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-service"])
            return true
        }
        val warningService = Services[RPKWarningService::class]
        if (warningService == null) {
            sender.sendMessage(plugin.messages["no-warning-service"])
            return true
        }
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(player)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile"])
            return true
        }
        val profile = minecraftProfile.profile
        if (profile !is RPKProfile) {
            sender.sendMessage(plugin.messages["no-profile"])
            return true
        }
        val warnings = warningService.getWarnings(profile)
        sender.sendMessage(plugin.messages["warning-list-title"])
        for ((index, warning) in warnings.withIndex()) {
            sender.sendMessage(plugin.messages["warning-list-item", mapOf(
                    Pair("issuer", warning.issuer.name),
                    Pair("profile", warning.profile.name),
                    Pair("index", (index + 1).toString()),
                    Pair("reason", warning.reason),
                    Pair("time", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(warning.time))
            )])
        }
        return true
    }

}
