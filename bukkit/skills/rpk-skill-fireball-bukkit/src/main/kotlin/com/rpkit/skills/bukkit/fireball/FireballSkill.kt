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

package com.rpkit.skills.bukkit.fireball

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.skills.bukkit.skills.RPKSkill
import com.rpkit.skills.bukkit.skills.RPKSkillPointProvider
import com.rpkit.skills.bukkit.skills.RPKSkillTypeProvider
import org.bukkit.Bukkit
import org.bukkit.entity.Fireball

class FireballSkill(private val plugin: RPKFireballSkillBukkit) : RPKSkill {

    override val name = plugin.config.getString("name", "fireball") ?: "fireball"
    override val manaCost = plugin.config.getInt("mana-cost", 2)
    override val cooldown = plugin.config.getInt("cooldown", 10)

    override fun use(character: RPKCharacter) {
        val minecraftProfile = character.minecraftProfile ?: return
        val bukkitPlayer = Bukkit.getPlayer(minecraftProfile.minecraftUUID) ?: return
        if (!bukkitPlayer.isOnline) return
        val bukkitOnlinePlayer = bukkitPlayer.player
        bukkitOnlinePlayer?.launchProjectile(Fireball::class.java)
    }

    override fun canUse(character: RPKCharacter): Boolean {
        val skillTypeProvider = plugin.core.serviceManager.getServiceProvider(RPKSkillTypeProvider::class)
        val skillPointProvider = plugin.core.serviceManager.getServiceProvider(RPKSkillPointProvider::class)
        return plugin.config.getConfigurationSection("requirements")
                ?.getKeys(false)
                ?.mapNotNull { skillTypeName ->
                    val skillType = skillTypeProvider.getSkillType(skillTypeName)
                    if (skillType != null) {
                        val skillPoints = plugin.config.getInt("requirements.${skillType.name}")
                        skillType to skillPoints
                    } else {
                        null
                    }
                }
                ?.all { (skillType, requiredPoints) ->
                    skillPointProvider.getSkillPoints(character, skillType) >= requiredPoints
                } ?: false
    }
}