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

package com.rpkit.chat.bukkit.discord

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.chatchannel.RPKChatChannelProvider
import com.rpkit.chat.bukkit.chatchannel.undirected.DiscordComponent
import com.rpkit.chat.bukkit.discord.command.DiscordCommand
import com.rpkit.chat.bukkit.discord.command.DiscordListCommand
import com.rpkit.players.bukkit.profile.RPKDiscordProfileProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.priv.react.PrivateMessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent.*
import org.bukkit.ChatColor

class DiscordServer(
        private val plugin: RPKChatBukkit,
        val guildName: String
): ListenerAdapter() {

    private val jda = JDABuilder.create(
            plugin.config.getString("discord.token"),
            listOf(
                    GUILD_MEMBERS,
                    GUILD_PRESENCES,
                    GUILD_VOICE_STATES,
                    GUILD_EMOJIS,
                    GUILD_MESSAGES,
                    DIRECT_MESSAGES,
                    DIRECT_MESSAGE_REACTIONS
            )
    ).build()

    private var ready = false

    private val commands = mapOf<String, DiscordCommand>(
            "list" to DiscordListCommand(plugin)
    )

    init {
        jda.addEventListener(this)
    }

    override fun onReady(event: ReadyEvent) {
        ready = true
        plugin.logger.info("Connected to Discord")
    }

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        val author = event.author
        if (author == jda.selfUser) return
        val message = event.message.contentStripped
        if (message.startsWith("rpk!")) {
            val messageParts = message.split(" ")
            val commandName = messageParts[0].replaceFirst("rpk!", "")
            val command = commands[commandName]
            if (command != null) {
                command.execute(event.channel, author, command, commandName, messageParts.drop(1))
            } else {
                event.channel.sendMessage("Invalid command: $commandName").queue()
            }
        } else {
            val discordProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKDiscordProfileProvider::class)
            val discordProfile = discordProfileProvider.getDiscordProfile(author)
            val profile = discordProfile.profile
            val chatChannelProvider = plugin.core.serviceManager.getServiceProvider(RPKChatChannelProvider::class)
            val chatChannel = chatChannelProvider.getChatChannelFromDiscordChannel(event.channel.name)
            if (profile != null) {
                chatChannel?.sendMessage(
                        profile,
                        null,
                        message,
                        chatChannel.directedPipeline,
                        chatChannel.undirectedPipeline.filter { it !is DiscordComponent },
                        true
                )
            } else {
                if (chatChannel != null) {
                    event.message.delete().queue()
                    author.openPrivateChannel().queue { privateChannel ->
                        privateChannel.sendMessage("In order to send messages in this channel, you must first link your Discord profile to your RPKit profile.").queue()
                    }
                }
            }
        }
    }

    override fun onPrivateMessageReactionAdd(event: PrivateMessageReactionAddEvent) {
        if (event.user == jda.selfUser) return
        if (event.reaction.reactionEmote.emoji != "\u2705") return
        val messageId = event.messageId
        val discordProvider = plugin.core.serviceManager.getServiceProvider(RPKDiscordProvider::class)
        val profile = discordProvider.getMessageProfileLink(messageId) ?: return
        val discordProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKDiscordProfileProvider::class)
        val user = event.user ?: return
        val discordProfile = discordProfileProvider.getDiscordProfile(user)
        discordProfile.profile = profile
        discordProfileProvider.updateDiscordProfile(discordProfile)
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        user.openPrivateChannel().queue { privateChannel ->
            privateChannel.sendMessage("Your Discord profile has been successfully linked to ${profile.name}.").queue()
            minecraftProfileProvider.getMinecraftProfiles(profile)
                    .filter{ minecraftProfile -> minecraftProfile.isOnline }
                    .forEach { minecraftProfile ->
                minecraftProfile.sendMessage(plugin.messages["account-link-discord-successful", mapOf(
                        "discord-tag" to user.asTag
                )])
            }
        }
    }

    fun sendMessage(channel: String, message: String) {
        if (!ready) return
        jda.getGuildsByName(guildName, false).forEach { guild ->
            guild.getTextChannelsByName(channel, false).forEach { channel ->
                channel.sendMessage(
                        ChatColor.stripColor(message)!!
                ).queue()
            }
        }
    }

    fun getUser(userName: String): User? {
        return jda.getUserByTag(userName)
    }

}