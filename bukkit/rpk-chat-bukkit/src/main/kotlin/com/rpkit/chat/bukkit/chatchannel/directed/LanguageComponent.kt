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

package com.rpkit.chat.bukkit.chatchannel.directed

import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.chatchannel.pipeline.DirectedChatChannelPipelineComponent
import com.rpkit.chat.bukkit.context.DirectedChatChannelMessageContext
import com.rpkit.languages.bukkit.characterlanguage.RPKCharacterLanguageProvider
import com.rpkit.languages.bukkit.language.RPKLanguageProvider
import org.bukkit.Bukkit
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("LanguageComponent")
class LanguageComponent(private val plugin: RPKChatBukkit): DirectedChatChannelPipelineComponent, ConfigurationSerializable {

    override fun process(context: DirectedChatChannelMessageContext): DirectedChatChannelMessageContext {
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val senderMinecraftProfile = context.senderMinecraftProfile ?: return context
        val receiverMinecraftProfile = context.receiverMinecraftProfile
        var message = context.message
        val senderCharacter = characterProvider.getActiveCharacter(senderMinecraftProfile) ?: return context
        val receiverCharacter = characterProvider.getActiveCharacter(receiverMinecraftProfile) ?: return context
        val receiverRace = receiverCharacter.race
        if (!message.startsWith("[") || !message.contains("]")) return context
        val languageName = Regex("\\[([^]]+)]").find(message)?.groupValues?.get(1) ?: return context
        val languageProvider = plugin.core.serviceManager.getServiceProvider(RPKLanguageProvider::class)
        val language = languageProvider.getLanguage(languageName) ?: return context
        message = message.replaceFirst(
                "[$languageName] ",
                ""
        )
        val characterLanguageProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterLanguageProvider::class)
        val senderUnderstanding = characterLanguageProvider.getCharacterLanguageUnderstanding(senderCharacter, language)
        val receiverUnderstanding = characterLanguageProvider.getCharacterLanguageUnderstanding(receiverCharacter, language)
        context.message = "[${language.name}] ${language.apply(message, senderUnderstanding, receiverUnderstanding)}"
        if (receiverRace != null) {
            characterLanguageProvider.setCharacterLanguageUnderstanding(
                    receiverCharacter,
                    language,
                    receiverUnderstanding + language.randomUnderstandingIncrement(receiverRace))
        }
        return context
    }

    override fun serialize(): MutableMap<String, Any> {
        return mutableMapOf()
    }

    companion object {
        @JvmStatic fun deserialize(serialized: MutableMap<String, Any>): LanguageComponent {
            return LanguageComponent(Bukkit.getPluginManager().getPlugin("rpk-chat-bukkit") as RPKChatBukkit)
        }
    }

}