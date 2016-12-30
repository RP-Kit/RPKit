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

package com.rpkit.chat.bukkit.chatchannel.undirected

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.chatchannel.pipeline.UndirectedChatChannelPipelineComponent
import com.rpkit.chat.bukkit.context.UndirectedChatChannelMessageContext
import com.rpkit.chat.bukkit.irc.RPKIRCProvider
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

/**
 * IRC component.
 * Sends message to IRC channel.
 */
@SerializableAs("IRCComponent")
class IRCComponent(
        private val plugin: RPKChatBukkit,
        val ircChannel: String,
        val isIRCWhitelisted: Boolean
): UndirectedChatChannelPipelineComponent, ConfigurationSerializable {

    override fun process(context: UndirectedChatChannelMessageContext): UndirectedChatChannelMessageContext {
        if (!context.isCancelled)
            plugin.core.serviceManager.getServiceProvider(RPKIRCProvider::class).ircBot.sendIRC().message(ircChannel, ChatColor.stripColor(context.message))
        return context
    }

    override fun serialize(): MutableMap<String, Any> {
        return mutableMapOf(
                Pair("irc-channel", ircChannel),
                Pair("irc-whitelisted", isIRCWhitelisted)
        )
    }

    companion object {
        @JvmStatic
        fun deserialize(serialized: MutableMap<String, Any>): IRCComponent {
            return IRCComponent(
                    Bukkit.getPluginManager().getPlugin("rpk-chat-bukkit") as RPKChatBukkit,
                    serialized["irc-channel"] as String,
                    serialized["irc-whitelisted"] as Boolean
            )
        }
    }

}
