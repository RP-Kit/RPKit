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
import com.seventh_root.elysium.core.util.MathUtils
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayer
import org.bukkit.ChatColor
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs
import java.util.*

@SerializableAs("GarbleComponent")
class GarbleComponent(var clearRadius: Double): DirectedChatChannelPipelineComponent, ConfigurationSerializable {

    override fun process(context: DirectedChatChannelMessageContext): DirectedChatChannelMessageContext {
        if (context.isCancelled) return context
        val sender = context.sender
        val receiver = context.receiver
        if (sender is ElysiumPlayer && receiver is ElysiumPlayer) {
            val senderOfflineBukkitPlayer = sender.bukkitPlayer
            val receiverOfflineBukkitPlayer = receiver.bukkitPlayer
            if (senderOfflineBukkitPlayer != null) {
                if (receiverOfflineBukkitPlayer != null) {
                    if (senderOfflineBukkitPlayer.isOnline && receiverOfflineBukkitPlayer.isOnline) {
                        val senderBukkitPlayer = senderOfflineBukkitPlayer.player
                        val receiverBukkitPlayer = receiverOfflineBukkitPlayer.player
                        if (senderBukkitPlayer.hasLineOfSight(receiverBukkitPlayer)) {
                            val distance = MathUtils.fastSqrt(senderBukkitPlayer.location.distanceSquared(receiverBukkitPlayer.location))
                            val hearingRange = context.chatChannel.radius.toDouble()
                            val clarity = 1.0 - (distance - clearRadius) / hearingRange
                            context.message = garbleMessage(context.message, clarity)
                        } else {
                            context.message = garbleMessage(context.message, 0.0)
                        }
                    }
                }
            }
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
            if (random.nextDouble() < clarity) {
                newMessage.appendCodePoint(c)
            } else if (random.nextDouble() < 0.1) {
                newMessage.append(ChatColor.DARK_GRAY)
                newMessage.appendCodePoint(c)
                newMessage.append(ChatColor.WHITE)
            } else {
                newMessage.append(' ')
                drops++
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
            return GarbleComponent((serialized["clear-radius"] as Int).toDouble())
        }
    }

}