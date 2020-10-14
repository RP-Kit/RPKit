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

package com.rpkit.locks.bukkit.keyring

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.locks.bukkit.RPKLocksBukkit
import com.rpkit.locks.bukkit.database.table.RPKKeyringTable
import org.bukkit.inventory.ItemStack


class RPKKeyringServiceImpl(override val plugin: RPKLocksBukkit) : RPKKeyringService {
    override fun getKeyring(character: RPKCharacter): MutableList<ItemStack> {
        return plugin.database.getTable(RPKKeyringTable::class)[character]?.items?.toMutableList()
                ?: mutableListOf()
    }

    override fun setKeyring(character: RPKCharacter, items: MutableList<ItemStack>) {
        val keyringTable = plugin.database.getTable(RPKKeyringTable::class)
        var keyring = keyringTable.get(character)
        if (keyring == null) {
            keyring = RPKKeyring(character = character, items = items)
            keyringTable.insert(keyring)
        } else {
            keyring.items.clear()
            keyring.items.addAll(items)
            keyringTable.update(keyring)
        }
    }

}