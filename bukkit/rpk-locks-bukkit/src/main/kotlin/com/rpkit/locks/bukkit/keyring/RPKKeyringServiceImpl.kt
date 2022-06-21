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

package com.rpkit.locks.bukkit.keyring

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.locks.bukkit.RPKLocksBukkit
import com.rpkit.locks.bukkit.database.table.RPKKeyringTable
import org.bukkit.inventory.ItemStack
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level


class RPKKeyringServiceImpl(override val plugin: RPKLocksBukkit) : RPKKeyringService {

    private val keyrings = ConcurrentHashMap<Int, MutableList<ItemStack?>>()

    override fun getKeyring(character: RPKCharacter): CompletableFuture<MutableList<ItemStack?>> {
        val preloadedKeyring = getPreloadedKeyring(character)
        if (preloadedKeyring != null) return CompletableFuture.completedFuture(preloadedKeyring)
        return plugin.database.getTable(RPKKeyringTable::class.java)[character].thenApply {
            it?.items?.toMutableList() ?: mutableListOf()
        }
    }

    override fun setKeyring(character: RPKCharacter, items: MutableList<ItemStack?>): CompletableFuture<Void> {
        val keyringTable = plugin.database.getTable(RPKKeyringTable::class.java)
        return keyringTable[character].thenAcceptAsync { keyring ->
            if (keyring == null) {
                keyringTable.insert(RPKKeyring(character = character, items = items)).join()
            } else {
                keyring.items.clear()
                keyring.items.addAll(items)
                keyringTable.update(keyring).join()
            }
            character.id?.value?.let { keyrings[it] = items }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to set keyring", exception)
            throw exception
        }
    }

    override fun loadKeyring(character: RPKCharacter): CompletableFuture<MutableList<ItemStack?>> {
        plugin.logger.info("Loading keyring for character ${character.name} (${character.id?.value})...")
        return plugin.database.getTable(RPKKeyringTable::class.java)[character].thenApply { keyring ->
            val items = keyring?.items ?: mutableListOf()
            character.id?.value?.let { keyrings[it] = items }
            plugin.logger.info("Loaded keyring for character ${character.name} (${character.id?.value})")
            return@thenApply items
        }
    }

    override fun unloadKeyring(character: RPKCharacter) {
        character.id?.value?.let { keyrings.remove(it) }
    }

    override fun getPreloadedKeyring(character: RPKCharacter): MutableList<ItemStack?>? {
        return character.id?.value?.let { keyrings[it] }
    }

}