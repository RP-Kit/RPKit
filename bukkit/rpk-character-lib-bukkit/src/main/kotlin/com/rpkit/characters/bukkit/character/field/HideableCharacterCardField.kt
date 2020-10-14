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

/**
 * Represents a hideable character card field.
 * Hideable character card fields can be set to hidden, which prevents their contents from being shown on the character
 * card.
 * What should be visible when the character card field is hidden is up to the implementation, and should be included
 * in the [get] method.
 */
interface HideableCharacterCardField : CharacterCardField {

    /**
     * Checks whether the field is hidden for the given character.
     *
     * @param character The character to check
     * @return Whether the field is hidden
     */
    fun isHidden(character: RPKCharacter): Boolean

    /**
     * Sets whether the field is hidden for the given character.
     *
     * @param character The character to set the field's hidden state for
     * @param hidden Whether the field should be hidden for the given character
     */
    fun setHidden(character: RPKCharacter, hidden: Boolean)

}