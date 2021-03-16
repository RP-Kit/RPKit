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

package com.rpkit.characters.bukkit.listener

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

/**
 * Player death listener for characters.
 * If server is configured to kill characters upon death, this will be done with this listener.
 */
class PlayerDeathListener(private val plugin: RPKCharactersBukkit) : Listener {

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        val characterService = Services[RPKCharacterService::class.java] ?: return
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(event.entity) ?: return
        val character = characterService.getPreloadedActiveCharacter(minecraftProfile) ?: return
        if (!plugin.config.getBoolean("characters.kill-character-on-death")) return
        character.isDead = true
        characterService.updateCharacter(character)
    }

}
