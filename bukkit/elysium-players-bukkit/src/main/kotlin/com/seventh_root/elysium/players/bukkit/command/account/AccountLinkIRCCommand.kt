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

package com.seventh_root.elysium.players.bukkit.command.account

import com.seventh_root.elysium.chat.bukkit.irc.ElysiumIRCProvider
import com.seventh_root.elysium.core.exception.UnregisteredServiceException
import com.seventh_root.elysium.players.bukkit.ElysiumPlayersBukkit
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


class AccountLinkIRCCommand(private val plugin: ElysiumPlayersBukkit): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            if (args.size > 0) {
                val nick = args[0]
                try {
                    val ircProvider = plugin.core.serviceManager.getServiceProvider(ElysiumIRCProvider::class)
                    val ircUser = ircProvider.getIRCUser(nick)
                    if (ircUser != null) {
                        val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
                        val ircPlayer = playerProvider.getPlayer(ircUser)
                        if (ircPlayer.bukkitPlayer == null) {
                            val bukkitPlayer = playerProvider.getPlayer(sender)
                            bukkitPlayer.ircNick = ircUser.nick
                            playerProvider.updatePlayer(bukkitPlayer)
                            playerProvider.removePlayer(ircPlayer)
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.account-link-irc-valid")))
                        } else {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.account-link-irc-invalid-already-linked")))
                        }
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.account-link-irc-invalid-nick")))
                    }
                } catch (exception: UnregisteredServiceException) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.account-link-irc-invalid-no-irc-provider")))
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.account-link-irc-usage")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
        }
        return true
    }
}