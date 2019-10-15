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

package com.rpkit.monsters.bukkit.monsterlevel

import com.rpkit.monsters.bukkit.RPKMonstersBukkit
import com.rpkit.monsters.bukkit.monsterspawnarea.RPKMonsterSpawnAreaProvider
import com.rpkit.monsters.bukkit.monsterstat.RPKMonsterStatProviderImpl
import org.bukkit.ChatColor.YELLOW
import org.bukkit.entity.LivingEntity
import kotlin.random.Random


class RPKMonsterLevelProviderImpl(private val plugin: RPKMonstersBukkit): RPKMonsterLevelProvider {

    override fun getMonsterLevel(monster: LivingEntity): Int {
        val monsterNameplate = monster.customName
        if (monsterNameplate != null) {
            val level = Regex("Lv${YELLOW}(\\d+)")
                    .find(monsterNameplate)
                    ?.groupValues?.get(1)
            if (level != null) {
                try {
                    return level.toInt()
                } catch (ignored: NumberFormatException) {}
            }
        }
        return calculateMonsterLevel(monster)
    }

    override fun setMonsterLevel(monster: LivingEntity, level: Int) {
        val monsterStatProvider = plugin.core.serviceManager.getServiceProvider(RPKMonsterStatProviderImpl::class)
        monsterStatProvider.setMonsterNameplate(monster, level = level)
    }

    fun calculateMonsterLevel(monster: LivingEntity): Int {
        val spawnAreaProvider = plugin.core.serviceManager.getServiceProvider(RPKMonsterSpawnAreaProvider::class)
        val spawnArea = spawnAreaProvider.getSpawnArea(monster.location)
        if (spawnArea != null) {
            return Random.nextInt(
                    spawnArea.getMinLevel(monster.type),
                    spawnArea.getMaxLevel(monster.type) + 1
            )
        }
        monster.remove()
        return 0
    }

}