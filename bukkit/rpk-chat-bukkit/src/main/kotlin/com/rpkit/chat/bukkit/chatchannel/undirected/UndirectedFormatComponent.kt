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

package com.rpkit.chat.bukkit.chatchannel.undirected

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.chat.bukkit.chatchannel.pipeline.UndirectedPipelineComponent
import com.rpkit.chat.bukkit.context.UndirectedMessageContext
import com.rpkit.chat.bukkit.prefix.RPKPrefixService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKProfile
import net.md_5.bungee.api.ChatColor
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("UndirectedFormatComponent")
class UndirectedFormatComponent(var formatString: String) : UndirectedPipelineComponent, ConfigurationSerializable {

    override fun process(context: UndirectedMessageContext): UndirectedMessageContext {
        val characterService = Services[RPKCharacterService::class.java]
        val prefixService = Services[RPKPrefixService::class.java]
        val senderMinecraftProfile = context.senderMinecraftProfile
        val senderProfile = context.senderProfile
        val senderCharacter = if (senderMinecraftProfile != null)
            characterService?.getActiveCharacter(senderMinecraftProfile)
        else
            null
        val chatChannel = context.chatChannel
        var formattedMessage = ChatColor.translateAlternateColorCodes('&', formatString)
        if (formattedMessage.contains("\$message")) {
            formattedMessage = formattedMessage.replace("\$message", context.message)
        }
        if (formattedMessage.contains("\$sender-prefix")) {
            formattedMessage = if (senderProfile is RPKProfile) {
                formattedMessage.replace("\$sender-prefix", prefixService?.getPrefix(senderProfile) ?: "")
            } else {
                formattedMessage.replace("\$sender-prefix", "")
            }
        }
        if (formattedMessage.contains("\$sender-player")) {
            formattedMessage = formattedMessage.replace("\$sender-player", senderProfile.name)
        }
        if (formattedMessage.contains("\$sender-character")) {
            if (senderCharacter != null) {
                formattedMessage = formattedMessage.replace("\$sender-character", if (senderCharacter.isNameHidden) "(HIDDEN ${senderCharacter.name.hashCode()})" else senderCharacter.name)
            } else {
                context.isCancelled = true
            }
        }
        if (formattedMessage.contains("\$channel")) {
            formattedMessage = formattedMessage.replace("\$channel", chatChannel.name)
        }
        if (formattedMessage.contains("\$color") || formattedMessage.contains("\$colour")) {
            val chatColorString = ChatColor.of(chatChannel.color).toString()
            formattedMessage = formattedMessage.replace("\$color", chatColorString).replace("\$colour", chatColorString)
        }
        context.message = formattedMessage
        return context
    }

    override fun serialize(): MutableMap<String, Any> {
        return mutableMapOf(
                Pair("format", formatString)
        )
    }

    companion object {
        @JvmStatic
        fun deserialize(serialized: MutableMap<String, Any>): UndirectedFormatComponent {
            return UndirectedFormatComponent(
                    serialized["format"] as String
            )
        }
    }

}