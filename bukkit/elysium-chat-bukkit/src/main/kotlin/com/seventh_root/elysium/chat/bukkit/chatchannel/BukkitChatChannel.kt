package com.seventh_root.elysium.chat.bukkit.chatchannel

import com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline.ChatChannelPipelineComponent
import com.seventh_root.elysium.chat.bukkit.context.ChatMessageContext
import com.seventh_root.elysium.chat.bukkit.context.ChatMessagePostProcessContext
import com.seventh_root.elysium.chat.bukkit.chatchannel.ElysiumChatChannel
import com.seventh_root.elysium.chat.bukkit.exception.ChatChannelMessageFormattingFailureException
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayer
import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit
import com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline.BukkitFormatChatChannelPipelineComponent
import com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline.BukkitGarbleChatChannelPipelineComponent
import com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline.BukkitIRCChatChannelPipelineComponent
import com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline.BukkitLogChatChannelPipelineComponent
import com.seventh_root.elysium.players.bukkit.player.BukkitPlayer
import java.awt.Color
import java.awt.Color.WHITE
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class BukkitChatChannel: ElysiumChatChannel {

    private val plugin: ElysiumChatBukkit
    override var id: Int
    override var name: String
    override var color: Color
    override var formatString: String
        set(formatString) {
            field = formatString
            pipeline.filter { pipelineComponent -> pipelineComponent is BukkitFormatChatChannelPipelineComponent }
                    .map { pipelineComponent -> pipelineComponent as BukkitFormatChatChannelPipelineComponent }
                    .forEach { pipelineComponent -> pipelineComponent.formatString = formatString }
        }
    override var radius: Int
        set(radius) {
            field = radius
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
    override var clearRadius: Int
        set(clearRadius) {
            field = clearRadius
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
    override val speakers: MutableList<ElysiumPlayer>
    override val listeners: MutableList<ElysiumPlayer>
    override val pipeline: MutableList<ChatChannelPipelineComponent>
    override var matchPattern: String
    override var isIRCEnabled: Boolean
        set(ircEnabled) {
            field = ircEnabled
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
    override var ircChannel: String
        set(ircChannel) {
            field = ircChannel
            if (isIRCEnabled) {
                pipeline.filter { pipelineComponent -> pipelineComponent is BukkitIRCChatChannelPipelineComponent }
                        .map { pipelineComponent -> pipelineComponent as BukkitIRCChatChannelPipelineComponent }
                        .forEach { pipelineComponent -> pipelineComponent.ircChannel = ircChannel }
            }
        }
    override var isIRCWhitelist: Boolean
        set(ircWhitelist) {
            field = ircWhitelist
            if (isIRCEnabled) {
                pipeline.filter { pipelineComponent -> pipelineComponent is BukkitIRCChatChannelPipelineComponent }
                        .map { pipelineComponent -> pipelineComponent as BukkitIRCChatChannelPipelineComponent }
                        .forEach { pipelineComponent -> pipelineComponent.isWhitelist = ircWhitelist }
            }
        }
    override var isJoinedByDefault: Boolean

    constructor(
            plugin: ElysiumChatBukkit,
            id: Int = 0,
            name: String = "",
            color: Color = WHITE,
            formatString: String = "&f[\$color\$channel&f] &7\$sender-player&f: \$message",
            radius: Int = -1,
            clearRadius: Int = -1,
            speakers: MutableList<ElysiumPlayer> = mutableListOf<ElysiumPlayer>(),
            listeners: MutableList<ElysiumPlayer> = mutableListOf<ElysiumPlayer>(),
            pipeline: MutableList<ChatChannelPipelineComponent> = mutableListOf<ChatChannelPipelineComponent>(),
            matchPattern: String = "",
            isIRCEnabled: Boolean = false,
            ircChannel: String = "",
            isIRCWhitelist: Boolean = false,
            isJoinedByDefault: Boolean = true
    ) {
        this.plugin = plugin
        this.id = id
        this.name = name
        this.color = color
        this.pipeline = pipeline
        this.formatString = formatString
        this.radius = radius
        this.clearRadius = clearRadius
        this.speakers = speakers
        this.listeners = listeners
        this.matchPattern = matchPattern
        this.isIRCEnabled = isIRCEnabled
        this.ircChannel = ircChannel
        this.isIRCWhitelist = isIRCWhitelist
        this.isJoinedByDefault = isJoinedByDefault
        if (radius > 0 && clearRadius > 0) {
            pipeline.add(BukkitGarbleChatChannelPipelineComponent(clearRadius.toDouble()));
        }
        pipeline.add(BukkitFormatChatChannelPipelineComponent(plugin, formatString));
        if (isIRCEnabled && radius <= 0 && !ircChannel.trim().equals("")) {
            pipeline.add(BukkitIRCChatChannelPipelineComponent(ircChannel, isIRCWhitelist));
        }
        pipeline.add(BukkitLogChatChannelPipelineComponent());
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

    override fun postProcess(message: String?, context: ChatMessagePostProcessContext) {
        var processedMessage = message
        for (pipelineComponent in pipeline) {
            if (processedMessage == null) break
            try {
                processedMessage = pipelineComponent.postProcess(processedMessage, context)
            } catch (exception: ChatChannelMessageFormattingFailureException) {
                val elysiumPlayer = context.sender
                if (elysiumPlayer is BukkitPlayer) {
                    val bukkitPlayer = elysiumPlayer.bukkitPlayer
                    if (bukkitPlayer.isOnline) {
                        bukkitPlayer.player.sendMessage(exception.message)
                    }
                }
            }
        }
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
