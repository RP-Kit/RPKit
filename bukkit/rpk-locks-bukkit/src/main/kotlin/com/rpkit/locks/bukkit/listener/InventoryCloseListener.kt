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

package com.rpkit.locks.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.locks.bukkit.RPKLocksBukkit
import com.rpkit.locks.bukkit.keyring.RPKKeyringService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent


class InventoryCloseListener(private val plugin: RPKLocksBukkit) : Listener {

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (!event.view.title.equals("Keyring", ignoreCase = true)) return
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        val characterService = Services[RPKCharacterService::class.java] ?: return
        val keyringService = Services[RPKKeyringService::class.java] ?: return
        val bukkitPlayer = event.player
        if (bukkitPlayer !is Player) return
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(bukkitPlayer) ?: return
        val character = characterService.getPreloadedActiveCharacter(minecraftProfile) ?: return
        val keyring = keyringService.getKeyring(character)
        keyring.clear()
        keyring.addAll(event.inventory.contents.filterNotNull())
        keyringService.setKeyring(character, keyring)
    }

}