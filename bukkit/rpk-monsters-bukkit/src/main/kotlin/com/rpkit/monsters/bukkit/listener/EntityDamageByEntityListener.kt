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

package com.rpkit.monsters.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.expression.RPKExpressionService
import com.rpkit.core.service.Services
import com.rpkit.monsters.bukkit.RPKMonstersBukkit
import com.rpkit.monsters.bukkit.monsterlevel.RPKMonsterLevelService
import com.rpkit.monsters.bukkit.monsterstat.RPKMonsterStatServiceImpl
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.stats.bukkit.stat.RPKStatService
import com.rpkit.stats.bukkit.stat.RPKStatVariableService
import org.bukkit.entity.Arrow
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent


class EntityDamageByEntityListener(private val plugin: RPKMonstersBukkit) : Listener {

    @EventHandler
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        if (plugin.config.getBoolean("monsters.${event.entityType}.ignored", plugin.config.getBoolean("monsters.default.ignored"))) {
            return
        }
        var attacker: LivingEntity? = null
        var defender: LivingEntity? = null
        val damager = event.damager
        val attackType = if (damager is Projectile) {
            val shooter = damager.shooter
            if (shooter is LivingEntity) {
                attacker = shooter
            }
            if (damager is Arrow) {
                "bow"
            } else {
                "projectile"
            }
        } else {
            if (damager is LivingEntity) {
                attacker = damager
            }
            "melee"
        }
        val entity = event.entity
        if (entity is LivingEntity) {
            defender = entity
        }
        val attack = if (attacker != null) {
            if (attacker is Player) {
                getPlayerStat(attacker, attackType, "attack")
            } else {
                getEntityStat(attacker, attackType, "attack")
            }
        } else {
            1.0
        }
        val defence = if (defender != null) {
            if (defender is Player) {
                getPlayerStat(defender, attackType, "defence")
            } else {
                getEntityStat(defender, attackType, "defence")
            }
        } else {
            1.0
        }
        if (attacker != null && defender != null) {
            event.damage = getDamage(event.damage, attack, defence)
        }
        if (defender != null) {
            if (defender !is Player) {
                val monsterStatService = Services[RPKMonsterStatServiceImpl::class.java] ?: return
                monsterStatService.setMonsterNameplate(defender, health = defender.health - event.finalDamage)
            }
        }
    }

    private fun getPlayerStat(player: Player, type: String, stat: String): Double {
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return 0.0
        val characterService = Services[RPKCharacterService::class.java] ?: return 0.0
        val statService = Services[RPKStatService::class.java] ?: return 0.0
        val statVariableService = Services[RPKStatVariableService::class.java] ?: return 0.0
        val expressionService = Services[RPKExpressionService::class.java] ?: return 0.0
        val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(player) ?: return 0.0
        val character = characterService.getPreloadedActiveCharacter(minecraftProfile) ?: return 0.0
        val expression = expressionService.createExpression(plugin.config.getString("stats.$type.$stat") ?: return 0.0)
        val statVariables = statVariableService.statVariables
        return expression.parseDouble(statService.stats.map { it.name.value to it.get(character, statVariables) }.toMap()) ?: 0.0
    }

    private fun getEntityStat(entity: LivingEntity, type: String, stat: String): Double {
        val monsterLevelService = Services[RPKMonsterLevelService::class.java] ?: return 0.0
        val expressionService = Services[RPKExpressionService::class.java] ?: return 0.0
        val expression = expressionService.createExpression(plugin.config.getString("monsters.${entity.type}.stats.$type.$stat")
            ?: plugin.config.getString("monsters.default.stats.$type.$stat") ?: return 0.0)
        return expression.parseDouble(mapOf(
            "level" to monsterLevelService.getMonsterLevel(entity).toDouble(),
            "monsterType" to "\"${entity.type}\""
        )) ?: 0.0
    }

    private fun getDamage(originalDamage: Double, attack: Double, defence: Double): Double {
        val expressionService = Services[RPKExpressionService::class.java] ?: return 0.0
        val expression = expressionService.createExpression(plugin.config.getString("damage") ?: return 0.0)
        return expression.parseDouble(mapOf(
            "damage" to originalDamage,
            "attack" to attack,
            "defence" to defence
        )) ?: 0.0
    }

}