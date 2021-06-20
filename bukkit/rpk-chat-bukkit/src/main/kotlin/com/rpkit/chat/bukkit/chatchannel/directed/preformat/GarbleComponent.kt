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
import com.rpkit.chat.bukkit.snooper.RPKSnooperService
import com.rpkit.core.service.Services
import com.rpkit.core.util.MathUtils
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * Garble component.
 * Garbles messages based on the distance between the sender and receiver of the message.
 */
@SerializableAs("GarbleComponent")
class GarbleComponent(var clearRadius: Double) : DirectedPreFormatPipelineComponent, ConfigurationSerializable {

    override fun process(context: DirectedPreFormatMessageContext): CompletableFuture<DirectedPreFormatMessageContext> {
        if (context.isCancelled) return CompletableFuture.completedFuture(context) // Don't bother garbling if the receiver won't receive anyway
        context.senderMinecraftProfile ?: return CompletableFuture.completedFuture(context) // Prevent garble if the message wasn't sent from Minecraft
        val receiverMinecraftProfile = context.receiverMinecraftProfile
        val snooperService = Services[RPKSnooperService::class.java]
        if (snooperService != null) {
            return snooperService.isSnooping(receiverMinecraftProfile).thenApply { isSnooping ->
                if (isSnooping) {
                    return@thenApply context // Prevent garble if the receiver is snooping
                } else {
                    return@thenApply applyGarble(context)
                }
            }
        } else {
            return CompletableFuture.completedFuture(applyGarble(context))
        }
    }

    fun applyGarble(context: DirectedPreFormatMessageContext): DirectedPreFormatMessageContext {
        val senderMinecraftProfile = context.senderMinecraftProfile
            ?: return context // Prevent garble if the message wasn't sent from Minecraft
        val receiverMinecraftProfile = context.receiverMinecraftProfile
        val senderOfflineBukkitPlayer = Bukkit.getOfflinePlayer(senderMinecraftProfile.minecraftUUID)
        val receiverOfflineBukkitPlayer = Bukkit.getOfflinePlayer(receiverMinecraftProfile.minecraftUUID)
        val senderBukkitPlayer = senderOfflineBukkitPlayer.player
        val receiverBukkitPlayer = receiverOfflineBukkitPlayer.player
        if (senderBukkitPlayer != null && receiverBukkitPlayer != null) {
            val distance = MathUtils.fastSqrt(senderBukkitPlayer.location.distanceSquared(receiverBukkitPlayer.location))
            val hearingRange = context.chatChannel.radius
            val clarity = 1.0 - (distance - clearRadius) / hearingRange
            context.message = garbleMessage(context.message, clarity)
        }
        return context
    }

    private fun garbleMessage(message: String, clarity: Double): String {
        val newMessage = StringBuilder()
        val random = Random()
        var i = 0
        var drops = 0
        while (i < message.length) {
            val c = message.codePointAt(i)
            i += Character.charCount(c)
            when {
                random.nextDouble() < clarity -> newMessage.appendCodePoint(c)
                random.nextDouble() < 0.1 -> {
                    newMessage.append(ChatColor.DARK_GRAY)
                    newMessage.appendCodePoint(c)
                    newMessage.append(ChatColor.WHITE)
                }
                else -> {
                    newMessage.append(' ')
                    drops++
                }
            }
        }
        if (drops == message.length) {
            return "~~~"
        }
        return newMessage.toString()
    }

    override fun serialize(): MutableMap<String, Any> {
        return mutableMapOf(
                Pair("clear-radius", clearRadius)
        )
    }

    companion object {
        @JvmStatic
        fun deserialize(serialized: MutableMap<String, Any>): GarbleComponent {
            return GarbleComponent(
                    (serialized["clear-radius"] as Int).toDouble()
            )
        }
    }

}