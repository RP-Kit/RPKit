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

import com.rpkit.chat.bukkit.irc.RPKIRCService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.profile.RPKIRCProfileImpl
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.irc.IRCNick
import com.rpkit.players.bukkit.profile.irc.RPKIRCProfileService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Account link IRC command.
 * Links an IRC account to the current player.
 */
class AccountLinkIRCCommand(private val plugin: RPKPlayersBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        if (!sender.hasPermission("rpkit.players.command.account.link.irc")) {
            sender.sendMessage(plugin.messages["no-permission-account-link-irc"])
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages["account-link-irc-usage"])
            return true
        }
        val nick = IRCNick(args[0])
        val ircService = Services[RPKIRCService::class.java]
        if (ircService == null) {
            sender.sendMessage(plugin.messages["no-irc-service"])
            return true
        }
        if (!ircService.isOnline(nick)) {
            sender.sendMessage(plugin.messages["account-link-irc-invalid-nick"])
            return true
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-service"])
            return true
        }
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(sender)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-self"])
            return true
        }
        val profile = minecraftProfile.profile
        if (profile !is RPKProfile) {
            sender.sendMessage(plugin.messages["account-link-irc-invalid-profile"])
            return true
        }
        val ircProfileService = Services[RPKIRCProfileService::class.java]
        if (ircProfileService == null) {
            sender.sendMessage(plugin.messages["no-irc-profile-service"])
            return true
        }
        var ircProfile = ircProfileService.getIRCProfile(nick)
        if (ircProfile != null) {
            sender.sendMessage(plugin.messages["account-link-irc-invalid-already-linked"])
            return true
        }
        ircProfile = RPKIRCProfileImpl(
                profile = profile,
                nick = nick
        )
        ircProfileService.addIRCProfile(ircProfile)
        sender.sendMessage(plugin.messages["account-link-irc-valid"])
        return true
    }
}