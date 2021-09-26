package com.rpkit.languages.bukkit.messages

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.bukkit.message.BukkitMessages
import com.rpkit.core.message.ParameterizedMessage
import com.rpkit.core.message.to
import com.rpkit.languages.bukkit.RPKLanguagesBukkit
import com.rpkit.languages.bukkit.language.RPKLanguage
import org.bukkit.entity.Player

class LanguageMessages(plugin: RPKLanguagesBukkit) : BukkitMessages(plugin) {

    class ListCharacterLanguageUnderstandingItemMessage(private val message: ParameterizedMessage) {
        fun withParameters(language: RPKLanguage, understanding: Float) = message.withParameters(
            mapOf(
                Pair("language", language.name.value),
                Pair("understanding", understanding.toString())
            )
        )
    }

    class ListCharacterLanguageUnderstandingTitleMessage(private val message: ParameterizedMessage) {
        fun withParameters(player: Player, character: RPKCharacter) = message.withParameters(
            mapOf(
                Pair("player", player.name),
                Pair("character", character.name)
            )
        )
    }

    class LanguageListItemMessage(private val message: ParameterizedMessage) {
        fun withParameters(language: RPKLanguage) = message.withParameters(
            "language" to language.name.value
        )
    }

    val listCharacterLanguageUnderstandingItem = getParameterized("list-character-language-understanding-item")
        .let(::ListCharacterLanguageUnderstandingItemMessage)
    val listCharacterLanguageUnderstandingTitle = getParameterized("list-character-language-understanding-title")
        .let(::ListCharacterLanguageUnderstandingTitleMessage)
    val noCharacter = get("no-character")
    val noMinecraftProfile = get("no-minecraft-profile")
    val noCharacterLanguageService = get("no-character-language-service")
    val noCharacterService = get("no-character-service")
    val noMinecraftProfileService = get("no-minecraft-profile-service")
    val noPlayerFound = get("no-player-found")
    val listCharacterLanguageUnderstandingUsage = get("list-character-language-understanding-usage")
    val noPermissionListCharacterLanguageUnderstanding = get("no-permission-list-character-language-understanding")
    val languageUsage = get("language-usage")
    val noPermissionLanguageList = get("no-permission-language-list")
    val noLanguageService = get("no-language-service")
    val languageListTitle = get("language-list-title")
    val languageListItem = getParameterized("language-list-item").let(::LanguageListItemMessage)
}
