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

import com.rpkit.chat.bukkit.chatchannel.pipeline.DirectedPostFormatPipelineComponent
import com.rpkit.chat.bukkit.context.DirectedPostFormatMessageContext
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs
import java.util.concurrent.CompletableFuture

/**
 * Send message component.
 * Sends message to the receiver.
 */
@SerializableAs("SendMessageComponent")
class SendMessageComponent : DirectedPostFormatPipelineComponent, ConfigurationSerializable {

    override fun process(context: DirectedPostFormatMessageContext): CompletableFuture<DirectedPostFormatMessageContext> {
        if (!context.isCancelled)
            context.receiverMinecraftProfile.sendMessage(*context.message)
        return CompletableFuture.completedFuture(context)
    }

    override fun serialize(): MutableMap<String, Any> {
        return mutableMapOf()
    }

    companion object {
        @JvmStatic
        fun deserialize(serialized: MutableMap<String, Any>): SendMessageComponent {
            return SendMessageComponent()
        }
    }

}