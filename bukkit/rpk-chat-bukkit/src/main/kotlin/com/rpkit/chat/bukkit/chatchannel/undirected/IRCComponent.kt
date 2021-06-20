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

package com.rpkit.chat.bukkit.chatchannel.undirected

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.chatchannel.pipeline.UndirectedPipelineComponent
import com.rpkit.chat.bukkit.context.UndirectedMessageContext
import com.rpkit.chat.bukkit.irc.IRCChannel
import com.rpkit.chat.bukkit.irc.RPKIRCService
import com.rpkit.core.service.Services
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs
import java.util.concurrent.CompletableFuture

/**
 * IRC component.
 * Sends message to IRC channel.
 */
@SerializableAs("IRCComponent")
class IRCComponent(
        private val plugin: RPKChatBukkit,
        val ircChannel: IRCChannel,
        val isIRCWhitelisted: Boolean
) : UndirectedPipelineComponent, ConfigurationSerializable {

    override fun process(context: UndirectedMessageContext): CompletableFuture<UndirectedMessageContext> {
        if (!context.isCancelled) {
            val ircService = Services[RPKIRCService::class.java] ?: return CompletableFuture.completedFuture(context)
            if (ircService.isConnected) {
                ircService.sendMessage(ircChannel, ChatColor.stripColor(context.message)!!)
            }
        }
        return CompletableFuture.completedFuture(context)
    }

    override fun serialize(): MutableMap<String, Any> {
        return mutableMapOf(
                "irc-channel" to ircChannel.name,
                "irc-whitelisted" to isIRCWhitelisted
        )
    }

    companion object {
        @JvmStatic
        fun deserialize(serialized: MutableMap<String, Any>): IRCComponent {
            return IRCComponent(
                    Bukkit.getPluginManager().getPlugin("rpk-chat-bukkit") as RPKChatBukkit,
                    IRCChannel(serialized["irc-channel"] as String),
                    serialized["irc-whitelisted"] as Boolean
            )
        }
    }

}
