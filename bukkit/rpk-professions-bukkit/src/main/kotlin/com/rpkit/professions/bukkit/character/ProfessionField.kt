/*
 * Copyright 2019 Ren Binden
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

package com.rpkit.professions.bukkit.character

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.field.HideableCharacterCardField
import com.rpkit.professions.bukkit.RPKProfessionsBukkit
import com.rpkit.professions.bukkit.database.table.RPKProfessionHiddenTable
import com.rpkit.professions.bukkit.profession.RPKProfession
import com.rpkit.professions.bukkit.profession.RPKProfessionProvider


class ProfessionField(val plugin: RPKProfessionsBukkit): HideableCharacterCardField {

    override val name = "profession"

    override fun get(character: RPKCharacter): String {
        return if (isHidden(character)) {
            "[HIDDEN]"
        } else {
            val professionProvider = plugin.core.serviceManager.getServiceProvider(RPKProfessionProvider::class)
            professionProvider.getProfessions(character).map(RPKProfession::name).joinToString(", ")
        }
    }

    override fun isHidden(character: RPKCharacter): Boolean {
        return plugin.core.database.getTable(RPKProfessionHiddenTable::class).get(character) != null
    }

    override fun setHidden(character: RPKCharacter, hidden: Boolean) {
        val professionHiddenTable = plugin.core.database.getTable(RPKProfessionHiddenTable::class)
        if (hidden) {
            if (professionHiddenTable.get(character) == null) {
                professionHiddenTable.insert(RPKProfessionHidden(character = character))
            }
        } else {
            val professionHidden = professionHiddenTable.get(character)
            if (professionHidden != null) {
                professionHiddenTable.delete(professionHidden)
            }
        }
    }

}