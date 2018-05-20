/*
 * Copyright 2018 Ross Binden
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

import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import com.rpkit.unconsciousness.bukkit.RPKUnconsciousnessBukkit
import com.rpkit.unconsciousness.bukkit.unconsciousness.RPKUnconsciousnessProvider
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType


class PlayerJoinListener(private val plugin: RPKUnconsciousnessBukkit): Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val bukkitPlayer = event.player
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val unconsciousnessProvider = plugin.core.serviceManager.getServiceProvider(RPKUnconsciousnessProvider::class)
        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitPlayer)
        if (minecraftProfile != null) {
            val character = characterProvider.getActiveCharacter(minecraftProfile)
            if (character != null) {
                if (!unconsciousnessProvider.isUnconscious(character)) {
                    event.player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 0,0), true)
                }
            }
        }
    }

}