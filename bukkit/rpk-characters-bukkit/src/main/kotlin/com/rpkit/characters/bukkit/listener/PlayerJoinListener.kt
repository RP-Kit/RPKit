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
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

/**
 * Player join listener for display names.
 * Updates player display names upon joining in order to allow for chat plugins that do not have built-in RPK
 * support to utilise character names.
 */
class PlayerJoinListener(val plugin: RPKCharactersBukkit) : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (!plugin.config.getBoolean("characters.set-player-display-name")) return
        val minecraftProfileService = Services[RPKMinecraftProfileService::class] ?: return
        val characterService = Services[RPKCharacterService::class] ?: return
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(event.player) ?: return
        val character = characterService.getActiveCharacter(minecraftProfile)
        event.player.setDisplayName(character?.name ?: event.player.name)
    }

}