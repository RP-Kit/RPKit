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

package com.rpkit.professions.bukkit.profession

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.professions.bukkit.RPKProfessionsBukkit
import com.rpkit.professions.bukkit.database.table.RPKCharacterProfessionChangeCooldownTable
import com.rpkit.professions.bukkit.database.table.RPKCharacterProfessionExperienceTable
import com.rpkit.professions.bukkit.database.table.RPKCharacterProfessionTable
import com.rpkit.professions.bukkit.event.profession.RPKBukkitProfessionAddEvent
import com.rpkit.professions.bukkit.event.profession.RPKBukkitProfessionExperienceChangeEvent
import com.rpkit.professions.bukkit.event.profession.RPKBukkitProfessionRemoveEvent
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap


class RPKProfessionServiceImpl(override val plugin: RPKProfessionsBukkit) : RPKProfessionService {
    override val professions: List<RPKProfession> = plugin.config
            .getConfigurationSection("professions")
            ?.getKeys(false)
            ?.map { professionName ->
                RPKProfessionImpl(
                        RPKProfessionName(professionName),
                        plugin.config.getInt("professions.$professionName.levels.max-level"),
                        plugin
                )
            }
            ?: emptyList()

    private val characterProfessions = ConcurrentHashMap<Int, List<RPKProfession>>()
    private val characterProfessionExperience = ConcurrentHashMap<Int, ConcurrentMap<RPKProfession, Int>>()

    override fun getProfession(name: RPKProfessionName): RPKProfession? {
        return professions.firstOrNull { profession -> profession.name.value == name.value }
    }

    override fun getProfessions(character: RPKCharacter): CompletableFuture<List<RPKProfession>> {
        return plugin.database.getTable(RPKCharacterProfessionTable::class.java)[character]
            .thenApply { it.map(RPKCharacterProfession::profession) }
    }

    override fun getPreloadedProfessions(character: RPKCharacter): List<RPKProfession>? {
        val characterId = character.id ?: return null
        return characterProfessions[characterId.value]
    }

    override fun loadProfessions(character: RPKCharacter): CompletableFuture<List<RPKProfession>> {
        val preloadedProfessions = getPreloadedProfessions(character)
        if (preloadedProfessions != null) return CompletableFuture.completedFuture(preloadedProfessions)
        val characterId = character.id ?: return CompletableFuture.completedFuture(emptyList())
        plugin.logger.info("Loading professions for character ${character.name} (${characterId.value})...")
        return plugin.database.getTable(RPKCharacterProfessionTable::class.java)[character].thenApply { characterProfessions ->
            val professions = characterProfessions.map(RPKCharacterProfession::profession)
            this.characterProfessions[characterId.value] = professions
            plugin.logger.info("Loaded professions for character ${character.name} (${characterId.value}): ${professions.joinToString(", ") { profession -> profession.name.value }}")
            return@thenApply professions
        }
    }

    override fun unloadProfessions(character: RPKCharacter) {
        val characterId = character.id ?: return
        characterProfessions.remove(characterId.value)
        plugin.logger.info("Unloaded professions for character ${character.name} (${characterId.value})")
    }

    override fun addProfession(character: RPKCharacter, profession: RPKProfession): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val characterProfessions = getProfessions(character).join()
            if (!characterProfessions.contains(profession)) {
                val event = RPKBukkitProfessionAddEvent(character, profession, true)
                plugin.server.pluginManager.callEvent(event)
                if (event.isCancelled) return@runAsync
                plugin.database.getTable(RPKCharacterProfessionTable::class.java)
                    .insert(
                        RPKCharacterProfession(
                            character = event.character,
                            profession = event.profession
                        )
                    )
                    .join()
                if (character.minecraftProfile?.isOnline == true) {
                    val characterId = character.id
                    if (characterId != null) {
                        this.characterProfessions[characterId.value] = characterProfessions + event.profession
                    }
                }
            }
        }
    }

    override fun removeProfession(character: RPKCharacter, profession: RPKProfession): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            if (getProfessions(character).join().contains(profession)) {
                val event = RPKBukkitProfessionRemoveEvent(character, profession, true)
                plugin.server.pluginManager.callEvent(event)
                if (event.isCancelled) return@runAsync
                val characterProfessionTable = plugin.database.getTable(RPKCharacterProfessionTable::class.java)
                val characterProfession = characterProfessionTable.get(event.character, event.profession).join()
                if (characterProfession != null) {
                    characterProfessionTable.delete(characterProfession).join()
                    if (character.minecraftProfile?.isOnline == true) {
                        val characterId = character.id
                        if (characterId != null) {
                            val characterProfessions =
                                this.characterProfessions[characterId.value] ?: listOf()
                            this.characterProfessions[characterId.value] = characterProfessions - profession
                        }
                    }
                }
            }
        }
    }

    override fun getProfessionLevel(character: RPKCharacter, profession: RPKProfession): CompletableFuture<Int> {
        return CompletableFuture.supplyAsync {
            val professionExperience = getProfessionExperience(character, profession).join()
            return@supplyAsync getLevelFromExperience(profession, professionExperience)
        }
    }

    override fun getPreloadedProfessionLevel(character: RPKCharacter, profession: RPKProfession): Int? {
        return getPreloadedProfessionExperience(character, profession)?.let { experience -> getLevelFromExperience(profession, experience) }
    }

    private fun getLevelFromExperience(profession: RPKProfession, experience: Int): Int {
        var level = 1
        while (experience >= profession.getExperienceNeededForLevel(level + 1)) {
            level++
        }
        return level
    }

    override fun setProfessionLevel(character: RPKCharacter, profession: RPKProfession, level: Int): CompletableFuture<Void> {
        return setProfessionExperience(character, profession, profession.getExperienceNeededForLevel(level))
    }

    override fun getProfessionExperience(character: RPKCharacter, profession: RPKProfession): CompletableFuture<Int> {
        val preloadedProfessionExperience = getPreloadedProfessionExperience(character, profession)
        if (preloadedProfessionExperience != null) return CompletableFuture.completedFuture(preloadedProfessionExperience)
        return plugin.database.getTable(RPKCharacterProfessionExperienceTable::class.java)[character, profession].thenApply { it?.experience ?: 0 }
    }

    override fun getPreloadedProfessionExperience(character: RPKCharacter, profession: RPKProfession): Int? {
        val characterId = character.id ?: return null
        return characterProfessionExperience[characterId.value]?.get(profession)
    }

    override fun loadProfessionExperience(character: RPKCharacter, profession: RPKProfession): CompletableFuture<Int?> {
        val preloadedExperience = getPreloadedProfessionExperience(character, profession)
        if (preloadedExperience != null) return CompletableFuture.completedFuture(preloadedExperience)
        val characterId = character.id ?: return CompletableFuture.completedFuture(null)
        plugin.logger.info("Loading profession experience for character ${character.name} (${characterId.value}), profession ${profession.name.value}...")
        return plugin.database.getTable(RPKCharacterProfessionExperienceTable::class.java)[character, profession].thenApply { characterProfessionExperience ->
            val professionExperienceValues = this.characterProfessionExperience[characterId.value] ?: ConcurrentHashMap()
            val experience = characterProfessionExperience?.experience ?: 0
            professionExperienceValues[profession] = experience
            this.characterProfessionExperience[characterId.value] = professionExperienceValues
            plugin.logger.info("Loaded profession experience for character ${character.name} (${characterId.value}), profession ${profession.name.value}")
            return@thenApply experience
        }
    }

    override fun unloadProfessionExperience(character: RPKCharacter, profession: RPKProfession) {
        val characterId = character.id ?: return
        val professionExperienceValues = characterProfessionExperience[characterId.value]
        if (professionExperienceValues != null) {
            professionExperienceValues.remove(profession)
            if (professionExperienceValues.isEmpty()) {
                characterProfessionExperience.remove(characterId.value)
            } else {
                characterProfessionExperience[characterId.value] = professionExperienceValues
            }
        }
        plugin.logger.info("Unloaded profession experience for character ${character.name} (${characterId.value}), profession ${profession.name.value}")
    }

    override fun setProfessionExperience(character: RPKCharacter, profession: RPKProfession, experience: Int): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val event = RPKBukkitProfessionExperienceChangeEvent(
                character,
                profession,
                getProfessionExperience(character, profession).join(),
                experience,
                true
            )
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            if (event.experience > profession.getExperienceNeededForLevel(event.profession.maxLevel)) {
                setProfessionLevel(event.character, event.profession, event.profession.maxLevel).join()
                return@runAsync
            }
            val characterProfessionExperienceTable =
                plugin.database.getTable(RPKCharacterProfessionExperienceTable::class.java)
            var characterProfessionExperience = characterProfessionExperienceTable[event.character, event.profession].join()
            if (characterProfessionExperience == null) {
                characterProfessionExperience = RPKCharacterProfessionExperience(
                    character = event.character,
                    profession = event.profession,
                    experience = event.experience
                )
                characterProfessionExperienceTable.insert(characterProfessionExperience).join()
            } else {
                characterProfessionExperience.experience = event.experience
                characterProfessionExperienceTable.update(characterProfessionExperience).join()
            }
            if (event.character.minecraftProfile?.isOnline == true) {
                val characterId = event.character.id
                if (characterId != null) {
                    val preloadedCharacterProfessionExperience = this.characterProfessionExperience[characterId.value] ?: ConcurrentHashMap()
                    preloadedCharacterProfessionExperience[profession] = experience
                    this.characterProfessionExperience[characterId.value] = preloadedCharacterProfessionExperience
                }
            }
        }
    }

    override fun getProfessionChangeCooldown(character: RPKCharacter): CompletableFuture<Duration> {
        return CompletableFuture.supplyAsync {
            val characterProfessionChangeCooldown =
                plugin.database.getTable(RPKCharacterProfessionChangeCooldownTable::class.java)
                    .get(character).join() ?: return@supplyAsync Duration.ZERO
            if (characterProfessionChangeCooldown.cooldownEndTime.isBefore(LocalDateTime.now())) return@supplyAsync Duration.ZERO
            return@supplyAsync Duration.between(
                LocalDateTime.now(),
                characterProfessionChangeCooldown.cooldownEndTime
            )
        }
    }

    override fun setProfessionChangeCooldown(character: RPKCharacter, cooldown: Duration): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val characterProfessionChangeCooldownTable =
                plugin.database.getTable(RPKCharacterProfessionChangeCooldownTable::class.java)
            var characterProfessionChangeCooldown = characterProfessionChangeCooldownTable.get(character).join()
            if (characterProfessionChangeCooldown == null) {
                characterProfessionChangeCooldown = RPKCharacterProfessionChangeCooldown(
                    character = character,
                    cooldownEndTime = LocalDateTime.now().plus(cooldown)
                )
                characterProfessionChangeCooldownTable.insert(characterProfessionChangeCooldown).join()
            } else {
                characterProfessionChangeCooldown.cooldownEndTime = LocalDateTime.now().plus(cooldown)
                characterProfessionChangeCooldownTable.update(characterProfessionChangeCooldown).join()
            }
        }
    }
}