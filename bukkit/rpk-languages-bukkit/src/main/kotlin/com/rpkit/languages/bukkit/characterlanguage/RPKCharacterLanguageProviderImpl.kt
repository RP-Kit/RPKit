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

package com.rpkit.languages.bukkit.characterlanguage

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.languages.bukkit.RPKLanguagesBukkit
import com.rpkit.languages.bukkit.database.table.RPKCharacterLanguageTable
import com.rpkit.languages.bukkit.language.RPKLanguage
import kotlin.math.max
import kotlin.math.min

class RPKCharacterLanguageProviderImpl(private val plugin: RPKLanguagesBukkit): RPKCharacterLanguageProvider {
    override fun getCharacterLanguageUnderstanding(character: RPKCharacter, language: RPKLanguage): Float {
        val race = character.race
        return plugin.core.database.getTable(RPKCharacterLanguageTable::class).get(character, language)?.understanding
                ?: if (race != null) language.getBaseUnderstanding(race) else 0f
    }

    override fun setCharacterLanguageUnderstanding(character: RPKCharacter, language: RPKLanguage, understanding: Float) {
        val characterLanguageTable = plugin.core.database.getTable(RPKCharacterLanguageTable::class)
        val characterLanguage = characterLanguageTable.get(character, language)
        if (characterLanguage != null) {
            characterLanguage.understanding = max(min(understanding, 100f), 0f)
            characterLanguageTable.update(characterLanguage)
        } else {
            characterLanguageTable.insert(RPKCharacterLanguage(
                    character = character,
                    language = language,
                    understanding = max(min(understanding, 100f), 0f)
            ))
        }
    }
}