/*
 * Copyright 2022 Ren Binden
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

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.bukkit.listener.registerListeners
import com.rpkit.core.database.Database
import com.rpkit.core.database.DatabaseConnectionProperties
import com.rpkit.core.database.DatabaseMigrationProperties
import com.rpkit.core.database.UnsupportedDatabaseDialectException
import com.rpkit.core.plugin.RPKPlugin
import com.rpkit.core.service.Services
import com.rpkit.drink.bukkit.drink.RPKDrinkService
import com.rpkit.drinks.bukkit.database.table.RPKDrunkennessTable
import com.rpkit.drinks.bukkit.drink.RPKDrinkServiceImpl
import com.rpkit.drinks.bukkit.listener.PlayerItemConsumeListener
import com.rpkit.drinks.bukkit.listener.RPKCharacterDeleteListener
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bstats.bukkit.Metrics
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType.*
import org.bukkit.scheduler.BukkitRunnable
import java.io.File


class RPKDrinksBukkit : JavaPlugin(), RPKPlugin {

    lateinit var database: Database

    override fun onEnable() {
        System.setProperty("com.rpkit.drinks.bukkit.shadow.impl.org.jooq.no-logo", "true")
        System.setProperty("com.rpkit.drinks.bukkit.shadow.impl.org.jooq.no-tips", "true")

        Metrics(this, 4389)
        saveDefaultConfig()

        val databaseConfigFile = File(dataFolder, "database.yml")
        if (!databaseConfigFile.exists()) {
            saveResource("database.yml", false)
        }
        val databaseConfig = YamlConfiguration.loadConfiguration(databaseConfigFile)
        val databaseUrl = databaseConfig.getString("database.url")
        if (databaseUrl == null) {
            logger.severe("Database URL not set!")
            isEnabled = false
            return
        }
        val databaseUsername = databaseConfig.getString("database.username")
        val databasePassword = databaseConfig.getString("database.password")
        val databaseSqlDialect = databaseConfig.getString("database.dialect")
        val databaseMaximumPoolSize = databaseConfig.getInt("database.maximum-pool-size", 3)
        val databaseMinimumIdle = databaseConfig.getInt("database.minimum-idle", 3)
        if (databaseSqlDialect == null) {
            logger.severe("Database SQL dialect not set!")
            isEnabled = false
            return
        }
        database = Database(
                DatabaseConnectionProperties(
                        databaseUrl,
                        databaseUsername,
                        databasePassword,
                        databaseSqlDialect,
                        databaseMaximumPoolSize,
                        databaseMinimumIdle
                ),
                DatabaseMigrationProperties(
                        when (databaseSqlDialect) {
                            "MYSQL" -> "com/rpkit/drinks/migrations/mysql"
                            "SQLITE" -> "com/rpkit/drinks/migrations/sqlite"
                            else -> throw UnsupportedDatabaseDialectException("Unsupported database dialect $databaseSqlDialect")
                        },
                        "flyway_schema_history_drinks"
                ),
                classLoader
        )
        database.addTable(RPKDrunkennessTable(database, this))

        val drinkService = RPKDrinkServiceImpl(this)
        drinkService.drinks.forEach { server.addRecipe(it.recipe) }

        Services[RPKDrinkService::class.java] = drinkService
        object : BukkitRunnable() {
            override fun run() {
                val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
                val characterService = Services[RPKCharacterService::class.java]
                server.onlinePlayers.forEach { bukkitPlayer ->
                    val minecraftProfile = minecraftProfileService?.getPreloadedMinecraftProfile(bukkitPlayer) ?: return@forEach
                    val character = characterService?.getPreloadedActiveCharacter(minecraftProfile) ?: return@forEach
                    drinkService.getDrunkenness(character).thenAccept { drunkenness ->
                        server.scheduler.runTask(this@RPKDrinksBukkit, Runnable {
                            if (drunkenness > 0) {
                                if (drunkenness > 1000) {
                                    if (config.getBoolean("kill-characters"))
                                        character.isDead = true
                                    characterService.updateCharacter(character)
                                }
                                if (drunkenness >= 75) {
                                    bukkitPlayer.addPotionEffect(PotionEffect(POISON, 1200, drunkenness))
                                }
                                if (drunkenness >= 50) {
                                    bukkitPlayer.addPotionEffect(PotionEffect(BLINDNESS, 1200, drunkenness))
                                }
                                if (drunkenness >= 10) {
                                    bukkitPlayer.addPotionEffect(PotionEffect(WEAKNESS, 1200, drunkenness))
                                }
                                bukkitPlayer.addPotionEffect(PotionEffect(CONFUSION, 1200, drunkenness))
                                drinkService.setDrunkenness(character, drunkenness - 1)
                            }
                        })
                    }
                }
            }
        }.runTaskTimer(this, 1200, 1200)

        registerListeners()
    }

    private fun registerListeners() {
        registerListeners(
            PlayerItemConsumeListener(this),
            RPKCharacterDeleteListener(this)
        )
    }
}