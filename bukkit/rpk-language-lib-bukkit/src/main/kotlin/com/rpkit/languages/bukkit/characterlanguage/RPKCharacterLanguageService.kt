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
import com.rpkit.core.service.Service
import com.rpkit.languages.bukkit.language.RPKLanguage
import java.util.concurrent.CompletableFuture

interface RPKCharacterLanguageService : Service {

    /**
     * Gets a character's understanding of a language. The understanding is returned as a float, with a minimum value of
     * 0 and a maximum value of 100. When a character hears a message in the language, their understanding of the
     * language increases. The understanding is used to determine how much of a message is affected by the language's
     * cypher.
     *
     * @param character The character
     * @param language The language
     * @return The character's understanding of a language. Minimum 0, maximum 100.
     */
    fun getCharacterLanguageUnderstanding(character: RPKCharacter, language: RPKLanguage): CompletableFuture<Float>

    /**
     * Sets a character's understanding of a language. The understanding must be a float, with a minimum value of 0 and
     * a maximum value of 100. The understanding is used to determine how much of a message is affected by the
     * language's cypher.
     *
     * @param character The character
     * @param language The language
     * @param understanding The character's understanding of the language. Minimum 0, maximum 100.
     */
    fun setCharacterLanguageUnderstanding(character: RPKCharacter, language: RPKLanguage, understanding: Float): CompletableFuture<Void>

}