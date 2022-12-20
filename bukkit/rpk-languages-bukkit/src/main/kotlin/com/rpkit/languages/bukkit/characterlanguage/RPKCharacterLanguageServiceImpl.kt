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

package com.rpkit.languages.bukkit.characterlanguage

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.languages.bukkit.RPKLanguagesBukkit
import com.rpkit.languages.bukkit.database.table.RPKCharacterLanguageTable
import com.rpkit.languages.bukkit.language.RPKLanguage
import java.util.concurrent.CompletableFuture
import java.util.logging.Level
import kotlin.math.max
import kotlin.math.min

class RPKCharacterLanguageServiceImpl(override val plugin: RPKLanguagesBukkit) : RPKCharacterLanguageService {
    override fun getCharacterLanguageUnderstanding(character: RPKCharacter, language: RPKLanguage): CompletableFuture<Float> {
        val species = character.species
        return plugin.database.getTable(RPKCharacterLanguageTable::class.java)[character, language]
            .thenApply { it?.understanding ?: if (species != null) language.getBaseUnderstanding(species) else 0f }

    }

    override fun setCharacterLanguageUnderstanding(character: RPKCharacter, language: RPKLanguage, understanding: Float): CompletableFuture<Void> {
        val characterLanguageTable = plugin.database.getTable(RPKCharacterLanguageTable::class.java)
        return characterLanguageTable[character, language].thenAcceptAsync { characterLanguage ->
            if (characterLanguage != null) {
                characterLanguage.understanding = max(min(understanding, 100f), 0f)
                characterLanguageTable.update(characterLanguage).join()
            } else {
                characterLanguageTable.insert(
                    RPKCharacterLanguage(
                        character = character,
                        language = language,
                        understanding = max(min(understanding, 100f), 0f)
                    )
                ).join()
            }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to set character language understanding", exception)
            throw exception
        }
    }
}