package com.seventh_root.elysium.chat.bukkit.chatchannel

import com.seventh_root.elysium.api.chat.ChatChannelPipelineComponent
import com.seventh_root.elysium.api.chat.ChatMessageContext
import com.seventh_root.elysium.api.chat.ElysiumChatChannel
import com.seventh_root.elysium.api.chat.exception.ChatChannelMessageFormattingFailureException
import com.seventh_root.elysium.api.player.ElysiumPlayer
import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit
import com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline.BukkitFormatChatChannelPipelineComponent
import com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline.BukkitGarbleChatChannelPipelineComponent
import com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline.BukkitIRCChatChannelPipelineComponent
import com.seventh_root.elysium.players.bukkit.BukkitPlayer
import java.awt.Color
import java.awt.Color.WHITE
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class BukkitChatChannel constructor(
        private val plugin: ElysiumChatBukkit,
        override var id: Int = 0,
        override var name: String = "",
        override var color: Color = WHITE,
        formatString: String = "&f[\$color\$channel&f] &7\$sender-player&f: \$message",
        radius: Int = -1,
        clearRadius: Int = -1,
        override val speakers: MutableList<ElysiumPlayer> = mutableListOf<ElysiumPlayer>(),
        override val listeners: MutableList<ElysiumPlayer> = mutableListOf<ElysiumPlayer>(),
        override val pipeline: MutableList<ChatChannelPipelineComponent> = mutableListOf<ChatChannelPipelineComponent>(),
        override var matchPattern: String = "",
        ircEnabled: Boolean = false,
        ircChannel: String = "",
        ircWhitelist: Boolean = false,
        override var isJoinedByDefault: Boolean = true
) : ElysiumChatChannel {

    override var formatString: String = formatString
        set(formatString) {
            this.formatString = formatString
            pipeline.filter { pipelineComponent -> pipelineComponent is BukkitFormatChatChannelPipelineComponent }
                    .map { pipelineComponent -> pipelineComponent as BukkitFormatChatChannelPipelineComponent }
                    .forEach { pipelineComponent -> pipelineComponent.formatString = formatString }
        }
    override var radius: Int = radius
        set(radius) {
            this.radius = radius
            if (clearRadius > 0 && radius > 0) {
                pipeline.filter { pipelineComponent -> pipelineComponent is BukkitGarbleChatChannelPipelineComponent }
                        .map { pipelineComponent -> pipelineComponent as BukkitGarbleChatChannelPipelineComponent }
                        .forEach { pipelineComponent -> pipelineComponent.clearRange = clearRadius.toDouble() }
            } else {
                val pipelineIterator = pipeline.listIterator()
                while (pipelineIterator.hasNext()) {
                    val pipelineComponent = pipelineIterator.next()
                    if (pipelineComponent is BukkitGarbleChatChannelPipelineComponent) {
                        pipelineIterator.remove()
                    }
                }
            }
        }
    override var clearRadius: Int = clearRadius
        set(clearRadius) {
            this.clearRadius = clearRadius
            if (clearRadius > 0 && radius > 0) {
                pipeline.filter { pipelineComponent -> pipelineComponent is BukkitGarbleChatChannelPipelineComponent }
                        .map { pipelineComponent -> pipelineComponent as BukkitGarbleChatChannelPipelineComponent }
                        .forEach { pipelineComponent -> pipelineComponent.clearRange = clearRadius.toDouble() }
            } else {
                val pipelineIterator = pipeline.iterator()
                while (pipelineIterator.hasNext()) {
                    val pipelineComponent = pipelineIterator.next()
                    if (pipelineComponent is BukkitGarbleChatChannelPipelineComponent) {
                        pipelineIterator.remove()
                    }
                }
            }
        }
    override var isIRCEnabled: Boolean = ircEnabled
        set(ircEnabled) {
            this.isIRCEnabled = ircEnabled
            if (ircEnabled) {
                pipeline.add(BukkitIRCChatChannelPipelineComponent(ircChannel, isIRCWhitelist))
                pipeline.sort()
            } else {
                val pipelineIterator = pipeline.iterator()
                while (pipelineIterator.hasNext()) {
                    val pipelineComponent = pipelineIterator.next()
                    if (pipelineComponent is BukkitIRCChatChannelPipelineComponent) {
                        pipelineIterator.remove()
                    }
                }
            }
        }
    override var ircChannel: String? = ircChannel
        set(ircChannel) {
            this.ircChannel = ircChannel
            if (isIRCEnabled) {
                pipeline.filter { pipelineComponent -> pipelineComponent is BukkitIRCChatChannelPipelineComponent }
                        .map { pipelineComponent -> pipelineComponent as BukkitIRCChatChannelPipelineComponent }
                        .forEach { pipelineComponent -> pipelineComponent.ircChannel = ircChannel }
            }
        }
    override var isIRCWhitelist: Boolean = ircWhitelist
        set(ircWhitelist) {
            this.isIRCWhitelist = ircWhitelist
            if (isIRCEnabled) {
                pipeline.filter { pipelineComponent -> pipelineComponent is BukkitIRCChatChannelPipelineComponent }
                        .map { pipelineComponent -> pipelineComponent as BukkitIRCChatChannelPipelineComponent }
                        .forEach { pipelineComponent -> pipelineComponent.isWhitelist = ircWhitelist }
            }
        }

    override fun addSpeaker(speaker: ElysiumPlayer) {
        if (!speakers.contains(speaker))
            speakers.add(speaker)
    }

    override fun removeSpeaker(speaker: ElysiumPlayer) {
        while (speakers.contains(speaker)) {
            speakers.remove(speaker)
        }
    }

    override fun addListener(listener: ElysiumPlayer) {
        if (!listeners.contains(listener))
            listeners.add(listener)
    }

    override fun removeListener(listener: ElysiumPlayer) {
        while (listeners.contains(listener)) {
            listeners.remove(listener)
        }
    }

    override fun processMessage(message: String?, context: ChatMessageContext): String? {
        var processedMessage = message
        for (pipelineComponent in pipeline) {
            if (processedMessage == null) break
            try {
                processedMessage = pipelineComponent.process(processedMessage, context)
            } catch (exception: ChatChannelMessageFormattingFailureException) {
                val elysiumPlayer = context.sender
                if (elysiumPlayer is BukkitPlayer) {
                    val bukkitPlayer = elysiumPlayer.bukkitPlayer
                    if (bukkitPlayer.isOnline) {
                        bukkitPlayer.player.sendMessage(exception.message)
                    }
                }
                break
            }

        }
        return processedMessage
    }

    @Throws(IOException::class)
    override fun log(message: String) {
        val logDirectory = File(plugin.dataFolder, "logs")
        val logDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val datedLogDirectory = File(logDirectory, logDateFormat.format(Date()))
        if (!datedLogDirectory.exists()) {
            if (!datedLogDirectory.mkdirs())
                throw IOException("Could not create log directory. Does the server have permission to write to the directory?")
        }
        val log = File(datedLogDirectory, name + ".log")
        if (!log.exists()) {
            try {
                if (!log.createNewFile())
                    throw IOException("Failed to create log file. Does the server have permission to write to the directory?")
            } catch (exception: IOException) {
                exception.printStackTrace()
            }

        }
        try {
            val writer = BufferedWriter(FileWriter(log, true))
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            writer.append("[").append(dateFormat.format(Date())).append("] ").append(message).append("\n")
            writer.close()
        } catch (exception: IOException) {
            exception.printStackTrace()
        }

    }

}
