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

package com.rpkit.chat.bukkit.discord.command

import com.rpkit.chat.bukkit.RPKChatBukkit
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType.STRING
import net.dv8tion.jda.api.interactions.commands.build.OptionData

class DiscordVersionCommand(private val plugin: RPKChatBukkit) : DiscordCommand(
    "version",
    "Views the version of the server or a plugin",
    OptionData(
        STRING,
        "plugin",
        "The plugin to get the version of",
        false
    )
) {
    override fun execute(
        channel: MessageChannel,
        sender: User,
        command: DiscordCommand,
        label: String,
        args: List<OptionMapping>,
        event: SlashCommandInteractionEvent
    ) {
        val pluginName = args.singleOrNull { it.name == "plugin" }
        if (pluginName != null) {
            val pluginToShow = plugin.server.pluginManager.getPlugin(pluginName.asString)
            if (pluginToShow == null) {
                event.reply(plugin.messages.versionInvalidPlugin).queue()
                return
            }
            val response = StringBuilder()
            response.append(plugin.messages.pluginVersion.withParameters(
                name = pluginToShow.name,
                version = pluginToShow.description.version
            )).append("\n")
            val description = pluginToShow.description.description
            if (description != null) {
                response.append(plugin.messages.pluginDescription.withParameters(
                    description = description
                )).append("\n")
            }
            val website = pluginToShow.description.website
            if (website != null) {
                response.append(plugin.messages.pluginWebsite.withParameters(
                    website = website
                )).append("\n")
            }
            if (pluginToShow.description.authors.isNotEmpty()) {
                if (pluginToShow.description.authors.size == 1) {
                    response.append(plugin.messages.pluginAuthor.withParameters(
                        author = pluginToShow.description.authors.single()
                    )).append("\n")
                } else {
                    response.append(plugin.messages.pluginAuthors.withParameters(
                        authors = pluginToShow.description.authors
                    )).append("\n")
                }
            }
            if (pluginToShow.description.contributors.isNotEmpty()) {
                response.append(plugin.messages.pluginContributors.withParameters(
                    contributors = pluginToShow.description.contributors
                )).append("\n")
            }
            event.reply(response.toString()).queue()
        } else {
            event.reply(plugin.messages.serverVersion.withParameters(
                name = plugin.server.name,
                version = plugin.server.version,
                apiVersion = plugin.server.bukkitVersion
            )).queue()
        }
    }
}