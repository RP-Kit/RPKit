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

package com.rpkit.unconsciousness.bukkit.unconsciousness

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.unconsciousness.bukkit.RPKUnconsciousnessBukkit
import com.rpkit.unconsciousness.bukkit.database.table.RPKUnconsciousStateTable
import com.rpkit.unconsciousness.bukkit.event.unconsciousness.RPKBukkitUnconsciousnessStateChangeEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_DATE_TIME
import java.time.temporal.ChronoUnit
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap


class RPKUnconsciousnessServiceImpl(override val plugin: RPKUnconsciousnessBukkit) : RPKUnconsciousnessService {

    private val deathTimes = ConcurrentHashMap<Int, LocalDateTime>()

    override fun isUnconscious(character: RPKCharacter): CompletableFuture<Boolean> {
        val unconsciousStateTable = plugin.database.getTable(RPKUnconsciousStateTable::class.java)
        return unconsciousStateTable[character].thenApply { unconsciousState ->
            return@thenApply if (unconsciousState == null) {
                false
            } else {
                unconsciousState.deathTime.plus(
                    plugin.config.getLong("unconscious-time"),
                    ChronoUnit.MILLIS
                ) > LocalDateTime.now()
            }
        }
    }

    override fun setUnconscious(character: RPKCharacter, unconscious: Boolean): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val event = RPKBukkitUnconsciousnessStateChangeEvent(character, unconscious, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            val unconsciousStateTable = plugin.database.getTable(RPKUnconsciousStateTable::class.java)
            unconsciousStateTable[event.character].thenAcceptAsync { unconsciousState ->
                if (unconsciousState != null) {
                    if (event.isUnconscious) {
                        unconsciousState.deathTime = LocalDateTime.now()
                        unconsciousStateTable.update(unconsciousState).join()
                        if (event.character.minecraftProfile?.isOnline == true) {
                            val characterId = event.character.id
                            if (characterId != null) {
                                deathTimes[characterId.value] = unconsciousState.deathTime
                            }
                        }
                        val minecraftUUID = event.character.minecraftProfile?.minecraftUUID ?: return@thenAcceptAsync
                        plugin.server.getPlayer(minecraftUUID)
                            ?.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 0))
                    } else {
                        unconsciousStateTable.delete(unconsciousState).join()
                        if (event.character.minecraftProfile?.isOnline == true) {
                            val characterId = event.character.id
                            if (characterId != null) {
                                deathTimes.remove(characterId.value)
                            }
                        }
                    }
                } else {
                    if (event.isUnconscious) {
                        val deathTime = LocalDateTime.now()
                        unconsciousStateTable.insert(RPKUnconsciousState(
                            character = event.character,
                            deathTime = deathTime
                        )).join()
                        if (event.character.minecraftProfile?.isOnline == true) {
                            val characterId = event.character.id
                            if (characterId != null) {
                                deathTimes[characterId.value] = deathTime
                            }
                        }
                    } else {
                        if (event.character.minecraftProfile?.isOnline == true) {
                            val characterId = event.character.id
                            if (characterId != null) {
                                deathTimes.remove(characterId.value)
                            }
                        }
                    }
                }
            }.join()
        }
    }

    override fun loadUnconsciousness(character: RPKCharacter): CompletableFuture<Boolean> {
        val characterId = character.id ?: return CompletableFuture.completedFuture(false)
        plugin.logger.info("Loading unconsciousness for character ${character.name} (${characterId.value})...")
        val unconsciousStateTable = plugin.database.getTable(RPKUnconsciousStateTable::class.java)
        return unconsciousStateTable[character].thenApply { unconsciousState ->
            if (unconsciousState == null) {
                plugin.logger.info("Loaded unconsciousness for character ${character.name} (${characterId.value}): Not currently unconscious")
                return@thenApply false
            } else {
                deathTimes[characterId.value] = unconsciousState.deathTime
                plugin.logger.info("Loaded unconsciousness for character ${character.name} (${characterId.value}): Died at ${ISO_DATE_TIME.format(unconsciousState.deathTime)}")
                return@thenApply unconsciousState.deathTime.plus(
                        plugin.config.getLong("unconscious-time"),
                        ChronoUnit.MILLIS
                ) > LocalDateTime.now()
            }
        }
    }

    override fun unloadUnconsciousness(character: RPKCharacter) {
        val characterId = character.id ?: return
        deathTimes.remove(characterId.value)
        plugin.logger.info("Unloaded unconsciousness for character ${character.name} (${characterId.value})")
    }

    override fun getPreloadedUnconsciousness(character: RPKCharacter): Boolean {
        val characterId = character.id ?: return false
        val deathTime = deathTimes[characterId.value]
        return if (deathTime == null) {
            false
        } else {
            deathTime.plus(
                plugin.config.getLong("unconscious-time"),
                ChronoUnit.MILLIS
            ) > LocalDateTime.now()
        }
    }

}