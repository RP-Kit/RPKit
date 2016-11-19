/*
 * Copyright 2016 Ross Binden
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

package com.seventh_root.elysium.characters.bukkit.character.field

import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter

/**
 * A character card field for age.
 */
class AgeField: HideableCharacterCardField {

    override val name = "age"
    override fun get(character: ElysiumCharacter): String {
        return if (isHidden(character)) "[HIDDEN]" else character.age.toString()
    }
    override fun isHidden(character: ElysiumCharacter): Boolean {
        return character.isAgeHidden
    }
    override fun setHidden(character: ElysiumCharacter, hidden: Boolean) {
        character.isAgeHidden = hidden
    }

}
