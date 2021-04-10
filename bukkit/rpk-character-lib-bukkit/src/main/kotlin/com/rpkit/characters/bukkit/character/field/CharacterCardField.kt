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

package com.rpkit.characters.bukkit.character.field

import com.rpkit.characters.bukkit.character.RPKCharacter
import java.util.concurrent.CompletableFuture

/**
 * Represents a character card field.
 * Each character card field gives a variable to use with character cards.
 * When the variable is used on the character card, it is replaced with what is returned from calling [get] with the
 * character for whom the character card belongs to.
 */
interface CharacterCardField {

    /**
     * The name of the character
     */
    val name: String

    /**
     * Gets the value of the field for a character.
     *
     * @param character The character
     * @return The value of the character card field for the character
     */
    fun get(character: RPKCharacter): CompletableFuture<String>

}