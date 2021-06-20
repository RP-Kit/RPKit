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

package com.rpkit.experience.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.experience.bukkit.RPKExperienceBukkit
import com.rpkit.experience.bukkit.experience.RPKExperienceService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent


class PlayerJoinListener(private val plugin: RPKExperienceBukkit) : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        val characterService = Services[RPKCharacterService::class.java] ?: return
        val experienceService = Services[RPKExperienceService::class.java] ?: return
        minecraftProfileService.getMinecraftProfile(event.player).thenAccept getMinecraftProfile@{ minecraftProfile ->
            if (minecraftProfile == null) return@getMinecraftProfile
            characterService.getActiveCharacter(minecraftProfile).thenAccept getActiveCharacter@{ character ->
                if (character == null) return@getActiveCharacter
                experienceService.getLevel(character).thenAccept { characterLevel ->
                    experienceService.getExperience(character).thenAccept { characterExperience ->
                        plugin.server.scheduler.runTask(plugin, Runnable {
                            event.player.level = characterLevel
                            event.player.exp =
                                (characterExperience - experienceService.getExperienceNeededForLevel(characterLevel)).toFloat() /
                                        (experienceService.getExperienceNeededForLevel(characterLevel + 1) - experienceService.getExperienceNeededForLevel(characterLevel)).toFloat()
                        })
                    }
                }
            }
        }
    }

}