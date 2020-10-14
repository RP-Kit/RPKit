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

package com.rpkit.monsters.bukkit.listener

import com.rpkit.core.service.Services
import com.rpkit.monsters.bukkit.RPKMonstersBukkit
import com.rpkit.monsters.bukkit.monsterlevel.RPKMonsterLevelService
import com.rpkit.monsters.bukkit.monsterspawnarea.RPKMonsterSpawnAreaService
import com.rpkit.monsters.bukkit.monsterstat.RPKMonsterStatServiceImpl
import org.bukkit.attribute.Attribute
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent


class CreatureSpawnListener(private val plugin: RPKMonstersBukkit) : Listener {

    @EventHandler
    fun onCreatureSpawn(event: CreatureSpawnEvent) {
        val monster = event.entity
        if (plugin.config.getBoolean("monsters.${event.entityType}.ignored", plugin.config.getBoolean("monsters.default.ignored"))) {
            return
        }
        val monsterSpawnAreaService = Services[RPKMonsterSpawnAreaService::class] ?: return
        val spawnArea = monsterSpawnAreaService.getSpawnArea(event.location)
        if (spawnArea == null) {
            event.isCancelled = true
            return
        }
        if (!spawnArea.allowedMonsters.contains(event.entityType)) {
            event.isCancelled = true
            return
        }
        val monsterLevelService = Services[RPKMonsterLevelService::class] ?: return
        val monsterStatService = Services[RPKMonsterStatServiceImpl::class] ?: return
        val level = monsterLevelService.getMonsterLevel(monster)
        val health = monsterStatService.calculateMonsterMaxHealth(event.entityType, level)
        val maxHealthInstance = event.entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)
        val calculatedHealth = if (maxHealthInstance != null) {
            maxHealthInstance.baseValue = health
            event.entity.health = maxHealthInstance.value
            maxHealthInstance.value
        } else {
            health
        }
        val minDamage = monsterStatService.calculateMonsterMinDamageMultiplier(event.entityType, level)
        val maxDamage = monsterStatService.calculateMonsterMaxDamageMultiplier(event.entityType, level)
        monsterStatService.setMonsterNameplate(
                monster,
                level,
                calculatedHealth,
                calculatedHealth,
                minDamage,
                maxDamage
        )
    }

}