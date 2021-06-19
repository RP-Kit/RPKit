/*
 * Copyright 2021 Ren Binden
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

package com.rpkit.unconsciousness.bukkit

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.unconsciousness.bukkit.unconsciousness.RPKUnconsciousnessService
import org.bukkit.potion.PotionEffectType.BLINDNESS
import org.bukkit.scheduler.BukkitRunnable

class WakeTask(private val plugin: RPKUnconsciousnessBukkit) : BukkitRunnable() {
    override fun run() {
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        val characterService = Services[RPKCharacterService::class.java]
        val unconsciousnessService = Services[RPKUnconsciousnessService::class.java]
        plugin.server.onlinePlayers.forEach { bukkitPlayer ->
            val minecraftProfile = minecraftProfileService?.getPreloadedMinecraftProfile(bukkitPlayer)
            if (minecraftProfile != null) {
                val character = characterService?.getPreloadedActiveCharacter(minecraftProfile)
                if (character != null) {
                    if (unconsciousnessService?.getPreloadedUnconsciousness(character) == false && !character.isDead) {
                        bukkitPlayer.removePotionEffect(BLINDNESS)
                    }
                }
            }
        }
    }
}