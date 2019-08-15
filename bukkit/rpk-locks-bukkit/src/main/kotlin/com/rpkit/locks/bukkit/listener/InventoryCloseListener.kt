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

package com.rpkit.locks.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.locks.bukkit.RPKLocksBukkit
import com.rpkit.locks.bukkit.keyring.RPKKeyringProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent


class InventoryCloseListener(private val plugin: RPKLocksBukkit): Listener {

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (event.view.title.equals("Keyring", ignoreCase = true)) {
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
            val keyringProvider = plugin.core.serviceManager.getServiceProvider(RPKKeyringProvider::class)
            val bukkitPlayer = event.player
            if (bukkitPlayer is Player) {
                val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitPlayer)
                if (minecraftProfile != null) {
                    val character = characterProvider.getActiveCharacter(minecraftProfile)
                    if (character != null) {
                        val keyring = keyringProvider.getKeyring(character)
                        keyring.clear()
                        keyring.addAll(event.inventory.contents.filterNotNull())
                        keyringProvider.setKeyring(character, keyring)
                    }
                }
            }
        }
    }

}