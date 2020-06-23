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

package com.rpkit.chat.bukkit.chatchannel.directed

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.chatchannel.pipeline.DirectedChatChannelPipelineComponent
import com.rpkit.chat.bukkit.context.DirectedChatChannelMessageContext
import com.rpkit.chat.bukkit.snooper.RPKSnooperProvider
import com.rpkit.core.util.MathUtils
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs
import java.util.*

/**
 * Garble component.
 * Garbles messages based on the distance between the sender and receiver of the message.
 */
@SerializableAs("GarbleComponent")
class GarbleComponent(private val plugin: RPKChatBukkit, var clearRadius: Double): DirectedChatChannelPipelineComponent, ConfigurationSerializable {

    override fun process(context: DirectedChatChannelMessageContext): DirectedChatChannelMessageContext {
        if (context.isCancelled) return context // Don't bother garbling if the receiver won't receive anyway
        val senderMinecraftProfile = context.senderMinecraftProfile ?: return context // Prevent garble if the message wasn't sent from Minecraft
        val receiverMinecraftProfile = context.receiverMinecraftProfile
        val snooperProvider = plugin.core.serviceManager.getServiceProvider(RPKSnooperProvider::class)
        if (snooperProvider.isSnooping(receiverMinecraftProfile)) return context // Prevent garble if the receiver is snooping
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
                    Bukkit.getPluginManager().getPlugin("rpk-chat-bukkit") as RPKChatBukkit,
                    (serialized["clear-radius"] as Int).toDouble()
            )
        }
    }

}