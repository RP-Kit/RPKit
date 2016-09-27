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

package com.seventh_root.elysium.chat.bukkit.chatchannel.directed

import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit
import com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline.DirectedChatChannelPipelineComponent
import com.seventh_root.elysium.chat.bukkit.context.DirectedChatChannelMessageContext
import com.seventh_root.elysium.chat.bukkit.snooper.ElysiumSnooperProvider
import org.bukkit.Bukkit
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("SnoopComponent")
class SnoopComponent(private val plugin: ElysiumChatBukkit): DirectedChatChannelPipelineComponent, ConfigurationSerializable {

    override fun process(context: DirectedChatChannelMessageContext): DirectedChatChannelMessageContext {
        if (!context.isCancelled) return context
        val snooperProvider = plugin.core.serviceManager.getServiceProvider(ElysiumSnooperProvider::class)
        if (snooperProvider.snoopers.contains(context.receiver)) {
            context.receiver.bukkitPlayer?.player?.sendMessage(context.message)
        }
        return context
    }

    override fun serialize(): MutableMap<String, Any> {
        return mutableMapOf()
    }

    companion object {
        @JvmStatic
        fun deserialize(serialized: MutableMap<String, Any>): SnoopComponent {
            return SnoopComponent(Bukkit.getPluginManager().getPlugin("elysium-chat-bukkit") as ElysiumChatBukkit)
        }
    }

}