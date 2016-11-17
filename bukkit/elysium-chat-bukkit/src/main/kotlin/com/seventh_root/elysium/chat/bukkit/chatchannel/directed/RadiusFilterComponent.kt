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

import com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline.DirectedChatChannelPipelineComponent
import com.seventh_root.elysium.chat.bukkit.context.DirectedChatChannelMessageContext
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

/**
 * Radius filter component.
 * Filters out messages if they are beyond the radius of the channel.
 */
@SerializableAs("RadiusFilterComponent")
class RadiusFilterComponent: DirectedChatChannelPipelineComponent, ConfigurationSerializable {

    override fun process(context: DirectedChatChannelMessageContext): DirectedChatChannelMessageContext {
        if (context.isCancelled) return context
        context.isCancelled = context.sender.bukkitPlayer?.player?.location
                ?.distanceSquared(context.receiver.bukkitPlayer?.player?.location)?:Double.MAX_VALUE > context.chatChannel.radius * context.chatChannel.radius
        return context
    }

    override fun serialize(): MutableMap<String, Any> {
        return mutableMapOf()
    }

    companion object {
        @JvmStatic
        fun deserialize(serialized: MutableMap<String, Any>): RadiusFilterComponent {
            return RadiusFilterComponent()
        }
    }

}