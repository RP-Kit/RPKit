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

package com.rpkit.drinks.bukkit

import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.drinks.bukkit.database.table.RPKDrunkennessTable
import com.rpkit.drinks.bukkit.drink.RPKDrinkProviderImpl
import com.rpkit.drinks.bukkit.listener.PlayerItemConsumeListener
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.bstats.bukkit.Metrics
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable


class RPKDrinksBukkit: RPKBukkitPlugin() {

    override fun onEnable() {
        Metrics(this)
        saveDefaultConfig()
        val drinkProvider = RPKDrinkProviderImpl(this)
        drinkProvider.drinks.forEach { server.addRecipe(it.recipe) }
        serviceProviders = arrayOf(
                drinkProvider
        )
        object: BukkitRunnable() {
            override fun run() {
                val minecraftProfileProvider = core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
                val characterProvider = core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                server.onlinePlayers.forEach { bukkitPlayer ->
                    val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitPlayer) ?: return@forEach
                    val character = characterProvider.getActiveCharacter(minecraftProfile) ?: return@forEach
                    val drunkenness = drinkProvider.getDrunkenness(character)
                    if (drunkenness > 0) {
                        if (drunkenness > 1000) {
                            if (config.getBoolean("kill-characters"))
                                character.isDead = true
                            characterProvider.updateCharacter(character)
                        }
                        if (drunkenness >= 75) {
                            bukkitPlayer.addPotionEffect(PotionEffect(PotionEffectType.POISON, 1200, drunkenness))
                        }
                        if (drunkenness >= 50) {
                            bukkitPlayer.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 1200, drunkenness))
                        }
                        if (drunkenness >= 10) {
                            bukkitPlayer.addPotionEffect(PotionEffect(PotionEffectType.WEAKNESS, 1200, drunkenness))
                        }
                        bukkitPlayer.addPotionEffect(PotionEffect(PotionEffectType.CONFUSION, 1200, drunkenness))
                        drinkProvider.setDrunkenness(character, drunkenness - 1)
                    }
                }
            }
        }.runTaskTimer(this, 1200, 1200)
    }

    override fun registerListeners() {
        registerListeners(
                PlayerItemConsumeListener(this)
        )
    }

    override fun createTables(database: Database) {
        database.addTable(RPKDrunkennessTable(database, this))
    }
}