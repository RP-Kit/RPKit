/*
 * Copyright 2016 Ross Binden
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

import com.rpkit.chat.bukkit.irc.RPKIRCProvider
import com.rpkit.core.exception.UnregisteredServiceException
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.profile.RPKIRCProfileImpl
import com.rpkit.players.bukkit.profile.RPKIRCProfileProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Account link IRC command.
 * Links an IRC account to the current player.
 */
class AccountLinkIRCCommand(private val plugin: RPKPlayersBukkit): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            if (args.isNotEmpty()) {
                val nick = args[0]
                try {
                    val ircProvider = plugin.core.serviceManager.getServiceProvider(RPKIRCProvider::class)
                    val ircUser = ircProvider.getIRCUser(nick)
                    if (ircUser != null) {
                        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
                        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(sender)
                        if (minecraftProfile != null) {
                            val profile = minecraftProfile.profile
                            if (profile != null) {
                                val ircProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKIRCProfileProvider::class)
                                var ircProfile = ircProfileProvider.getIRCProfile(ircUser)
                                if (ircProfile == null) {
                                    ircProfile = RPKIRCProfileImpl(
                                            profile = profile,
                                            nick = ircUser.nick
                                    )
                                    ircProfileProvider.addIRCProfile(ircProfile)
                                    sender.sendMessage(plugin.messages["account-link-irc-valid"])
                                } else {
                                    sender.sendMessage(plugin.messages["account-link-irc-invalid-already-linked"])
                                }
                            } else {
                                sender.sendMessage(plugin.messages["account-link-irc-invalid-profile"])
                            }
                        } else {
                            sender.sendMessage(plugin.messages["no-minecraft-profile"])
                        }
                    } else {
                        sender.sendMessage(plugin.messages["account-link-irc-invalid-nick"])
                    }
                } catch (exception: UnregisteredServiceException) {
                    sender.sendMessage(plugin.messages["account-link-irc-invalid-no-irc-provider"])
                }
            } else {
                sender.sendMessage(plugin.messages["account-link-irc-usage"])
            }
        } else {
            sender.sendMessage(plugin.messages["not-from-console"])
        }
        return true
    }
}