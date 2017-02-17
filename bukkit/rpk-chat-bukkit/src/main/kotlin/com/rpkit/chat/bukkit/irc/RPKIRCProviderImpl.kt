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

package com.rpkit.chat.bukkit.irc

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.irc.command.IRCListCommand
import com.rpkit.chat.bukkit.irc.command.IRCRegisterCommand
import com.rpkit.chat.bukkit.irc.command.IRCVerifyCommand
import com.rpkit.chat.bukkit.irc.listener.IRCChannelJoinListener
import com.rpkit.chat.bukkit.irc.listener.IRCConnectListener
import com.rpkit.chat.bukkit.irc.listener.IRCMessageListener
import com.rpkit.chat.bukkit.irc.listener.IRCUserListListener
import org.bukkit.scheduler.BukkitRunnable
import org.pircbotx.Configuration
import org.pircbotx.PircBotX
import org.pircbotx.User

/**
 * IRC provider implementation.
 */
class RPKIRCProviderImpl(private val plugin: RPKChatBukkit): RPKIRCProvider {

    override val ircBot: PircBotX
    private val ircUsers: MutableMap<String, User> = mutableMapOf()

    init {
        val configuration = Configuration.Builder()
                .setAutoNickChange(true)
                .setCapEnabled(true)
                .addListener(IRCChannelJoinListener(plugin))
                .addListener(IRCConnectListener(plugin))
                .addListener(IRCMessageListener(plugin))
                .addListener(IRCUserListListener(plugin))
                .addListener(IRCRegisterCommand(plugin))
                .addListener(IRCVerifyCommand(plugin))
                .addListener(IRCListCommand(plugin))
                .setAutoReconnect(true)
        if (plugin.config.get("irc.name") != null) {
            val name = plugin.config.getString("irc.name")
            configuration.name = name
        }
        if (plugin.config.get("irc.real-name") != null) {
            val realName = plugin.config.getString("irc.real-name")
            configuration.realName = realName
        }
        if (plugin.config.get("irc.login") != null) {
            val login = plugin.config.getString("irc.login")
            configuration.login = login
        }
        if (plugin.config.get("irc.cap-enabled") != null) {
            val capEnabled = plugin.config.getBoolean("irc.cap-enabled")
            configuration.isCapEnabled = capEnabled
        }
        if (plugin.config.get("irc.auto-nick-change-enabled") != null) {
            val autoNickChange = plugin.config.getBoolean("irc.auto-nick-change-enabled")
            configuration.isAutoNickChange = autoNickChange
        }
        if (plugin.config.get("irc.auto-split-message-enabled") != null) {
            val autoSplitMessage = plugin.config.getBoolean("irc.auto-split-message-enabled")
            configuration.isAutoSplitMessage = autoSplitMessage
        }
        if (plugin.config.getString("irc.server").contains(":")) {
            try {
                configuration.addServer(
                        plugin.config.getString("irc.server").split(":")[0], plugin.config.getString("irc.server").split(":")[1].toInt()
                )
            } catch (ignore: NumberFormatException) {
            }
        } else {
            configuration.addServer(plugin.config.getString("irc.server"))
        }
        if (plugin.config.get("irc.max-line-length") != null) {
            val maxLineLength = plugin.config.getInt("irc.max-line-length")
            configuration.maxLineLength = maxLineLength
        }
        if (plugin.config.get("irc.message-delay") != null) {
            val messageDelay = plugin.config.getLong("irc.message-delay")
            configuration.messageDelay = messageDelay
        }
        if (plugin.config.get("irc.password") != null) {
            val password = plugin.config.getString("irc.password")
            configuration.nickservPassword = password
        }
        ircBot = PircBotX(configuration.buildConfiguration())
        object: BukkitRunnable() {
            override fun run() {
                ircBot.startBot()
            }
        }.runTaskAsynchronously(plugin)
    }

    override fun getIRCUser(nick: String): User? {
        return ircUsers[nick]
    }

    override fun addIRCUser(user: User) {
        ircUsers[user.nick] = user
    }

}
