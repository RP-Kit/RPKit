/*
 * Copyright 2022 Ren Binden
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

package com.rpkit.chat.bukkit.irc

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.chatchannel.RPKChatChannelService
import com.rpkit.chat.bukkit.chatchannel.undirected.IRCComponent
import com.rpkit.core.service.Services
import com.rpkit.permissions.bukkit.group.hasPermission
import com.rpkit.permissions.bukkit.permissions.RPKPermissionsService
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileName
import com.rpkit.players.bukkit.profile.RPKProfileService
import com.rpkit.players.bukkit.profile.irc.RPKIRCNick
import com.rpkit.players.bukkit.profile.irc.RPKIRCProfileService
import org.pircbotx.Channel
import org.pircbotx.PircBotX
import org.pircbotx.User
import java.util.concurrent.CompletableFuture
import java.util.logging.Level

class IRCWhitelistValidator(private val plugin: RPKChatBukkit) {

    fun enforceWhitelist(user: User, nick: RPKIRCNick, bot: PircBotX, channel: Channel) {
        val verified = user.isVerified
        val chatChannelService = Services[RPKChatChannelService::class.java] ?: return
        val chatChannel = chatChannelService.getChatChannelFromIRCChannel(IRCChannel(channel.name)) ?: return
        if (chatChannel.undirectedPipeline
                .firstNotNullOfOrNull { component -> component as? IRCComponent }
                ?.isIRCWhitelisted != true) return
        if (!verified) {
            kick(
                bot,
                channel,
                nick.value,
                "${channel.name} is whitelisted, but you are not verified.",
                "${nick.value} attempted to join, but was not verified."
            )
        }
        val permissionsService = Services[RPKPermissionsService::class.java]
        if (permissionsService == null) {
            kick(
                bot,
                channel,
                nick.value,
                "${channel.name} is whitelisted, but the permissions service could not be found.",
                "${nick.value} attempted to join, but no permissions service could be found and the channel is whitelisted."
            )
            return
        }
        val profileService = Services[RPKProfileService::class.java]
        if (profileService == null) {
            kick(
                bot,
                channel,
                nick.value,
                "${channel.name} is whitelisted, but the profile service could not be found.",
                "${nick.value} attempted to join, but no profile service could be found and the channel is whitelisted."
            )
            return
        }
        val ircProfileService = Services[RPKIRCProfileService::class.java]
        if (ircProfileService == null) {
            kick(
                bot,
                channel,
                nick.value,
                "${channel.name} is whitelisted, but the IRC profile service could not be found.",
                "${nick.value} attempted to join, but no IRC profile service could be found and the channel is whitelisted."
            )
            return
        }
        CompletableFuture.runAsync {
            val ircProfile = ircProfileService.getIRCProfile(nick).join() ?: ircProfileService.createIRCProfile(
                profileService.createThinProfile(RPKProfileName(nick.value)),
                nick
            ).join()
            val profile = ircProfile.profile
            if (profile !is RPKProfile) {
                kick(
                    bot,
                    channel,
                    nick.value,
                    "${channel.name} is whitelisted, but this IRC account has not been linked to a profile.",
                    "${nick.value} attempted to join, but their IRC account was not linked to a profile."
                )
                return@runAsync
            }
            if (!profile.hasPermission("rpkit.chat.listen.${chatChannel.name.value}").join()) {
                kick(
                    bot,
                    channel,
                    nick.value,
                    "${channel.name} is whitelisted, you do not have permission to view it.",
                    "${nick.value} attempted to join, but does not have permission to view the channel."
                )
            }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to check IRC profile", exception)
            throw exception
        }
    }

    private fun kick(
        bot: PircBotX,
        channel: Channel,
        nick: String,
        kickMessage: String,
        channelMessage: String
    ) {
        bot.sendIRC().message(channel.name, "/kick ${channel.name} $nick $kickMessage")
        channel.send().message(channelMessage)
    }

}