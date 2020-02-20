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

import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.core.expression.function.addRPKitFunctions
import com.rpkit.monsters.bukkit.RPKMonstersBukkit
import com.rpkit.monsters.bukkit.monsterlevel.RPKMonsterLevelProvider
import com.rpkit.monsters.bukkit.monsterstat.RPKMonsterStatProviderImpl
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.bukkit.entity.Arrow
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.nfunk.jep.JEP


class EntityDamageByEntityListener(private val plugin: RPKMonstersBukkit): Listener {

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
                val monsterStatProvider = plugin.core.serviceManager.getServiceProvider(RPKMonsterStatProviderImpl::class)
                monsterStatProvider.setMonsterNameplate(defender, health = defender.health - event.finalDamage)
            }
        }
    }

    private fun getPlayerStat(player: Player, type: String, stat: String): Double {
        val expression = plugin.config.getString("stats.$type.$stat")
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(player) ?: return 0.0
        val character = characterProvider.getActiveCharacter(minecraftProfile) ?: return 0.0
        val parser = JEP()
        parser.addStandardConstants()
        parser.addStandardFunctions()
        parser.addRPKitFunctions()
        parser.addVariableAsObject("core", plugin.core)
        parser.addVariableAsObject("character", character)
        parser.parseExpression(expression)
        return parser.value
    }

    private fun getEntityStat(entity: LivingEntity, type: String, stat: String): Double {
        val expression = plugin.config.getString("monsters.${entity.type}.stats.$type.$stat")
                ?: plugin.config.getString("monsters.default.stats.$type.$stat")
        val monsterLevelProvider = plugin.core.serviceManager.getServiceProvider(RPKMonsterLevelProvider::class)
        val parser = JEP()
        parser.addStandardConstants()
        parser.addStandardFunctions()
        parser.addRPKitFunctions()
        parser.addVariableAsObject("core", plugin.core)
        parser.addVariable("level", monsterLevelProvider.getMonsterLevel(entity).toDouble())
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