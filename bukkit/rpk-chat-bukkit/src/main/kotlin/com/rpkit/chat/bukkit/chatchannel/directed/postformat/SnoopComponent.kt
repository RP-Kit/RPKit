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

package com.rpkit.chat.bukkit.chatchannel.directed.postformat

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.chatchannel.pipeline.DirectedPostFormatPipelineComponent
import com.rpkit.chat.bukkit.context.DirectedPostFormatMessageContext
import com.rpkit.chat.bukkit.snooper.RPKSnooperProvider
import org.bukkit.Bukkit
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

/**
 * Snoop component.
 * Sends message to snoopers if the message would not be sent to them otherwise.
 */
@SerializableAs("SnoopComponent")
class SnoopComponent(private val plugin: RPKChatBukkit): DirectedPostFormatPipelineComponent, ConfigurationSerializable {

    override fun process(context: DirectedPostFormatMessageContext): DirectedPostFormatMessageContext {
        if (!context.isCancelled) return context
        val snooperProvider = plugin.core.serviceManager.getServiceProvider(RPKSnooperProvider::class)
        if (snooperProvider.snoopers.contains(context.receiverMinecraftProfile)) {
            context.receiverMinecraftProfile.sendMessage(*context.message)
        }
        return context
    }

    override fun serialize(): MutableMap<String, Any> {
        return mutableMapOf()
    }

    companion object {
        @JvmStatic
        fun deserialize(serialized: MutableMap<String, Any>): SnoopComponent {
            return SnoopComponent(Bukkit.getPluginManager().getPlugin("rpk-chat-bukkit") as RPKChatBukkit)
        }
    }

}