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
import com.rpkit.chat.bukkit.servlet.websocket.RPKChatWebSocketProvider
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.configuration.serialization.ConfigurationSerializable


class WebComponent(private val plugin: RPKChatBukkit): UndirectedPipelineComponent, ConfigurationSerializable {
    override fun process(context: UndirectedMessageContext): UndirectedMessageContext {
        if (context.isCancelled) return context
        plugin.core.serviceManager.getServiceProvider(RPKChatWebSocketProvider::class).sockets
                .filter { socket -> socket.value.session?.isOpen == true }
                .forEach { socket -> socket.value.session?.remote
                        ?.sendStringByFuture("${context.chatChannel.name}:::${ChatColor.stripColor(context.message)}") }
        return context
    }

    override fun serialize(): MutableMap<String, Any> {
        return mutableMapOf()
    }

    companion object {
        @JvmStatic
        fun deserialize(serialized: MutableMap<String, Any>): WebComponent {
            return WebComponent(
                    Bukkit.getPluginManager().getPlugin("rpk-chat-bukkit") as RPKChatBukkit
            )
        }
    }
}