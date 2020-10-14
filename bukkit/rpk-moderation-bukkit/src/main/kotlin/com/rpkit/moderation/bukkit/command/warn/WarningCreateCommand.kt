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
import com.rpkit.moderation.bukkit.warning.RPKWarningImpl
import com.rpkit.moderation.bukkit.warning.RPKWarningService
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileService
import com.rpkit.players.bukkit.profile.RPKProfile
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.time.format.DateTimeFormatter


class WarningCreateCommand(private val plugin: RPKModerationBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.moderation.command.warning.create")) {
            sender.sendMessage(plugin.messages["no-permission-warning-create"])
            return true
        }
        if (args.size < 2) {
            sender.sendMessage(plugin.messages["warning-create-usage"])
            return true
        }
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        val targetPlayer = plugin.server.getPlayer(args[0])
        if (targetPlayer == null) {
            sender.sendMessage(plugin.messages["warning-create-invalid-target"])
            return true
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-service"])
            return true
        }
        val targetMinecraftProfile = minecraftProfileService.getMinecraftProfile(targetPlayer)
        if (targetMinecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile"])
            return true
        }
        val issuerMinecraftProfile = minecraftProfileService.getMinecraftProfile(sender)
        if (issuerMinecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile"])
            return true
        }
        val targetProfile = targetMinecraftProfile.profile
        if (targetProfile !is RPKProfile) {
            sender.sendMessage(plugin.messages["no-profile"])
            return true
        }
        val issuerProfile = issuerMinecraftProfile.profile
        if (issuerProfile !is RPKProfile) {
            sender.sendMessage(plugin.messages["no-profile"])
            return true
        }
        val warningReason = args.drop(1).joinToString(" ")
        val warningService = Services[RPKWarningService::class]
        if (warningService == null) {
            sender.sendMessage(plugin.messages["no-warning-service"])
            return true
        }
        val warning = RPKWarningImpl(warningReason, targetProfile, issuerProfile)
        warningService.addWarning(warning)
        sender.sendMessage(plugin.messages["warning-create-valid", mapOf(
                Pair("reason", warningReason),
                Pair("issuer", issuerProfile.name),
                Pair("profile", targetProfile.name),
                Pair("time", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(warning.time)),
                Pair("index", warningService.getWarnings(targetProfile).size.toString())
        )])
        targetPlayer.sendMessage(plugin.messages["warning-received", mapOf(
                Pair("reason", warningReason),
                Pair("issuer", issuerProfile.name),
                Pair("profile", targetProfile.name),
                Pair("time", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(warning.time)),
                Pair("index", warningService.getWarnings(targetProfile).size.toString())
        )])
        return true
    }


}