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
import com.rpkit.economy.bukkit.currency.RPKCurrency
import com.rpkit.economy.bukkit.currency.RPKCurrencyName
import com.rpkit.economy.bukkit.currency.RPKCurrencyService
import com.rpkit.experience.bukkit.experience.RPKExperienceService
import com.rpkit.monsters.bukkit.RPKMonstersBukkit
import com.rpkit.monsters.bukkit.monsterlevel.RPKMonsterLevelService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack


class EntityDeathListener(private val plugin: RPKMonstersBukkit) : Listener {

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
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(damager) ?: return
        val characterService = Services[RPKCharacterService::class.java] ?: return
        val character = characterService.getActiveCharacter(minecraftProfile) ?: return
        val experienceService = Services[RPKExperienceService::class.java] ?: return
        val experience = getExperience(event.entity)
        experienceService.setExperience(character, experienceService.getExperience(character) + experience)
        damager.sendMessage(plugin.messages["experience-gained", mapOf(
                "experience_gained" to experience.toString(),
                "experience" to (experienceService.getExperience(character) - experienceService.getExperienceNeededForLevel(experienceService.getLevel(character))).toString(),
                "required_experience" to experienceService.getExperienceNeededForLevel(experienceService.getLevel(character) + 1).toString()
        )])
        val currencyService = Services[RPKCurrencyService::class.java] ?: return
        val moneyConfigSection = plugin.config.getConfigurationSection("monsters.${event.entityType}.money")
                ?: plugin.config.getConfigurationSection("monsters.default.money")
        moneyConfigSection?.getKeys(false)
                ?.forEach { currencyName ->
                    val currency = currencyService.getCurrency(RPKCurrencyName(currencyName))
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
    }

    private fun getExperience(entity: LivingEntity): Int {
        val expressionService = Services[RPKExpressionService::class.java] ?: return 0
        val expression = expressionService.createExpression(plugin.config.getString("monsters.${entity.type}.experience", plugin.config.getString("monsters.default.experience")) ?: return 0)
        return expression.parseInt(mapOf(
            "level" to (Services[RPKMonsterLevelService::class.java]?.getMonsterLevel(entity)?.toDouble() ?: 1.0)
        )) ?: 0
    }

    private fun getMoney(entity: LivingEntity, currency: RPKCurrency): Int {
        val expressionService = Services[RPKExpressionService::class.java] ?: return 0
        val expression = expressionService.createExpression(plugin.config.getString("monsters.${entity.type}.money.${currency.name}", plugin.config.getString("monsters.default.money.${currency.name}")) ?: return 0)
        return expression.parseInt(mapOf(
            "level" to (Services[RPKMonsterLevelService::class.java]?.getMonsterLevel(entity)?.toDouble() ?: 1.0)
        )) ?: 0
    }

}