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
import com.rpkit.chat.bukkit.snooper.RPKSnooperService
import com.rpkit.core.service.Services
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs
import java.util.concurrent.CompletableFuture

/**
 * Snoop component.
 * Sends message to snoopers if the message would not be sent to them otherwise.
 */
@SerializableAs("SnoopComponent")
class SnoopComponent : DirectedPostFormatPipelineComponent, ConfigurationSerializable {

    override fun process(context: DirectedPostFormatMessageContext): CompletableFuture<DirectedPostFormatMessageContext> {
        if (!context.isCancelled) return CompletableFuture.completedFuture(context)
        val snooperService = Services[RPKSnooperService::class.java] ?: return CompletableFuture.completedFuture(context)
        // Since there's no mutation happening here we should be able to just send the messages.
        // In practice most, if not all servers will have the component as the last one (or at least after any mutations)
        // There might be some weirdness if mutations to context happen afterwards since context is mutable,
        // The speed benefit of not blocking the post-format pipeline is probably better though.
        snooperService.snoopers.thenAccept { snoopers ->
            if (snoopers.contains(context.receiverMinecraftProfile)) {
                context.receiverMinecraftProfile.sendMessage(*context.message)
            }
        }
        return CompletableFuture.completedFuture(context)
    }

    override fun serialize(): MutableMap<String, Any> {
        return mutableMapOf()
    }

    companion object {
        @JvmStatic
        fun deserialize(serialized: MutableMap<String, Any>): SnoopComponent {
            return SnoopComponent()
        }
    }

}