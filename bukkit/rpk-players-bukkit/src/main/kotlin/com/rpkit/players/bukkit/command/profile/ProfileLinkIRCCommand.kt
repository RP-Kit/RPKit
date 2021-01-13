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

package com.rpkit.players.bukkit.command.profile

import com.rpkit.chat.bukkit.irc.RPKIRCService
import com.rpkit.core.command.RPKCommandExecutor
import com.rpkit.core.command.result.CommandFailure
import com.rpkit.core.command.result.CommandResult
import com.rpkit.core.command.result.CommandSuccess
import com.rpkit.core.command.result.IncorrectUsageFailure
import com.rpkit.core.command.result.MissingServiceFailure
import com.rpkit.core.command.result.NoPermissionFailure
import com.rpkit.core.command.sender.RPKCommandSender
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.command.result.NoProfileSelfFailure
import com.rpkit.players.bukkit.command.result.NotAPlayerFailure
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileService
import com.rpkit.players.bukkit.profile.irc.RPKIRCNick
import com.rpkit.players.bukkit.profile.irc.RPKIRCProfile
import com.rpkit.players.bukkit.profile.irc.RPKIRCProfileService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile

/**
 * Account link IRC command.
 * Links an IRC account to the current player.
 */
class ProfileLinkIRCCommand(private val plugin: RPKPlayersBukkit) : RPKCommandExecutor {

    class InvalidIRCNickFailure : CommandFailure()
    class IRCProfileAlreadyLinkedFailure(val ircProfile: RPKIRCProfile) : CommandFailure()

    override fun onCommand(sender: RPKCommandSender, args: Array<out String>): CommandResult {
        if (sender !is RPKMinecraftProfile) {
            sender.sendMessage(plugin.messages.notFromConsole)
            return NotAPlayerFailure()
        }
        if (!sender.hasPermission("rpkit.players.command.profile.link.irc")) {
            sender.sendMessage(plugin.messages.noPermissionProfileLinkIrc)
            return NoPermissionFailure("rpkit.players.command.profile.link.irc")
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages.profileLinkIrcUsage)
            return IncorrectUsageFailure()
        }
        val nick = RPKIRCNick(args[0])
        val ircService = Services[RPKIRCService::class.java]
        if (ircService == null) {
            sender.sendMessage(plugin.messages.noIrcService)
            return MissingServiceFailure(RPKIRCService::class.java)
        }
        if (!ircService.isOnline(nick)) {
            sender.sendMessage(plugin.messages.profileLinkIrcInvalidNick)
            return InvalidIRCNickFailure()
        }
        val profile = sender.profile
        if (profile !is RPKProfile) {
            sender.sendMessage(plugin.messages.noProfileSelf)
            return NoProfileSelfFailure()
        }
        val profileService = Services[RPKProfileService::class.java]
        if (profileService == null) {
            sender.sendMessage(plugin.messages.noProfileService)
            return MissingServiceFailure(RPKProfileService::class.java)
        }
        val ircProfileService = Services[RPKIRCProfileService::class.java]
        if (ircProfileService == null) {
            sender.sendMessage(plugin.messages.noIrcProfileService)
            return MissingServiceFailure(RPKIRCProfileService::class.java)
        }
        val ircProfile = ircProfileService.getIRCProfile(nick) ?: null
        if (ircProfile != null && ircProfile.profile is RPKProfile) {
            sender.sendMessage(plugin.messages.profileLinkIrcInvalidAlreadyLinked)
            return IRCProfileAlreadyLinkedFailure(ircProfile)
        }
        if (ircProfile == null) {
            ircProfileService.createIRCProfile(profile, nick)
        } else {
            ircProfile.profile = profile
            ircProfileService.updateIRCProfile(ircProfile)
        }
        sender.sendMessage(plugin.messages.profileLinkIrcValid)
        return CommandSuccess
    }
}