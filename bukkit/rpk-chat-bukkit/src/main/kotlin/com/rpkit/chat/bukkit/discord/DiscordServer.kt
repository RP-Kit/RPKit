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

package com.rpkit.chat.bukkit.discord

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.chatchannel.RPKChatChannelService
import com.rpkit.chat.bukkit.chatchannel.undirected.DiscordComponent
import com.rpkit.chat.bukkit.discord.command.DiscordListCommand
import com.rpkit.chat.bukkit.discord.command.DiscordVersionCommand
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.discord.DiscordUserId
import com.rpkit.players.bukkit.profile.discord.RPKDiscordProfileService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.requests.GatewayIntent.*
import org.bukkit.ChatColor
import java.util.concurrent.ConcurrentHashMap

class DiscordServer(
        private val plugin: RPKChatBukkit,
        val guildName: String
) : ListenerAdapter() {

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

    private val commands = mapOf(
        "list" to DiscordListCommand(plugin),
        "version" to DiscordVersionCommand(plugin)
    )

    private val buttonListeners = ConcurrentHashMap<String, DiscordButtonClickListener>()

    init {
        jda.addEventListener(this)
        commands.forEach { (name, command) ->
            val jdaCommand = Commands.slash(name, command.description)
                .addOptions(command.options.toList())
            jda.upsertCommand(jdaCommand).queue()
        }
    }

    override fun onReady(event: ReadyEvent) {
        ready = true
        plugin.logger.info("Connected to Discord")
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val author = event.author
        if (author == jda.selfUser) return
        val message = event.message.contentStripped
        val discordProfileService = Services[RPKDiscordProfileService::class.java] ?: return
        discordProfileService.getDiscordProfile(DiscordUserId(author.idLong)).thenAccept { discordProfile ->
            val profile = discordProfile.profile
            val chatChannelService = Services[RPKChatChannelService::class.java] ?: return@thenAccept
            val chatChannel = chatChannelService.getChatChannelFromDiscordChannel(DiscordChannel(event.channel.idLong))
            chatChannel?.sendMessage(
                profile,
                null,
                message,
                chatChannel.directedPreFormatPipeline,
                chatChannel.format,
                chatChannel.directedPostFormatPipeline,
                chatChannel.undirectedPipeline.filter { it !is DiscordComponent },
                true
            )
        }
    }

    override fun onMessageReactionAdd(event: MessageReactionAddEvent) {
        if (event.user == jda.selfUser) return
        if (event.reaction.reactionEmote.emoji != "\u2705") return
        val messageId = event.messageIdLong
        val discordService = Services[RPKDiscordService::class.java] ?: return
        discordService.getMessageProfileLink(messageId).thenAccept getMessageProfileLink@{ profile ->
            if (profile == null) return@getMessageProfileLink
            val discordProfileService = Services[RPKDiscordProfileService::class.java] ?: return@getMessageProfileLink
            val user = event.user ?: return@getMessageProfileLink
            discordProfileService.getDiscordProfile(DiscordUserId(user.idLong)).thenAccept getDiscordProfile@{ discordProfile ->
                discordProfile.profile = profile
                discordProfileService.updateDiscordProfile(discordProfile)
                val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return@getDiscordProfile
                user.openPrivateChannel().queue { privateChannel ->
                    privateChannel.sendMessage("Your Discord profile has been successfully linked to ${profile.name.value}.").queue()
                    minecraftProfileService.getMinecraftProfiles(profile).thenAccept { minecraftProfiles ->
                        minecraftProfiles
                            .filter { minecraftProfile -> minecraftProfile.isOnline }
                            .forEach { minecraftProfile ->
                                minecraftProfile.sendMessage(plugin.messages["account-link-discord-successful", mapOf(
                                    "discord-tag" to user.asTag
                                )])
                            }
                    }
                }
            }
        }
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        val listener = buttonListeners[event.button.id]
        if (listener != null) {
            listener.onClick(object : DiscordButtonClickEvent {
                override fun reply(message: String) {
                    event.reply(message).queue()
                }
            })
        } else {
            event.reply("That action has expired, please try again.").queue()
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val commandName = event.name
        val command = commands[commandName]
        if (command != null) {
            command.execute(event.channel, event.user, command, commandName, event.options, event)
        } else {
            event.channel.sendMessage("Invalid command: $commandName").queue()
        }
    }

    fun sendMessage(channel: DiscordChannel, message: String) {
        if (!ready) return
        jda.getGuildsByName(guildName, false).forEach { guild ->
            guild.getTextChannelById(channel.id)?.sendMessage(
                ChatColor.stripColor(message)!!
            )?.queue()
        }
    }

    fun getUser(discordId: DiscordUserId): User? {
        return jda.getUserById(discordId.value)
    }

    fun getUser(userName: String): User? {
        return jda.getUserByTag(userName)
    }

    fun getChannel(name: String): DiscordChannel? {
        return jda.getGuildsByName(guildName, false)
                .flatMap { guild -> guild.getTextChannelsByName(name, false) }
                .firstOrNull()
                ?.idLong
                ?.let(::DiscordChannel)
    }

    fun disconnect() {
        jda.shutdown()
    }

    fun addButtonListener(id: String, listener: DiscordButtonClickListener) {
        buttonListeners[id] = listener
    }

}