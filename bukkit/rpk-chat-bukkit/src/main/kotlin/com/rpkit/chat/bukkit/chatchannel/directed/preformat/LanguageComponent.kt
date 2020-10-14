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

package com.rpkit.chat.bukkit.chatchannel.directed.preformat

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.chat.bukkit.chatchannel.pipeline.DirectedPreFormatPipelineComponent
import com.rpkit.chat.bukkit.context.DirectedPreFormatMessageContext
import com.rpkit.core.service.Services
import com.rpkit.languages.bukkit.characterlanguage.RPKCharacterLanguageService
import com.rpkit.languages.bukkit.language.RPKLanguageService
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("LanguageComponent")
class LanguageComponent : DirectedPreFormatPipelineComponent, ConfigurationSerializable {

    override fun process(context: DirectedPreFormatMessageContext): DirectedPreFormatMessageContext {
        val characterService = Services[RPKCharacterService::class] ?: return context
        val senderMinecraftProfile = context.senderMinecraftProfile ?: return context
        val receiverMinecraftProfile = context.receiverMinecraftProfile
        var message = context.message
        val senderCharacter = characterService.getActiveCharacter(senderMinecraftProfile) ?: return context
        val receiverCharacter = characterService.getActiveCharacter(receiverMinecraftProfile) ?: return context
        val receiverRace = receiverCharacter.race
        if (!message.startsWith("[") || !message.contains("]")) return context
        val languageName = Regex("\\[([^]]+)]").find(message)?.groupValues?.get(1) ?: return context
        val languageService = Services[RPKLanguageService::class] ?: return context
        val language = languageService.getLanguage(languageName) ?: return context
        message = message.replaceFirst(
                "[$languageName] ",
                ""
        )
        val characterLanguageService = Services[RPKCharacterLanguageService::class] ?: return context
        val senderUnderstanding = characterLanguageService.getCharacterLanguageUnderstanding(senderCharacter, language)
        val receiverUnderstanding = characterLanguageService.getCharacterLanguageUnderstanding(receiverCharacter, language)
        context.message = "[${language.name}] ${language.apply(message, senderUnderstanding, receiverUnderstanding)}"
        if (receiverRace != null) {
            characterLanguageService.setCharacterLanguageUnderstanding(
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
        @JvmStatic
        fun deserialize(serialized: MutableMap<String, Any>): LanguageComponent {
            return LanguageComponent()
        }
    }

}