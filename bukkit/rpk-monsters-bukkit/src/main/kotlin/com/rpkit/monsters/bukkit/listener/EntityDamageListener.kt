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

package com.rpkit.monsters.bukkit.listener

import com.rpkit.monsters.bukkit.RPKMonstersBukkit
import com.rpkit.monsters.bukkit.monsterstat.RPKMonsterStatProviderImpl
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent


class EntityDamageListener(private val plugin: RPKMonstersBukkit): Listener {

    @EventHandler
    fun onEntityDamage(event: EntityDamageEvent) {
        if (plugin.config.getBoolean("monsters.${event.entityType}.ignored", plugin.config.getBoolean("monsters.default.ignored"))) {
            return
        }
        val entity = event.entity
        if (entity is LivingEntity && entity !is Player) {
            val monsterStatProvider = plugin.core.serviceManager.getServiceProvider(RPKMonsterStatProviderImpl::class)
            monsterStatProvider.setMonsterNameplate(entity, health = entity.health - event.finalDamage)
        }
    }

}