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

package com.rpkit.professions.bukkit.listener

import com.rpkit.characters.bukkit.event.character.RPKBukkitCharacterDeleteEvent
import com.rpkit.professions.bukkit.RPKProfessionsBukkit
import com.rpkit.professions.bukkit.database.table.RPKCharacterProfessionChangeCooldownTable
import com.rpkit.professions.bukkit.database.table.RPKCharacterProfessionExperienceTable
import com.rpkit.professions.bukkit.database.table.RPKCharacterProfessionTable
import com.rpkit.professions.bukkit.database.table.RPKProfessionHiddenTable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener


class RPKBukkitCharacterDeleteListener(private val plugin: RPKProfessionsBukkit) : Listener {

    @EventHandler
    fun onCharacterDelete(event: RPKBukkitCharacterDeleteEvent) {
        val characterProfessionChangeCooldownTable = plugin.database.getTable(RPKCharacterProfessionChangeCooldownTable::class.java)
        val characterProfessionChangeCooldown = characterProfessionChangeCooldownTable.get(event.character)
        if (characterProfessionChangeCooldown != null) {
            characterProfessionChangeCooldownTable.delete(characterProfessionChangeCooldown)
        }

        val characterProfessionExperienceTable = plugin.database.getTable(RPKCharacterProfessionExperienceTable::class.java)
        characterProfessionExperienceTable.delete(event.character)

        val characterProfessionTable = plugin.database.getTable(RPKCharacterProfessionTable::class.java)
        characterProfessionTable[event.character].forEach { characterProfession ->
            characterProfessionTable.delete(characterProfession)
        }

        val professionHiddenTable = plugin.database.getTable(RPKProfessionHiddenTable::class.java)
        val professionHidden = professionHiddenTable[event.character]
        if (professionHidden != null) {
            professionHiddenTable.delete(professionHidden)
        }
    }

}