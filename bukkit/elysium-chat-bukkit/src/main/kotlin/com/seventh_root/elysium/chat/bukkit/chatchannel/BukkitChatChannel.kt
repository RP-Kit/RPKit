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
import com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline.BukkitLogChatChannelPipelineComponent
import com.seventh_root.elysium.players.bukkit.BukkitPlayer
import java.awt.Color
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class BukkitChatChannel private constructor(
        private val plugin: ElysiumChatBukkit,
        override var id: Int,
        override var name: String,
        override var color: Color,
        formatString: String,
        radius: Int,
        clearRadius: Int,
        override val speakers: MutableList<ElysiumPlayer>,
        override val listeners: MutableList<ElysiumPlayer>,
        override val pipeline: MutableList<ChatChannelPipelineComponent>,
        override var matchPattern: String,
        ircEnabled: Boolean,
        ircChannel: String,
        ircWhitelist: Boolean,
        override var isJoinedByDefault: Boolean
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

    class Builder(private val plugin: ElysiumChatBukkit) {

        private var id: Int = 0
        private var name: String? = null
        private var color: Color
        private var formatString: String? = null
        private var radius: Int = 0
        private var clearRadius: Int = 0
        private val speakers: MutableList<ElysiumPlayer>
        private val listeners: MutableList<ElysiumPlayer>
        private val pipeline: MutableList<ChatChannelPipelineComponent>
        private var matchPattern: String = ""
        private var ircEnabled: Boolean = false
        private var ircChannel: String? = null
        private var ircWhitelist: Boolean = false
        private var joinedByDefault: Boolean = false

        init {
            this.id = 0
            this.name = ""
            this.color = Color.WHITE
            this.formatString = "&f[\$color\$channel&f] &7\$sender-player&f: \$message"
            this.radius = -1
            this.clearRadius = -1
            this.speakers = ArrayList<ElysiumPlayer>()
            this.listeners = ArrayList<ElysiumPlayer>()
            this.pipeline = ArrayList<ChatChannelPipelineComponent>()
            this.matchPattern = ""
            this.ircEnabled = false
            this.ircChannel = ""
            this.ircWhitelist = false
            this.joinedByDefault = true
        }

        fun id(id: Int): Builder {
            this.id = id
            return this
        }

        fun name(name: String): Builder {
            this.name = name
            return this
        }

        fun color(color: Color): Builder {
            this.color = color
            return this
        }

        fun formatString(formatString: String): Builder {
            this.formatString = formatString
            return this
        }

        fun radius(radius: Int): Builder {
            this.radius = radius
            return this
        }

        fun clearRadius(clearRadius: Int): Builder {
            this.clearRadius = clearRadius
            return this
        }

        fun speaker(speaker: ElysiumPlayer): Builder {
            this.speakers.add(speaker)
            return this
        }

        fun speakers(speakers: Collection<ElysiumPlayer>): Builder {
            this.speakers.clear()
            this.speakers.addAll(speakers)
            return this
        }

        fun listener(listener: ElysiumPlayer): Builder {
            this.listeners.add(listener)
            return this
        }

        fun listeners(listeners: Collection<ElysiumPlayer>): Builder {
            this.listeners.clear()
            this.listeners.addAll(listeners)
            return this
        }

        fun pipelineComponent(component: ChatChannelPipelineComponent): Builder {
            this.pipeline.add(component)
            return this
        }

        fun pipeline(pipeline: Collection<ChatChannelPipelineComponent>): Builder {
            this.pipeline.clear()
            this.pipeline.addAll(pipeline)
            return this
        }

        fun matchPattern(matchPattern: String): Builder {
            this.matchPattern = matchPattern
            return this
        }

        fun ircEnabled(ircEnabled: Boolean): Builder {
            this.ircEnabled = ircEnabled
            return this
        }

        fun ircChannel(ircChannel: String): Builder {
            this.ircChannel = ircChannel
            return this
        }

        fun ircWhitelist(ircWhitelist: Boolean): Builder {
            this.ircWhitelist = ircWhitelist
            return this
        }

        fun joinedByDefault(joinedByDefault: Boolean): Builder {
            this.joinedByDefault = joinedByDefault
            return this
        }

        fun build(): BukkitChatChannel {
            if (radius > 0 && clearRadius > 0) {
                pipelineComponent(BukkitGarbleChatChannelPipelineComponent(clearRadius.toDouble()))
            }
            pipelineComponent(BukkitFormatChatChannelPipelineComponent(plugin, formatString))
            if (ircEnabled && radius <= 0 && ircChannel!!.trim { it <= ' ' } != "") {
                pipelineComponent(BukkitIRCChatChannelPipelineComponent(ircChannel, ircWhitelist))
            }
            pipelineComponent(BukkitLogChatChannelPipelineComponent())
            return BukkitChatChannel(
                    plugin,
                    id,
                    name!!,
                    color,
                    formatString!!,
                    radius,
                    clearRadius,
                    speakers,
                    listeners,
                    pipeline,
                    matchPattern,
                    ircEnabled,
                    ircChannel!!,
                    ircWhitelist,
                    joinedByDefault)
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
