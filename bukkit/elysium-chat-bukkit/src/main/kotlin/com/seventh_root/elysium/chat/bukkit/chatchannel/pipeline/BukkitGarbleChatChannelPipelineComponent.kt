package com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline

import com.seventh_root.elysium.api.chat.ChatChannelPipelineComponent
import com.seventh_root.elysium.api.chat.ChatChannelPipelineComponent.Type.PRE_PROCESSOR
import com.seventh_root.elysium.api.chat.ChatMessageContext
import com.seventh_root.elysium.core.util.MathUtils
import com.seventh_root.elysium.players.bukkit.BukkitPlayer
import org.bukkit.ChatColor
import java.util.*

class BukkitGarbleChatChannelPipelineComponent(var clearRange: Double) : ChatChannelPipelineComponent() {

    override val type: ChatChannelPipelineComponent.Type
        get() = PRE_PROCESSOR

    override fun process(message: String, context: ChatMessageContext): String {
        val sender = context.sender
        val receiver = context.receiver
        if (sender is BukkitPlayer && receiver is BukkitPlayer) {
            val senderOfflineBukkitPlayer = sender.bukkitPlayer.player
            val receiverOfflineBukkitPlayer = receiver.bukkitPlayer.player
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
