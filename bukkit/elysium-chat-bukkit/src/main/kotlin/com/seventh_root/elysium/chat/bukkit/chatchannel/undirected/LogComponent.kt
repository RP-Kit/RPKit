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

package com.seventh_root.elysium.chat.bukkit.chatchannel.undirected

import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit
import com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline.UndirectedChatChannelPipelineComponent
import com.seventh_root.elysium.chat.bukkit.context.UndirectedChatChannelMessageContext
import org.bukkit.Bukkit
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@SerializableAs("LogComponent")
class LogComponent(private val plugin: ElysiumChatBukkit): UndirectedChatChannelPipelineComponent, ConfigurationSerializable {

    override fun process(context: UndirectedChatChannelMessageContext): UndirectedChatChannelMessageContext {
        val logDirectory = File(plugin.dataFolder, "logs")
        val logDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val datedLogDirectory = File(logDirectory, logDateFormat.format(Date()))
        if (!datedLogDirectory.exists()) {
            if (!datedLogDirectory.mkdirs())
                throw IOException("Could not create log directory. Does the server have permission to write to the directory?")
        }
        val log = File(datedLogDirectory, context.chatChannel.name + ".log")
        if (!log.exists()) {
            if (!log.createNewFile())
                throw IOException("Failed to create log file. Does the server have permission to write to the directory?")
        }
        val writer = BufferedWriter(FileWriter(log, true))
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        writer.append("[").append(dateFormat.format(Date())).append("] ").append(context.message).append("\n")
        writer.close()
        return context
    }

    override fun serialize(): MutableMap<String, Any> {
        return mutableMapOf()
    }

    companion object {
        @JvmStatic
        fun deserialize(serialized: MutableMap<String, Any>): LogComponent {
            return LogComponent(Bukkit.getPluginManager().getPlugin("elysium-chat-bukkit") as ElysiumChatBukkit)
        }
    }

}