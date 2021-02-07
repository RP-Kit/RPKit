/*
 * Copyright 2021 Ren Binden
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
 * Character card field for race.
 */
class RaceField : HideableCharacterCardField {

    override val name = "race"
    override fun get(character: RPKCharacter): String {
        return if (isHidden(character)) "[HIDDEN]" else character.race?.name?.value ?: "unset"
    }

    override fun isHidden(character: RPKCharacter): Boolean {
        return character.isRaceHidden
    }

    override fun setHidden(character: RPKCharacter, hidden: Boolean) {
        character.isRaceHidden = hidden
    }

}
