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

package com.rpkit.unconsciousness.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileService
import com.rpkit.unconsciousness.bukkit.unconsciousness.RPKUnconsciousnessService
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent


class PlayerMoveListener : Listener {

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val minecraftProfileService = Services[RPKMinecraftProfileService::class] ?: return
        val characterService = Services[RPKCharacterService::class] ?: return
        val unconsciousnessService = Services[RPKUnconsciousnessService::class] ?: return
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(event.player) ?: return
        val character = characterService.getActiveCharacter(minecraftProfile) ?: return
        if (!unconsciousnessService.isUnconscious(character)) return
        if (event.from.blockX == event.to?.blockX && event.from.blockZ == event.to?.blockZ) return
        event.player.teleport(Location(
                event.from.world,
                event.from.blockX + 0.5,
                event.from.blockY + 0.5,
                event.from.blockZ + 0.5,
                event.to?.yaw ?: event.from.yaw,
                event.to?.pitch ?: event.from.pitch
        ))
    }

}