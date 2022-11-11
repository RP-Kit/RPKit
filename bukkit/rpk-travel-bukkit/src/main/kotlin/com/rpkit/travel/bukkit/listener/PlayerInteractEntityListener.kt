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
import com.rpkit.travel.bukkit.RPKTravelBukkit
import com.rpkit.travel.bukkit.tamedcreature.RPKTamedCreatureService
import com.rpkit.travel.bukkit.untamer.RPKUntamerService
import org.bukkit.entity.Tameable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent

class PlayerInteractEntityListener(private val plugin: RPKTravelBukkit) : Listener {

    @EventHandler
    fun onPlayerInteractEntity(event: PlayerInteractEntityEvent) {
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(event.player) ?: return
        val minecraftProfileId = minecraftProfile.id ?: return
        val characterService = Services[RPKCharacterService::class.java] ?: return
        val untamerService = Services[RPKUntamerService::class.java] ?: return
        val entity = event.rightClicked
        if (entity !is Tameable) return
        if (entity.owner == null) return
        val tamedCreatureService = Services[RPKTamedCreatureService::class.java] ?: return
        val ownerCharacterId = tamedCreatureService.getOwner(entity)
        val playerCharacter = characterService.getPreloadedActiveCharacter(minecraftProfile)
        if (ownerCharacterId?.value != null && playerCharacter?.id?.value != ownerCharacterId.value) {
            event.isCancelled = true
            characterService.getCharacter(ownerCharacterId).thenAccept { ownerCharacter ->
                if (ownerCharacter != null) {
                    event.player.sendMessage(plugin.messages.tamedBy.withParameters(entity = entity, character = ownerCharacter))
                }
            }
            return
        }
        if (!untamerService.isUntaming(minecraftProfileId)) return
        entity.isTamed = false
        untamerService.setUntaming(minecraftProfileId, false)
        event.player.sendMessage(plugin.messages.untameValid)
        event.isCancelled = true
        tamedCreatureService.setOwner(entity, null)
    }

}