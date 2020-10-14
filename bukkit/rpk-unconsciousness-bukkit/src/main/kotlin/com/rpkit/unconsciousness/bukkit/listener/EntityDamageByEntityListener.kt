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
import com.rpkit.unconsciousness.bukkit.RPKUnconsciousnessBukkit
import com.rpkit.unconsciousness.bukkit.unconsciousness.RPKUnconsciousnessService
import org.bukkit.entity.Creature
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent


class EntityDamageByEntityListener(private val plugin: RPKUnconsciousnessBukkit) : Listener {

    @EventHandler
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        val damager = event.damager
        if (damager is Player) {
            val minecraftProfileService = Services[RPKMinecraftProfileService::class] ?: return
            val characterService = Services[RPKCharacterService::class] ?: return
            val unconsciousnessService = Services[RPKUnconsciousnessService::class] ?: return
            val minecraftProfile = minecraftProfileService.getMinecraftProfile(damager) ?: return
            val character = characterService.getActiveCharacter(minecraftProfile) ?: return
            if (!unconsciousnessService.isUnconscious(character)) return
            event.isCancelled = true
        } else if (damager is Projectile) {
            val shooter = damager.shooter
            if (shooter !is Player) return
            val minecraftProfileService = Services[RPKMinecraftProfileService::class] ?: return
            val characterService = Services[RPKCharacterService::class] ?: return
            val unconsciousnessService = Services[RPKUnconsciousnessService::class] ?: return
            val minecraftProfile = minecraftProfileService.getMinecraftProfile(shooter) ?: return
            val character = characterService.getActiveCharacter(minecraftProfile) ?: return
            if (!unconsciousnessService.isUnconscious(character)) return
            event.isCancelled = true
            val target = event.entity
            if (target !is Creature) return
            target.target = shooter
        }
    }

}