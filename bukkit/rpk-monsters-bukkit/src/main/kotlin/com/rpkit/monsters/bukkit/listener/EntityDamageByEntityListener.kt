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
import javax.script.ScriptContext
import javax.script.ScriptEngineManager


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
        val originalClassLoader = Thread.currentThread().contextClassLoader
        Thread.currentThread().contextClassLoader = plugin.javaClass.classLoader
        val script = plugin.config.getString("stats.$type.$stat")
        val engineManager = ScriptEngineManager()
        val engine = engineManager.getEngineByName("nashorn")
        val bindings = engine.createBindings()
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(player) ?: return 0.0
        val character = characterProvider.getActiveCharacter(minecraftProfile) ?: return 0.0
        bindings["core"] = plugin.core
        bindings["character"] = character
        engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE)
        val statValue = (engine.eval(script) as Number).toDouble()
        Thread.currentThread().contextClassLoader = originalClassLoader
        return statValue
    }

    private fun getEntityStat(entity: LivingEntity, type: String, stat: String): Double {
        val originalClassLoader = Thread.currentThread().contextClassLoader
        Thread.currentThread().contextClassLoader = plugin.javaClass.classLoader
        val script = plugin.config.getString("monsters.${entity.type}.stats.$type.$stat")
                ?: plugin.config.getString("monsters.default.stats.$type.$stat")
        val engineManager = ScriptEngineManager()
        val engine = engineManager.getEngineByName("nashorn")
        val bindings = engine.createBindings()
        val monsterLevelProvider = plugin.core.serviceManager.getServiceProvider(RPKMonsterLevelProvider::class)
        bindings["core"] = plugin.core
        bindings["level"] = monsterLevelProvider.getMonsterLevel(entity)
        bindings["monster"] = entity
        engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE)
        val statValue = (engine.eval(script) as Number).toDouble()
        Thread.currentThread().contextClassLoader = originalClassLoader
        return statValue
    }

    private fun getDamage(originalDamage: Double, attack: Double, defence: Double): Double {
        val script = plugin.config.getString("damage")
        val engineManager = ScriptEngineManager()
        val engine = engineManager.getEngineByName("nashorn")
        val bindings = engine.createBindings()
        bindings["damage"] = originalDamage
        bindings["attack"] = attack
        bindings["defence"] = defence
        engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE)
        return (engine.eval(script) as Number).toDouble()
    }

}