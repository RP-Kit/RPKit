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

package com.rpkit.chat.bukkit.irc

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.irc.command.IRCListCommand
import com.rpkit.chat.bukkit.irc.command.IRCRegisterCommand
import com.rpkit.chat.bukkit.irc.command.IRCVerifyCommand
import com.rpkit.chat.bukkit.irc.listener.*
import com.rpkit.players.bukkit.profile.irc.RPKIRCNick
import com.rpkit.players.bukkit.profile.irc.RPKIRCProfile
import org.bukkit.scheduler.BukkitRunnable
import org.pircbotx.Configuration
import org.pircbotx.PircBotX
import org.pircbotx.delay.StaticDelay

/**
 * IRC service implementation.
 */
class RPKIRCServiceImpl(override val plugin: RPKChatBukkit) : RPKIRCService {

    private val ircBot: PircBotX
    private val onlineUsers = mutableListOf<String>()

    init {
        val whitelistValidator = IRCWhitelistValidator()
        val configuration = Configuration.Builder()
                .setAutoNickChange(true)
                .setCapEnabled(true)
                .addListener(IRCChannelJoinListener(whitelistValidator))
                .addListener(IRCChannelQuitListener())
                .addListener(IRCConnectListener())
                .addListener(IRCMessageListener(plugin))
                .addListener(IRCUserListListener(whitelistValidator))
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
        if (plugin.config.getString("irc.server")?.contains(":") == true) {
            val serverAddress = plugin.config.getString("irc.server")?.split(":")?.get(0)
            val serverPort = plugin.config.getString("irc.server")?.split(":")?.get(1)?.toIntOrNull()
            if (serverAddress != null && serverPort != null) {
                configuration.addServer(
                        serverAddress,
                        serverPort
                )
            }
        } else {
            val serverAddress = plugin.config.getString("irc.server")
            if (serverAddress != null) {
                configuration.addServer(serverAddress)
            }
        }
        if (plugin.config.get("irc.max-line-length") != null) {
            val maxLineLength = plugin.config.getInt("irc.max-line-length")
            configuration.maxLineLength = maxLineLength
        }
        if (plugin.config.get("irc.message-delay") != null) {
            val messageDelay = plugin.config.getLong("irc.message-delay")
            configuration.messageDelay = StaticDelay(messageDelay)
        }
        if (plugin.config.get("irc.password") != null) {
            val password = plugin.config.getString("irc.password")
            configuration.nickservPassword = password
        }

        ircBot = PircBotX(configuration.buildConfiguration())
        connect()
    }

    override val isConnected: Boolean
        get() = ircBot.isConnected

    override val nick: RPKIRCNick
        get() = RPKIRCNick(ircBot.nick)

    override fun sendMessage(channel: IRCChannel, message: String) {
        ircBot.send().message(channel.name, message)
    }

    override fun sendMessage(user: RPKIRCProfile, message: String) {
        sendMessage(user.nick, message)
    }

    override fun sendMessage(nick: RPKIRCNick, message: String) {
        ircBot.send().message(nick.value, message)
    }

    override fun isOnline(nick: RPKIRCNick): Boolean {
        return onlineUsers.contains(nick.value)
    }

    override fun setOnline(nick: RPKIRCNick, isOnline: Boolean) {
        if (isOnline) {
            if (!onlineUsers.contains(nick.value)) {
                onlineUsers.add(nick.value)
            }
        } else {
            if (onlineUsers.contains(nick.value)) {
                onlineUsers.remove(nick.value)
            }
        }
    }

    override fun joinChannel(ircChannel: IRCChannel) {
        ircBot.send().joinChannel(ircChannel.name)
    }

    override fun connect() {
        object : BukkitRunnable() {
            override fun run() {
                ircBot.startBot()
            }
        }.runTaskAsynchronously(plugin)
    }

    override fun disconnect() {
        ircBot.stopBotReconnect()
        ircBot.sendIRC()?.quitServer(plugin.messages["irc-quit"])
    }

}
