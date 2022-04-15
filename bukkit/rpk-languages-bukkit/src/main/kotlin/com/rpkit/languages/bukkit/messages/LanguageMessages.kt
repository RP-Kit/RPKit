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

package com.rpkit.languages.bukkit.messages

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.bukkit.message.BukkitMessages
import com.rpkit.core.message.ParameterizedMessage
import com.rpkit.core.message.to
import com.rpkit.languages.bukkit.RPKLanguagesBukkit
import com.rpkit.languages.bukkit.language.RPKLanguage
import org.bukkit.entity.Player
import java.text.DecimalFormat

class LanguageMessages(plugin: RPKLanguagesBukkit) : BukkitMessages(plugin) {

    class LanguageListItemMessage(private val message: ParameterizedMessage) {
        fun withParameters(language: RPKLanguage) = message.withParameters(
            "language" to language.name.value
        )
    }

    class LanguageListUnderstandingItemMessage(private val message: ParameterizedMessage) {
        fun withParameters(language: RPKLanguage, understanding: Float) = message.withParameters(
            "language" to language.name.value,
            "understanding" to understanding.toString()
        )
    }

    class LanguageListUnderstandingTitleMessage(private val message: ParameterizedMessage) {
        fun withParameters(player: Player, character: RPKCharacter) = message.withParameters(
            "player" to player.name,
            "character" to character.name,
        )
    }

    class LanguageSetUnderstandingValidMessage(private val message: ParameterizedMessage) {
        private val decimalFormat = DecimalFormat("#.####")
        fun withParameters(
            character: RPKCharacter,
            language: RPKLanguage,
            understanding: Float
        ) = message.withParameters(
            "character" to character.name,
            "language" to language.name.value,
            "understanding" to decimalFormat.format(understanding)
        )
    }

    val languageUsage = get("language-usage")
    val languageListTitle = get("language-list-title")
    val languageListItem = getParameterized("language-list-item").let(::LanguageListItemMessage)
    val languageListUnderstandingUsage = get("language-list-understanding-usage")
    val languageListUnderstandingTitle = getParameterized("language-list-understanding-title")
        .let(::LanguageListUnderstandingTitleMessage)
    val languageListUnderstandingItem = getParameterized("language-list-understanding-item")
        .let(::LanguageListUnderstandingItemMessage)
    val languageSetUnderstandingUsage = get("language-set-understanding-usage")
    val languageSetUnderstandingInvalidTarget = get("language-set-understanding-invalid-target")
    val languageSetUnderstandingInvalidLanguage = get("language-set-understanding-invalid-language")
    val languageSetUnderstandingInvalidUnderstanding = get("language-set-understanding-invalid-understanding")
    val languageSetUnderstandingValid = getParameterized("language-set-understanding-valid")
        .let(::LanguageSetUnderstandingValidMessage)
    val noCharacter = get("no-character")
    val noMinecraftProfile = get("no-minecraft-profile")
    val noCharacterLanguageService = get("no-character-language-service")
    val noCharacterService = get("no-character-service")
    val noMinecraftProfileService = get("no-minecraft-profile-service")
    val noPlayerFound = get("no-player-found")
    val noLanguageService = get("no-language-service")
    val noPermissionLanguageList = get("no-permission-language-list")
    val noPermissionLanguageListUnderstanding = get("no-permission-language-list-understanding")

}
