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

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.expression.function.addRPKitFunctions
import com.rpkit.core.service.Services
import com.rpkit.monsters.bukkit.RPKMonstersBukkit
import com.rpkit.monsters.bukkit.monsterlevel.RPKMonsterLevelService
import com.rpkit.monsters.bukkit.monsterstat.RPKMonsterStatServiceImpl
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileService
import com.rpkit.stats.bukkit.stat.RPKStatService
import com.rpkit.stats.bukkit.stat.RPKStatVariableService
import org.bukkit.entity.Arrow
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.nfunk.jep.JEP


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
                val monsterStatService = Services[RPKMonsterStatServiceImpl::class] ?: return
                monsterStatService.setMonsterNameplate(defender, health = defender.health - event.finalDamage)
            }
        }
    }

    private fun getPlayerStat(player: Player, type: String, stat: String): Double {
        val expression = plugin.config.getString("stats.$type.$stat")
        val minecraftProfileService = Services[RPKMinecraftProfileService::class] ?: return 0.0
        val characterService = Services[RPKCharacterService::class] ?: return 0.0
        val statService = Services[RPKStatService::class] ?: return 0.0
        val statVariableService = Services[RPKStatVariableService::class] ?: return 0.0
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(player) ?: return 0.0
        val character = characterService.getActiveCharacter(minecraftProfile) ?: return 0.0
        val statVariables = statVariableService.statVariables
        val parser = JEP()
        parser.addStandardConstants()
        parser.addStandardFunctions()
        parser.addRPKitFunctions()
        statService.stats.forEach {
            parser.addVariable(it.name, it.get(character, statVariables).toDouble())
        }
        parser.parseExpression(expression)
        return parser.value
    }

    private fun getEntityStat(entity: LivingEntity, type: String, stat: String): Double {
        val expression = plugin.config.getString("monsters.${entity.type}.stats.$type.$stat")
                ?: plugin.config.getString("monsters.default.stats.$type.$stat")
        val monsterLevelService = Services[RPKMonsterLevelService::class] ?: return 0.0
        val parser = JEP()
        parser.addStandardConstants()
        parser.addStandardFunctions()
        parser.addRPKitFunctions()
        parser.addVariable("level", monsterLevelService.getMonsterLevel(entity).toDouble())
        parser.addVariableAsObject("monster", entity)
        parser.parseExpression(expression)
        return parser.value
    }

    private fun getDamage(originalDamage: Double, attack: Double, defence: Double): Double {
        val expression = plugin.config.getString("damage")
        val parser = JEP()
        parser.addStandardConstants()
        parser.addStandardFunctions()
        parser.addRPKitFunctions()
        parser.addVariable("damage", originalDamage)
        parser.addVariable("attack", attack)
        parser.addVariable("defence", defence)
        parser.parseExpression(expression)
        return parser.value
    }

}