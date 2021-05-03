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
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.unconsciousness.bukkit.unconsciousness.RPKUnconsciousnessService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.potion.PotionEffectType.BLINDNESS


class PlayerJoinListener : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val bukkitPlayer = event.player
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        val characterService = Services[RPKCharacterService::class.java] ?: return
        val unconsciousnessService = Services[RPKUnconsciousnessService::class.java] ?: return
        val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(bukkitPlayer) ?: return
        val character = characterService.getPreloadedActiveCharacter(minecraftProfile) ?: return
        if (unconsciousnessService.isUnconscious(character)) return
        event.player.removePotionEffect(BLINDNESS)
    }

}