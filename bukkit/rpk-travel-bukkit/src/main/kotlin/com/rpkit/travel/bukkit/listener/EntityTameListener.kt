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

package com.rpkit.travel.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.travel.bukkit.tamedcreature.RPKTamedCreatureService
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityTameEvent

class EntityTameListener : Listener {

    @EventHandler
    fun onEntityTame(event: EntityTameEvent) {
        val player = event.owner as? Player ?: return
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(player) ?: return
        val characterService = Services[RPKCharacterService::class.java] ?: return
        val character = characterService.getPreloadedActiveCharacter(minecraftProfile) ?: return
        val tamedCreatureService = Services[RPKTamedCreatureService::class.java] ?: return
        val characterId = character.id
        if (characterId != null) {
            tamedCreatureService.setOwner(event.entity, characterId)
        }
    }

}