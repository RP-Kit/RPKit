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

package com.rpkit.languages.bukkit.listener

import com.rpkit.characters.bukkit.event.character.RPKBukkitCharacterDeleteEvent
import com.rpkit.languages.bukkit.RPKLanguagesBukkit
import com.rpkit.languages.bukkit.database.table.RPKCharacterLanguageTable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class RPKCharacterDeleteListener(private val plugin: RPKLanguagesBukkit) : Listener {

    @EventHandler
    fun onCharacterDelete(event: RPKBukkitCharacterDeleteEvent) {
        val characterLanguageTable = plugin.database.getTable(RPKCharacterLanguageTable::class.java)
        characterLanguageTable.get(event.character).thenAccept { characterLanguages ->
            characterLanguages.forEach { characterLanguageTable.delete(it).join() }
        }
    }

}