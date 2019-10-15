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
import com.rpkit.core.exception.UnregisteredServiceException
import com.rpkit.economy.bukkit.currency.RPKCurrency
import com.rpkit.economy.bukkit.currency.RPKCurrencyProvider
import com.rpkit.experience.bukkit.experience.RPKExperienceProvider
import com.rpkit.monsters.bukkit.RPKMonstersBukkit
import com.rpkit.monsters.bukkit.monsterlevel.RPKMonsterLevelProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack
import javax.script.ScriptContext
import javax.script.ScriptEngineManager


class EntityDeathListener(private val plugin: RPKMonstersBukkit): Listener {

    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        event.droppedExp = 0
        if (plugin.config.getBoolean("monsters.${event.entityType}.ignored", plugin.config.getBoolean("monsters.default.ignored"))) {
            return
        }
        val lastDamageEvent = event.entity.lastDamageCause
        if (lastDamageEvent !is EntityDamageByEntityEvent) return
        val damager = lastDamageEvent.damager
        if (damager !is Player) return
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(damager) ?: return
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val character = characterProvider.getActiveCharacter(minecraftProfile) ?: return
        val experienceProvider = plugin.core.serviceManager.getServiceProvider(RPKExperienceProvider::class)
        val experience = getExperience(event.entity)
        experienceProvider.setExperience(character, experienceProvider.getExperience(character) + experience)
        damager.sendMessage(plugin.messages["experience-gained", mapOf(
                "experience-gained" to experience.toString(),
                "experience" to (experienceProvider.getExperience(character) - experienceProvider.getExperienceNeededForLevel(experienceProvider.getLevel(character))).toString(),
                "required-experience" to experienceProvider.getExperienceNeededForLevel(experienceProvider.getLevel(character) + 1).toString()
        )])
        try {
            val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
            val moneyConfigSection = plugin.config.getConfigurationSection("monsters.${event.entityType}.money")
                    ?: plugin.config.getConfigurationSection("monsters.default.money")
            moneyConfigSection?.getKeys(false)
                ?.forEach { currencyName ->
                    val currency = currencyProvider.getCurrency(currencyName)
                    if (currency != null) {
                        val amount = getMoney(event.entity, currency)
                        if (amount > 0) {
                            val coins = ItemStack(currency.material, amount)
                            val meta = coins.itemMeta ?: plugin.server.itemFactory.getItemMeta(coins.type) ?: return
                            meta.setDisplayName(currency.nameSingular)
                            coins.itemMeta = meta
                            event.entity.world.dropItem(event.entity.location, coins)
                        }
                    }
                }
        } catch (exception: UnregisteredServiceException) {}
    }

    private fun getExperience(entity: LivingEntity): Int {
        val script = plugin.config.getString("monsters.${entity.type}.experience", plugin.config.getString("monsters.default.experience"))
        val engineManager = ScriptEngineManager()
        val engine = engineManager.getEngineByName("nashorn")
        val bindings = engine.createBindings()
        bindings["level"] = plugin.core.serviceManager.getServiceProvider(RPKMonsterLevelProvider::class).getMonsterLevel(entity)
        engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE)
        return (engine.eval(script) as Number).toDouble().toInt()
    }

    private fun getMoney(entity: LivingEntity, currency: RPKCurrency): Int {
        val script = plugin.config.getString("monsters.${entity.type}.money.${currency.name}", plugin.config.getString("monsters.default.money.${currency.name}"))
        val engineManager = ScriptEngineManager()
        val engine = engineManager.getEngineByName("nashorn")
        val bindings = engine.createBindings()
        bindings["level"] = plugin.core.serviceManager.getServiceProvider(RPKMonsterLevelProvider::class).getMonsterLevel(entity)
        engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE)
        return (engine.eval(script) as Number).toDouble().toInt()
    }

}