/*
 * Copyright 2019 Ren Binden
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


class RPKProfessionProviderImpl(val plugin: RPKProfessionsBukkit): RPKProfessionProvider {
    override val professions: List<RPKProfession> = plugin.config
            .getConfigurationSection("professions")
            ?.getKeys(false)
            ?.withIndex()
            ?.map { (index, professionName) ->
                RPKProfessionImpl(
                        index,
                        professionName,
                        plugin.config.getInt("professions.$professionName.levels.max-level"),
                        plugin
                )
            }
            ?: emptyList()

    override fun getProfession(name: String): RPKProfession? {
        return professions.firstOrNull { profession -> profession.name == name }
    }

    override fun getProfessions(character: RPKCharacter): List<RPKProfession> {
        return plugin.core.database.getTable(RPKCharacterProfessionTable::class)
                .get(character)
                .map(RPKCharacterProfession::profession)
    }

    override fun addProfession(character: RPKCharacter, profession: RPKProfession) {
        if (!getProfessions(character).contains(profession)) {
            val event = RPKBukkitProfessionAddEvent(character, profession)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return
            plugin.core.database.getTable(RPKCharacterProfessionTable::class)
                    .insert(RPKCharacterProfession(
                            character = event.character,
                            profession = event.profession
                    ))
        }
    }

    override fun removeProfession(character: RPKCharacter, profession: RPKProfession) {
        if (getProfessions(character).contains(profession)) {
            val event = RPKBukkitProfessionRemoveEvent(character, profession)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return
            val characterProfessionTable = plugin.core.database.getTable(RPKCharacterProfessionTable::class)
            val characterProfession = characterProfessionTable.get(event.character, event.profession)
            if (characterProfession != null) {
                characterProfessionTable.delete(characterProfession)
            }
        }
    }

    override fun getProfessionLevel(character: RPKCharacter, profession: RPKProfession): Int {
        val professionExperience = getProfessionExperience(character, profession)
        var level = 1
        while (professionExperience >= profession.getExperienceNeededForLevel(level + 1)) {
            level++
        }
        return level
    }

    override fun setProfessionLevel(character: RPKCharacter, profession: RPKProfession, level: Int) {
        setProfessionExperience(character, profession, profession.getExperienceNeededForLevel(level))
    }

    override fun getProfessionExperience(character: RPKCharacter, profession: RPKProfession): Int {
        return plugin.core.database.getTable(RPKCharacterProfessionExperienceTable::class)
                .get(character, profession)?.experience ?: 0
    }

    override fun setProfessionExperience(character: RPKCharacter, profession: RPKProfession, experience: Int) {
        val event = RPKBukkitProfessionExperienceChangeEvent(
                character,
                profession,
                getProfessionExperience(character, profession),
                experience
        )
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        if (event.experience > profession.getExperienceNeededForLevel(event.profession.maxLevel)) {
            setProfessionLevel(event.character, event.profession, event.profession.maxLevel)
            return
        }
        val characterProfessionExperienceTable = plugin.core.database.getTable(RPKCharacterProfessionExperienceTable::class)
        var characterProfessionExperience = characterProfessionExperienceTable.get(event.character, event.profession)
        if (characterProfessionExperience == null) {
            characterProfessionExperience = RPKCharacterProfessionExperience(
                    character = event.character,
                    profession = event.profession,
                    experience = event.experience
            )
            characterProfessionExperienceTable.insert(characterProfessionExperience)
        } else {
            characterProfessionExperience.experience = event.experience
            characterProfessionExperienceTable.update(characterProfessionExperience)
        }
    }

    override fun getProfessionChangeCooldown(character: RPKCharacter): Duration {
        val characterProfessionChangeCooldown = plugin.core.database.getTable(RPKCharacterProfessionChangeCooldownTable::class)
                .get(character) ?: return Duration.ZERO
        if (characterProfessionChangeCooldown.cooldownEndTime.isBefore(LocalDateTime.now())) return Duration.ZERO
        return Duration.between(
                LocalDateTime.now(),
                characterProfessionChangeCooldown.cooldownEndTime
        )
    }

    override fun setProfessionChangeCooldown(character: RPKCharacter, cooldown: Duration) {
        val characterProfessionChangeCooldownTable = plugin.core.database.getTable(RPKCharacterProfessionChangeCooldownTable::class)
        var characterProfessionChangeCooldown = characterProfessionChangeCooldownTable.get(character)
        if (characterProfessionChangeCooldown == null) {
            characterProfessionChangeCooldown = RPKCharacterProfessionChangeCooldown(
                    character = character,
                    cooldownEndTime = LocalDateTime.now().plus(cooldown)
            )
            characterProfessionChangeCooldownTable.insert(characterProfessionChangeCooldown)
        } else {
            characterProfessionChangeCooldown.cooldownEndTime = LocalDateTime.now().plus(cooldown)
            characterProfessionChangeCooldownTable.update(characterProfessionChangeCooldown)
        }
    }
}