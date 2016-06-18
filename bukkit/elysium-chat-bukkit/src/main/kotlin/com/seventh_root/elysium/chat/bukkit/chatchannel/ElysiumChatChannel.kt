package com.seventh_root.elysium.chat.bukkit.chatchannel

import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit
import com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline.*
import com.seventh_root.elysium.chat.bukkit.context.ChatMessageContext
import com.seventh_root.elysium.chat.bukkit.context.ChatMessagePostProcessContext
import com.seventh_root.elysium.chat.bukkit.database.table.ChatChannelListenerTable
import com.seventh_root.elysium.chat.bukkit.database.table.ChatChannelSpeakerTable
import com.seventh_root.elysium.chat.bukkit.exception.ChatChannelMessageFormattingFailureException
import com.seventh_root.elysium.core.database.TableRow
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayer
import java.awt.Color
import java.awt.Color.WHITE
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ElysiumChatChannel: TableRow {

    private val plugin: ElysiumChatBukkit
    override var id: Int
    var name: String
    var color: Color
    var formatString: String
        set(formatString) {
            field = formatString
            pipeline.filter { pipelineComponent -> pipelineComponent is FormatChatChannelPipelineComponent }
                    .map { pipelineComponent -> pipelineComponent as FormatChatChannelPipelineComponent }
                    .forEach { pipelineComponent -> pipelineComponent.formatString = formatString }
        }
    var radius: Int
        set(radius) {
            field = radius
            if (clearRadius > 0 && radius > 0) {
                pipeline.filter { pipelineComponent -> pipelineComponent is GarbleChatChannelPipelineComponent }
                        .map { pipelineComponent -> pipelineComponent as GarbleChatChannelPipelineComponent }
                        .forEach { pipelineComponent -> pipelineComponent.clearRange = clearRadius.toDouble() }
            } else {
                val pipelineIterator = pipeline.listIterator()
                while (pipelineIterator.hasNext()) {
                    val pipelineComponent = pipelineIterator.next()
                    if (pipelineComponent is GarbleChatChannelPipelineComponent) {
                        pipelineIterator.remove()
                    }
                }
            }
        }
    var clearRadius: Int
        set(clearRadius) {
            field = clearRadius
            if (clearRadius > 0 && radius > 0) {
                pipeline.filter { pipelineComponent -> pipelineComponent is GarbleChatChannelPipelineComponent }
                        .map { pipelineComponent -> pipelineComponent as GarbleChatChannelPipelineComponent }
                        .forEach { pipelineComponent -> pipelineComponent.clearRange = clearRadius.toDouble() }
            } else {
                val pipelineIterator = pipeline.iterator()
                while (pipelineIterator.hasNext()) {
                    val pipelineComponent = pipelineIterator.next()
                    if (pipelineComponent is GarbleChatChannelPipelineComponent) {
                        pipelineIterator.remove()
                    }
                }
            }
        }
    private val speakers0: MutableList<ElysiumPlayer>
    val speakers: List<ElysiumPlayer>
        get() = Collections.unmodifiableList(speakers0)
    private val listeners0: MutableList<ElysiumPlayer>
    val listeners: List<ElysiumPlayer>
        get() = Collections.unmodifiableList(listeners0)
    val pipeline: MutableList<ChatChannelPipelineComponent>
    var matchPattern: String
    var isIRCEnabled: Boolean
        set(ircEnabled) {
            field = ircEnabled
            if (ircEnabled) {
                pipeline.add(IRCChatChannelPipelineComponent(ircChannel, isIRCWhitelist))
                pipeline.sort()
            } else {
                val pipelineIterator = pipeline.iterator()
                while (pipelineIterator.hasNext()) {
                    val pipelineComponent = pipelineIterator.next()
                    if (pipelineComponent is IRCChatChannelPipelineComponent) {
                        pipelineIterator.remove()
                    }
                }
            }
        }
    var ircChannel: String
        set(ircChannel) {
            field = ircChannel
            if (isIRCEnabled) {
                pipeline.filter { pipelineComponent -> pipelineComponent is IRCChatChannelPipelineComponent }
                        .map { pipelineComponent -> pipelineComponent as IRCChatChannelPipelineComponent }
                        .forEach { pipelineComponent -> pipelineComponent.ircChannel = ircChannel }
            }
        }
    var isIRCWhitelist: Boolean
        set(ircWhitelist) {
            field = ircWhitelist
            if (isIRCEnabled) {
                pipeline.filter { pipelineComponent -> pipelineComponent is IRCChatChannelPipelineComponent }
                        .map { pipelineComponent -> pipelineComponent as IRCChatChannelPipelineComponent }
                        .forEach { pipelineComponent -> pipelineComponent.isWhitelist = ircWhitelist }
            }
        }
    var isJoinedByDefault: Boolean

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
        this.speakers0 = speakers
        this.listeners0 = listeners
        this.matchPattern = matchPattern
        this.isIRCEnabled = isIRCEnabled
        this.ircChannel = ircChannel
        this.isIRCWhitelist = isIRCWhitelist
        this.isJoinedByDefault = isJoinedByDefault
        if (radius > 0 && clearRadius > 0) {
            pipeline.add(GarbleChatChannelPipelineComponent(clearRadius.toDouble()));
        }
        pipeline.add(FormatChatChannelPipelineComponent(plugin, formatString));
        if (isIRCEnabled && radius <= 0 && !ircChannel.trim().equals("")) {
            pipeline.add(IRCChatChannelPipelineComponent(ircChannel, isIRCWhitelist));
        }
        pipeline.add(LogChatChannelPipelineComponent());
    }

    fun addSpeaker(speaker: ElysiumPlayer) {
        if (!speakers0.contains(speaker)) {
            speakers0.add(speaker)
            plugin.core.database.getTable(ChatChannelSpeaker::class.java)?.insert(ChatChannelSpeaker(0, this, speaker))
        }
    }

    fun addSpeaker0(speaker: ElysiumPlayer) {
        if (!speakers0.contains(speaker)) {
            speakers0.add(speaker)
        }
    }

    fun removeSpeaker(speaker: ElysiumPlayer) {
        while (speakers0.contains(speaker)) {
            speakers0.remove(speaker)
            val chatChannelSpeakerTable = plugin.core.database.getTable(ChatChannelSpeaker::class.java) as? ChatChannelSpeakerTable
            if (chatChannelSpeakerTable != null) {
                val chatChannelSpeaker = chatChannelSpeakerTable.get(speaker)
                if (chatChannelSpeaker != null) {
                    if (chatChannelSpeaker.chatChannel == this) {
                        chatChannelSpeakerTable.delete(chatChannelSpeaker)
                    }
                }
            }
        }
    }

    fun removeSpeaker0(speaker: ElysiumPlayer) {
        while (speakers0.contains(speaker)) {
            speakers0.remove(speaker)
        }
    }

    fun addListener(listener: ElysiumPlayer) {
        if (!listeners0.contains(listener)) {
            listeners0.add(listener)
            plugin.core.database.getTable(ChatChannelListener::class.java)?.insert(ChatChannelListener(0, this, listener))
        }
    }

    fun addListener0(listener: ElysiumPlayer) {
        if (!listeners0.contains(listener)) {
            listeners0.add(listener)
        }
    }

    fun removeListener(listener: ElysiumPlayer) {
        while (listeners0.contains(listener)) {
            listeners0.remove(listener)
            val chatChannelListenerTable = plugin.core.database.getTable(ChatChannelListener::class.java) as? ChatChannelListenerTable
            if (chatChannelListenerTable != null) {
                val chatChannelListeners = chatChannelListenerTable.get(listener)
                chatChannelListeners
                    .filter { chatChannelListener -> chatChannelListener.chatChannel == this }
                    .forEach { chatChannelListener -> chatChannelListenerTable.delete(chatChannelListener) }
            }
        }
    }

    fun removeListener0(listener: ElysiumPlayer) {
        while (listeners0.contains(listener)) {
            listeners0.remove(listener)
        }
    }

    fun processMessage(message: String?, context: ChatMessageContext): String? {
        var processedMessage = message
        for (pipelineComponent in pipeline) {
            if (processedMessage == null) break
            try {
                processedMessage = pipelineComponent.process(processedMessage, context)
            } catch (exception: ChatChannelMessageFormattingFailureException) {
                val elysiumPlayer = context.sender
                if (elysiumPlayer is ElysiumPlayer) {
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

    fun postProcess(message: String?, context: ChatMessagePostProcessContext) {
        var processedMessage = message
        for (pipelineComponent in pipeline) {
            if (processedMessage == null) break
            try {
                processedMessage = pipelineComponent.postProcess(processedMessage, context)
            } catch (exception: ChatChannelMessageFormattingFailureException) {
                val elysiumPlayer = context.sender
                if (elysiumPlayer is ElysiumPlayer) {
                    val bukkitPlayer = elysiumPlayer.bukkitPlayer
                    if (bukkitPlayer.isOnline) {
                        bukkitPlayer.player.sendMessage(exception.message)
                    }
                }
            }
        }
    }

    fun log(message: String) {
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
