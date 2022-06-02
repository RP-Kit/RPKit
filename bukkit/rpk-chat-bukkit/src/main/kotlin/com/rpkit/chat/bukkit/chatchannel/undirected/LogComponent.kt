/*
 * Copyright 2022 Ren Binden
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

package com.rpkit.chat.bukkit.chatchannel.undirected

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.chatchannel.pipeline.UndirectedPipelineComponent
import com.rpkit.chat.bukkit.context.UndirectedMessageContext
import org.bukkit.Bukkit
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.CompletableFuture

/**
 * Log component.
 * Logs messages to a dated log file.
 */
@SerializableAs("LogComponent")
class LogComponent(private val plugin: RPKChatBukkit) : UndirectedPipelineComponent, ConfigurationSerializable {

    override fun process(context: UndirectedMessageContext): CompletableFuture<UndirectedMessageContext> {
        val logDirectory = File(plugin.dataFolder, "logs")
        val logDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val datedLogDirectory = File(logDirectory, logDateFormat.format(LocalDateTime.now()))
        if (!datedLogDirectory.exists()) {
            if (!datedLogDirectory.mkdirs())
                throw IOException("Could not create log directory. Does the server have permission to write to the directory?")
        }
        val log = File(datedLogDirectory, context.chatChannel.name.value + ".log")
        if (!log.exists()) {
            if (!log.createNewFile())
                throw IOException("Failed to create log file. Does the server have permission to write to the directory?")
        }
        val writer = BufferedWriter(FileWriter(log, true))
        val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        writer.append("[").append(dateFormat.format(LocalDateTime.now())).append("] ").append(context.message).append("\n")
        writer.close()
        return CompletableFuture.completedFuture(context)
    }

    override fun serialize(): MutableMap<String, Any> {
        return mutableMapOf()
    }

    companion object {
        @JvmStatic
        fun deserialize(serialized: MutableMap<String, Any>): LogComponent {
            return LogComponent(Bukkit.getPluginManager().getPlugin("rpk-chat-bukkit") as RPKChatBukkit)
        }
    }

}