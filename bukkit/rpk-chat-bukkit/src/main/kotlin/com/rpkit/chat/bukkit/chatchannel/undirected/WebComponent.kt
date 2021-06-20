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

import com.rpkit.chat.bukkit.chatchannel.pipeline.UndirectedPipelineComponent
import com.rpkit.chat.bukkit.context.UndirectedMessageContext
import org.bukkit.configuration.serialization.ConfigurationSerializable
import java.util.concurrent.CompletableFuture


class WebComponent : UndirectedPipelineComponent, ConfigurationSerializable {
    override fun process(context: UndirectedMessageContext): CompletableFuture<UndirectedMessageContext> {
        if (context.isCancelled) return CompletableFuture.completedFuture(context)
//        Services[RPKChatWebSocketService::class.java].sockets
//                .filter { socket -> socket.value.session?.isOpen == true }
//                .forEach { socket ->
//                    socket.value.session?.remote
//                            ?.sendStringByFuture("${context.chatChannel.name}:::${ChatColor.stripColor(context.message)}")
//                }
        return CompletableFuture.completedFuture(context)
    }

    override fun serialize(): MutableMap<String, Any> {
        return mutableMapOf()
    }

    companion object {
        @JvmStatic
        fun deserialize(serialized: MutableMap<String, Any>): WebComponent {
            return WebComponent()
        }
    }
}