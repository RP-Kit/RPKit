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

package com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline

import com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline.ChatChannelPipelineComponent.Type.PRE_PROCESSOR
import com.seventh_root.elysium.chat.bukkit.context.ChatMessageContext
import com.seventh_root.elysium.chat.bukkit.context.ChatMessagePostProcessContext
import com.seventh_root.elysium.core.util.MathUtils
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayer
import org.bukkit.ChatColor
import java.util.*

class GarbleChatChannelPipelineComponent(var clearRange: Double): ChatChannelPipelineComponent() {

    override val type: ChatChannelPipelineComponent.Type
        get() = PRE_PROCESSOR

    override fun process(message: String, context: ChatMessageContext): String {
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
                            val clarity = 1.0 - (distance - clearRange) / hearingRange
                            return garbleMessage(message, clarity)
                        } else {
                            return garbleMessage(message, 0.0)
                        }
                    }
                }
            }
        }
        return message
    }

    override fun postProcess(message: String, context: ChatMessagePostProcessContext): String? {
        return message
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

}
