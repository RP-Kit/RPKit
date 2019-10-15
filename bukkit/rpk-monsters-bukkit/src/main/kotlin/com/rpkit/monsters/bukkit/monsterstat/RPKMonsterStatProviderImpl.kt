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

package com.rpkit.monsters.bukkit.monsterstat

import com.rpkit.monsters.bukkit.RPKMonstersBukkit
import com.rpkit.monsters.bukkit.monsterlevel.RPKMonsterLevelProvider
import org.bukkit.ChatColor
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import javax.script.ScriptContext
import javax.script.ScriptEngineManager


class RPKMonsterStatProviderImpl(private val plugin: RPKMonstersBukkit): RPKMonsterStatProvider {

    override fun getMonsterHealth(monster: LivingEntity): Double {
        return monster.health
    }

    override fun setMonsterHealth(monster: LivingEntity, health: Double) {
        monster.health = health
        setMonsterNameplate(monster, health = health)
    }

    override fun getMonsterMaxHealth(monster: LivingEntity): Double {
        val monsterNameplate = monster.customName
        val monsterLevelProvider = plugin.core.serviceManager.getServiceProvider(RPKMonsterLevelProvider::class)
        val maxHealth = if (monsterNameplate != null) {
            val maxHealthString = Regex("${ChatColor.RED}❤ ${ChatColor.WHITE}(\\d+\\.\\d+)/(\\d+\\.\\d+)")
                    .find(monsterNameplate)
                    ?.groupValues
                    ?.get(2)
            if (maxHealthString != null) {
                try {
                    maxHealthString.toDouble()
                } catch (ignored: NumberFormatException) {
                    calculateMonsterMaxHealth(monster.type, monsterLevelProvider.getMonsterLevel(monster))
                }
            } else {
                calculateMonsterMaxHealth(monster.type, monsterLevelProvider.getMonsterLevel(monster))
            }
        } else {
            calculateMonsterMaxHealth(monster.type, monsterLevelProvider.getMonsterLevel(monster))
        }
        monster.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = maxHealth
        return monster.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: monster.health
    }

    override fun getMonsterMinDamageMultiplier(monster: LivingEntity): Double {
        return getMonsterMinDamage(monster) / (monster.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)?.value ?: 1.0)
    }

    override fun setMonsterMinDamageMultiplier(monster: LivingEntity, minDamageMultiplier: Double) {
        setMonsterNameplate(monster, minDamageMultiplier = minDamageMultiplier)
    }

    override fun getMonsterMinDamage(monster: LivingEntity): Double {
        val monsterNameplate = monster.customName
        if (monsterNameplate != null) {
            val minDamage = Regex("${ChatColor.RED}⚔ ${ChatColor.WHITE}(\\d+\\.\\d+)-(\\d+\\.\\d+)")
                    .find(monsterNameplate)
                    ?.groupValues
                    ?.get(1)
            if (minDamage != null) {
                try {
                    return minDamage.toDouble()
                } catch (ignored: NumberFormatException) {}
            }
        }
        val monsterLevelProvider = plugin.core.serviceManager.getServiceProvider(RPKMonsterLevelProvider::class)
        return calculateMonsterMinDamageMultiplier(monster.type, monsterLevelProvider.getMonsterLevel(monster)) * (monster.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)?.value ?: 1.0)
    }

    override fun setMonsterMinDamage(monster: LivingEntity, minDamage: Double) {
        setMonsterNameplate(monster, minDamage = minDamage)
    }

    override fun getMonsterMaxDamageMultiplier(monster: LivingEntity): Double {
        return getMonsterMaxDamage(monster) / (monster.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)?.value ?: 1.0)
    }

    override fun setMonsterMaxDamageMultiplier(monster: LivingEntity, maxDamageMultiplier: Double) {
        setMonsterNameplate(monster, maxDamageMultiplier = maxDamageMultiplier)
    }

    override fun getMonsterMaxDamage(monster: LivingEntity): Double {
        val monsterNameplate = monster.customName
        if (monsterNameplate != null) {
            val maxDamage = Regex("${ChatColor.RED}⚔ ${ChatColor.WHITE}(\\d+\\.\\d+)-(\\d+\\.\\d+)")
                    .find(monsterNameplate)
                    ?.groupValues
                    ?.get(2)
            if (maxDamage != null) {
                try {
                    return maxDamage.toDouble()
                } catch (ignored: NumberFormatException) {}
            }
        }
        val monsterLevelProvider = plugin.core.serviceManager.getServiceProvider(RPKMonsterLevelProvider::class)
        return calculateMonsterMaxDamageMultiplier(monster.type, monsterLevelProvider.getMonsterLevel(monster)) * (monster.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)?.value ?: 1.0)
    }

    override fun setMonsterMaxDamage(monster: LivingEntity, maxDamage: Double) {
        setMonsterNameplate(monster, maxDamage = maxDamage)
    }

    fun calculateMonsterMaxHealth(entityType: EntityType, level: Int): Double {
        val script = plugin.config.getString("monsters.$entityType.max-health")
        val engineManager = ScriptEngineManager()
        val engine = engineManager.getEngineByName("nashorn")
        val bindings = engine.createBindings()
        bindings["level"] = level
        engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE)
        return (engine.eval(script) as Number).toDouble()
    }

    fun calculateMonsterMinDamageMultiplier(entityType: EntityType, level: Int): Double {
        val script = plugin.config.getString("monsters.$entityType.min-damage-multiplier")
        val engineManager = ScriptEngineManager()
        val engine = engineManager.getEngineByName("nashorn")
        val bindings = engine.createBindings()
        bindings["level"] = level
        engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE)
        return (engine.eval(script) as Number).toDouble()
    }

    fun calculateMonsterMaxDamageMultiplier(entityType: EntityType, level: Int): Double {
        val script = plugin.config.getString("monsters.$entityType.max-damage-multiplier")
        val engineManager = ScriptEngineManager()
        val engine = engineManager.getEngineByName("nashorn")
        val bindings = engine.createBindings()
        bindings["level"] = level
        engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE)
        return (engine.eval(script) as Number).toDouble()
    }

    fun setMonsterNameplate(
            monster: LivingEntity,
            level: Int = plugin.core.serviceManager.getServiceProvider(RPKMonsterLevelProvider::class).getMonsterLevel(monster),
            health: Double = getMonsterHealth(monster),
            maxHealth: Double = getMonsterMaxHealth(monster),
            minDamageMultiplier: Double = getMonsterMinDamageMultiplier(monster),
            maxDamageMultiplier: Double = getMonsterMaxDamageMultiplier(monster),
            minDamage: Double = (minDamageMultiplier * (monster.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)?.value ?: 1.0)),
            maxDamage: Double = (maxDamageMultiplier * (monster.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)?.value ?: 1.0))
    ) {
        val monsterName = monster.type.toString().toLowerCase().replace('_', ' ')
        val formattedHealth = String.format("%.2f", health)
        val formattedMaxHealth = String.format("%.2f", maxHealth)
        val formattedMinDamage = String.format("%.2f", minDamage)
        val formattedMaxDamage = String.format("%.2f", maxDamage)
        val nameplate = "${ChatColor.WHITE}Lv${ChatColor.YELLOW}$level " +
                "$monsterName " +
                "${ChatColor.RED}❤ ${ChatColor.WHITE}$formattedHealth/$formattedMaxHealth " +
                "${ChatColor.RED}⚔ ${ChatColor.WHITE}$formattedMinDamage-$formattedMaxDamage"
        monster.customName = nameplate
        monster.isCustomNameVisible = true
    }

}