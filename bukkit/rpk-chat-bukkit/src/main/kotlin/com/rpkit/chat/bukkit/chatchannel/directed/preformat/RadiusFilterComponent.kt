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

package com.rpkit.chat.bukkit.chatchannel.directed.preformat

import com.rpkit.chat.bukkit.chatchannel.pipeline.DirectedPreFormatPipelineComponent
import com.rpkit.chat.bukkit.context.DirectedPreFormatMessageContext
import org.bukkit.Bukkit
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs
import java.util.concurrent.CompletableFuture

/**
 * Radius filter component.
 * Filters out messages if they are beyond the radius of the channel.
 */
@SerializableAs("RadiusFilterComponent")
class RadiusFilterComponent : DirectedPreFormatPipelineComponent, ConfigurationSerializable {

    override fun process(context: DirectedPreFormatMessageContext): CompletableFuture<DirectedPreFormatMessageContext> {
        if (context.isCancelled) return CompletableFuture.completedFuture(context)
        val senderMinecraftProfile = context.senderMinecraftProfile
        if (senderMinecraftProfile != null) {
            val senderBukkitPlayer = Bukkit.getOfflinePlayer(senderMinecraftProfile.minecraftUUID)
            val receiverBukkitPlayer = Bukkit.getOfflinePlayer(context.receiverMinecraftProfile.minecraftUUID)
            val senderBukkitOnlinePlayer = senderBukkitPlayer.player
            val receiverBukkitOnlinePlayer = receiverBukkitPlayer.player
            if (senderBukkitOnlinePlayer != null && receiverBukkitOnlinePlayer != null) {
                val senderLocation = senderBukkitOnlinePlayer.location
                val receiverLocation = receiverBukkitOnlinePlayer.location
                if (senderLocation.world == receiverLocation.world) {
                    if (senderLocation.distanceSquared(receiverLocation) > context.chatChannel.radius * context.chatChannel.radius) {
                        context.isCancelled = true
                    }
                    // If there is a radius filter in place, the only situation where the message is not cancelled
                    // is both players having a Minecraft player online AND the players being in the same world,
                    // within the chat channel's radius.
                    // If any of these conditions fail, the message is cancelled, otherwise it maintains the same
                    // cancelled state as before reaching the component.
                } else {
                    context.isCancelled = true
                }
            } else {
                context.isCancelled = true
            }
        } else {
            context.isCancelled = true
        }
        return CompletableFuture.completedFuture(context)
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