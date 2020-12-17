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

package com.rpkit.experience.bukkit.experience

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.experience.bukkit.RPKExperienceBukkit
import com.rpkit.experience.bukkit.database.table.RPKExperienceTable
import com.rpkit.experience.bukkit.event.experience.RPKBukkitExperienceChangeEvent
import org.nfunk.jep.JEP
import kotlin.math.roundToInt


class RPKExperienceServiceImpl(override val plugin: RPKExperienceBukkit) : RPKExperienceService {

    override fun getLevel(character: RPKCharacter): Int {
        val experience = getExperience(character)
        var level = 1
        while (level + 1 <= plugin.config.getInt("levels.max-level") && getExperienceNeededForLevel(level + 1) <= experience) {
            level++
        }
        return level
    }

    override fun setLevel(character: RPKCharacter, level: Int) {
        setExperience(character, getExperienceNeededForLevel(level))
    }

    override fun getExperience(character: RPKCharacter): Int {
        val experienceTable = plugin.database.getTable(RPKExperienceTable::class.java)
        val experienceValue = experienceTable.get(character)
        return experienceValue?.value ?: 0
    }

    override fun setExperience(character: RPKCharacter, experience: Int) {
        val event = RPKBukkitExperienceChangeEvent(character, getExperience(character), experience)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        val experienceTable = plugin.database.getTable(RPKExperienceTable::class.java)
        var experienceValue = experienceTable.get(character)
        if (experienceValue == null) {
            experienceValue = RPKExperienceValue(character = character, value = event.experience)
            experienceTable.insert(experienceValue)
        } else {
            experienceValue.value = event.experience
            experienceTable.update(experienceValue)
        }
        var level = 1
        while (level + 1 <= plugin.config.getInt("levels.max-level") && getExperienceNeededForLevel(level + 1) <= event.experience) {
            level++
        }
        val isMaxLevel = level == plugin.config.getInt("levels.max-level")
        val minecraftProfile = character.minecraftProfile
        if (minecraftProfile != null) {
            val bukkitPlayer = plugin.server.getPlayer(minecraftProfile.minecraftUUID)
            if (bukkitPlayer != null) {
                bukkitPlayer.level = level
                if (isMaxLevel) {
                    bukkitPlayer.exp = 0F
                } else {
                    bukkitPlayer.exp = (event.experience - getExperienceNeededForLevel(level)).toFloat() / (getExperienceNeededForLevel(level + 1) - getExperienceNeededForLevel(level)).toFloat()
                }
            }
        }
    }

    override fun getExperienceNeededForLevel(level: Int): Int {
        val expression = plugin.config.getString("experience.formula")
        val parser = JEP()
        parser.addStandardConstants()
        parser.addStandardFunctions()
        parser.addVariable("level", level.toDouble())
        parser.parseExpression(expression)
        return parser.value.roundToInt()
    }

}