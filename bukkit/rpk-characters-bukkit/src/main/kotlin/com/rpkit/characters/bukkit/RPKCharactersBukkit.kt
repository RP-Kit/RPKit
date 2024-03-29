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

package com.rpkit.characters.bukkit

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.characters.bukkit.character.RPKCharacterServiceImpl
import com.rpkit.characters.bukkit.character.field.*
import com.rpkit.characters.bukkit.command.character.CharacterCommand
import com.rpkit.characters.bukkit.command.species.SpeciesCommand
import com.rpkit.characters.bukkit.database.table.RPKCharacterTable
import com.rpkit.characters.bukkit.database.table.RPKNewCharacterCooldownTable
import com.rpkit.characters.bukkit.listener.*
import com.rpkit.characters.bukkit.messages.CharactersMessages
import com.rpkit.characters.bukkit.newcharactercooldown.RPKNewCharacterCooldownService
import com.rpkit.characters.bukkit.placeholder.RPKCharactersPlaceholderExpansion
import com.rpkit.characters.bukkit.species.RPKSpeciesService
import com.rpkit.characters.bukkit.species.RPKSpeciesServiceImpl
import com.rpkit.characters.bukkit.web.CharactersWebAPI
import com.rpkit.core.bukkit.listener.registerListeners
import com.rpkit.core.database.Database
import com.rpkit.core.database.DatabaseConnectionProperties
import com.rpkit.core.database.DatabaseMigrationProperties
import com.rpkit.core.database.UnsupportedDatabaseDialectException
import com.rpkit.core.plugin.RPKPlugin
import com.rpkit.core.service.Services
import org.bstats.bukkit.Metrics
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.logging.Level
import kotlin.concurrent.thread
import kotlin.text.Charsets.UTF_8

/**
 * RPK characters plugin default implementation.
 */
class RPKCharactersBukkit : JavaPlugin(), RPKPlugin {

    lateinit var database: Database
    lateinit var messages: CharactersMessages

    private lateinit var characterService: RPKCharacterService
    private lateinit var speciesService: RPKSpeciesService
    private lateinit var characterCardFieldService: RPKCharacterCardFieldService
    private lateinit var newCharacterCooldownService: RPKNewCharacterCooldownService

    override fun onEnable() {
        System.setProperty("com.rpkit.characters.bukkit.shadow.impl.org.jooq.no-logo", "true")
        System.setProperty("com.rpkit.characters.bukkit.shadow.impl.org.jooq.no-tips", "true")

        Metrics(this, 4382)
        saveDefaultConfig()
        var configUpdated = false
        if (config.contains("races")) {
            config.set("species", config.get("races"))
            config.set("races", null)
            configUpdated = true
        }
        if (config.contains("characters.defaults.race")) {
            config.set("characters.defaults.species", config.get("characters.defaults.race"))
            config.set("characters.defaults.race", null)
            configUpdated = true
        }
        if (config.contains("characters.defaults.race-hidden")) {
            config.set("characters.defaults.species-hidden", config.get("characters.defaults.race-hidden"))
            config.set("characters.defaults.race-hidden", null)
            configUpdated = true
        }
        if (configUpdated) {
            saveConfig()
        }

        messages = CharactersMessages(this)

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
                    "MYSQL" -> "com/rpkit/characters/migrations/mysql"
                    "SQLITE" -> "com/rpkit/characters/migrations/sqlite"
                    else -> throw UnsupportedDatabaseDialectException("Unsupported database dialect $databaseSqlDialect")
                },
                "flyway_schema_history_characters"
            ),
            classLoader
        )
        database.addTable(RPKCharacterTable(database, this))
        database.addTable(RPKNewCharacterCooldownTable(database, this))

        characterService = RPKCharacterServiceImpl(this)
        speciesService = RPKSpeciesServiceImpl(this)
        characterCardFieldService = RPKCharacterCardFieldServiceImpl(this)
        newCharacterCooldownService = RPKNewCharacterCooldownService(this)

        Services[RPKCharacterService::class.java] = characterService
        Services[RPKSpeciesService::class.java] = speciesService
        Services[RPKCharacterCardFieldService::class.java] = characterCardFieldService
        Services[RPKNewCharacterCooldownService::class.java] = newCharacterCooldownService

        characterCardFieldService.addCharacterCardField(NameField(this))
        characterCardFieldService.addCharacterCardField(ProfileField(this))
        characterCardFieldService.addCharacterCardField(GenderField(this))
        characterCardFieldService.addCharacterCardField(AgeField(this))
        characterCardFieldService.addCharacterCardField(SpeciesField(this))
        characterCardFieldService.addCharacterCardField(DescriptionField(this))
        characterCardFieldService.addCharacterCardField(HeightField(this))
        characterCardFieldService.addCharacterCardField(WeightField(this))
        characterCardFieldService.addCharacterCardField(DeadField(this))
        characterCardFieldService.addCharacterCardField(HealthField())
        characterCardFieldService.addCharacterCardField(MaxHealthField())
        characterCardFieldService.addCharacterCardField(ManaField())
        characterCardFieldService.addCharacterCardField(MaxManaField())
        characterCardFieldService.addCharacterCardField(FoodField())
        characterCardFieldService.addCharacterCardField(MaxFoodField())
        characterCardFieldService.addCharacterCardField(ThirstField())
        characterCardFieldService.addCharacterCardField(MaxThirstField())

        registerCommands()
        registerListeners()

        saveDefaultWebConfig()
        if (getWebConfig().getBoolean("enabled")) {
            thread(
                name = "RPKit Characters Web API thread",
                contextClassLoader = classLoader
            ) { CharactersWebAPI(this).start() }
        }

        if (server.pluginManager.getPlugin("PlaceholderAPI") != null) {
            RPKCharactersPlaceholderExpansion(this).register()

            config.getConfigurationSection("placeholder-api.fields")
                ?.getKeys(false)
                ?.forEach { key ->
                    characterCardFieldService.addCharacterCardField(
                        PlaceholderAPIField(
                            this,
                            key,
                            config.getString("placeholder-api.fields.${key}") ?: ""
                        )
                    )
                }
        }

        if (config.getBoolean("characters.set-player-nameplate")) {
            if (server.pluginManager.getPlugin("ProtocolLib") != null) {
                logger.info("Detected ProtocolLib, enabling player nameplates")
            }
        }
    }

    private fun registerCommands() {
        getCommand("character")?.setExecutor(CharacterCommand(this))
        getCommand("species")?.setExecutor(SpeciesCommand(this))
    }

    private fun registerListeners() {
        registerListeners(
            PlayerJoinListener(this),
            PlayerInteractEntityListener(this),
            AsyncPlayerPreLoginListener(this),
            PlayerEditBookListener(this),
            PlayerQuitListener(),
            RPKMinecraftProfileDeleteListener(this),
            RPKProfileDeleteListener(this)
        )
        if (config.getBoolean("characters.strict-movement-prevention-when-dead")) {
            registerListeners(PlayerMoveListener(this))
        }
        if (config.getBoolean("characters.kill-character-on-death")) {
            registerListeners(PlayerDeathListener(this))
        }

//        ProtocolLibrary.getProtocolManager().addPacketListener(object : PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.PLAYER_INFO) {
//            override fun onPacketSending(event: PacketEvent) {
//                plugin.logger.info(event.packet.playerInfoAction.fields.zip(event.packet.playerInfoAction.values).toMap().toString())
//                plugin.logger.info(event.packet.playerInfoActions.)
//            }
//        })
    }

    private var webConfig: FileConfiguration? = null
    private val webConfigFile = File(dataFolder, "web.yml")

    fun getWebConfig(): FileConfiguration {
        return webConfig ?: reloadWebConfig()
    }

    fun reloadWebConfig(): FileConfiguration = YamlConfiguration.loadConfiguration(webConfigFile)
        .also { config ->
            val defConfigStream = getResource("web.yml")
            if (defConfigStream != null) {
                config.setDefaults(
                    YamlConfiguration.loadConfiguration(
                        InputStreamReader(
                            defConfigStream,
                            UTF_8
                        )
                    )
                )
            }
            webConfig = config
        }

    fun saveWebConfig() {
        try {
            getWebConfig().save(webConfigFile)
        } catch (exception: IOException) {
            logger.log(Level.SEVERE, "Could not save config to $webConfigFile", exception)
        }
    }

    fun saveDefaultWebConfig() {
        if (!webConfigFile.exists()) {
            saveResource("web.yml", false)
        }
    }
}
