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

package com.rpkit.players.bukkit.command.account

import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import com.rpkit.players.bukkit.profile.RPKProfile
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


class AccountConfirmLinkCommand(private val plugin: RPKPlayersBukkit): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        if (args.size <= 1) {
            sender.sendMessage(plugin.messages["account-confirm-link-usage"])
            return true
        }
        val type = args[0]
        val id = args[1].toIntOrNull()
        when (type.toLowerCase()) {
            "minecraft" -> {
                if (id == null) {
                    sender.sendMessage(plugin.messages["account-confirm-link-invalid-id"])
                    return true
                }
                val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
                val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(sender)
                if (minecraftProfile == null) {
                    sender.sendMessage(plugin.messages["no-minecraft-profile-self"])
                    return true
                }
                if (minecraftProfile.profile is RPKProfile) {
                    sender.sendMessage(plugin.messages["account-confirm-link-invalid-already-linked"])
                    return true
                }
                val linkRequests = minecraftProfileProvider.getMinecraftProfileLinkRequests(minecraftProfile)
                val linkRequest = linkRequests.firstOrNull { request -> request.profile.id == id }
                if (linkRequest == null) {
                    sender.sendMessage(plugin.messages["account-confirm-link-invalid-request"])
                    return true
                }
                minecraftProfile.profile = linkRequest.profile
                minecraftProfileProvider.updateMinecraftProfile(minecraftProfile)
                minecraftProfileProvider.removeMinecraftProfileLinkRequest(linkRequest)
                sender.sendMessage(plugin.messages["account-confirm-link-valid"])
                return true
            }
            else -> {
                sender.sendMessage(plugin.messages["account-confirm-link-invalid-type"])
                return true
            }
        }
    }

}