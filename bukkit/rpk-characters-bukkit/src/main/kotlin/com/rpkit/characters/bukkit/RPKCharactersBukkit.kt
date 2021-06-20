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

package com.rpkit.characters.bukkit

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.characters.bukkit.character.RPKCharacterServiceImpl
import com.rpkit.characters.bukkit.character.field.*
import com.rpkit.characters.bukkit.command.character.CharacterCommand
import com.rpkit.characters.bukkit.command.race.RaceCommand
import com.rpkit.characters.bukkit.database.table.RPKCharacterTable
import com.rpkit.characters.bukkit.database.table.RPKNewCharacterCooldownTable
import com.rpkit.characters.bukkit.listener.*
import com.rpkit.characters.bukkit.messages.CharactersMessages
import com.rpkit.characters.bukkit.newcharactercooldown.RPKNewCharacterCooldownService
import com.rpkit.characters.bukkit.race.RPKRaceService
import com.rpkit.characters.bukkit.race.RPKRaceServiceImpl
import com.rpkit.characters.bukkit.web.CharactersWebAPI
import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.core.database.DatabaseConnectionProperties
import com.rpkit.core.database.DatabaseMigrationProperties
import com.rpkit.core.database.UnsupportedDatabaseDialectException
import com.rpkit.core.service.Services
import org.bstats.bukkit.Metrics
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.logging.Level
import kotlin.concurrent.thread
import kotlin.text.Charsets.UTF_8

/**
 * RPK characters plugin default implementation.
 */
class RPKCharactersBukkit : RPKBukkitPlugin() {

    lateinit var database: Database
    lateinit var messages: CharactersMessages

    private lateinit var characterService: RPKCharacterService
    private lateinit var raceService: RPKRaceService
    private lateinit var characterCardFieldService: RPKCharacterCardFieldService
    private lateinit var newCharacterCooldownService: RPKNewCharacterCooldownService

    override fun onEnable() {
        System.setProperty("com.rpkit.characters.bukkit.shadow.impl.org.jooq.no-logo", "true")

        Metrics(this, 4382)
        saveDefaultConfig()

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
        raceService = RPKRaceServiceImpl(this)
        characterCardFieldService = RPKCharacterCardFieldServiceImpl(this)
        newCharacterCooldownService = RPKNewCharacterCooldownService(this)

        Services[RPKCharacterService::class.java] = characterService
        Services[RPKRaceService::class.java] = raceService
        Services[RPKCharacterCardFieldService::class.java] = characterCardFieldService
        Services[RPKNewCharacterCooldownService::class.java] = newCharacterCooldownService

        characterCardFieldService.characterCardFields.add(NameField())
        characterCardFieldService.characterCardFields.add(ProfileField())
        characterCardFieldService.characterCardFields.add(GenderField())
        characterCardFieldService.characterCardFields.add(AgeField())
        characterCardFieldService.characterCardFields.add(RaceField())
        characterCardFieldService.characterCardFields.add(DescriptionField())
        characterCardFieldService.characterCardFields.add(DeadField())
        characterCardFieldService.characterCardFields.add(HealthField())
        characterCardFieldService.characterCardFields.add(MaxHealthField())
        characterCardFieldService.characterCardFields.add(ManaField())
        characterCardFieldService.characterCardFields.add(MaxManaField())
        characterCardFieldService.characterCardFields.add(FoodField())
        characterCardFieldService.characterCardFields.add(MaxFoodField())
        characterCardFieldService.characterCardFields.add(ThirstField())
        characterCardFieldService.characterCardFields.add(MaxThirstField())

        registerCommands()
        registerListeners()

        saveDefaultWebConfig()
        if (getWebConfig().getBoolean("enabled")) {
            thread(
                name = "RPKit Characters Web API thread",
                contextClassLoader = classLoader
            ) { CharactersWebAPI(this).start() }
        }
    }

    fun registerCommands() {
        getCommand("character")?.setExecutor(CharacterCommand(this))
        getCommand("race")?.setExecutor(RaceCommand(this))
    }

    fun registerListeners() {
        registerListeners(
            PlayerJoinListener(this),
            PlayerInteractEntityListener(this),
            AsyncPlayerPreLoginListener(this),
            PlayerQuitListener()
        )
        if (config.getBoolean("characters.strict-movement-prevention-when-dead")) {
            registerListeners(PlayerMoveListener(this))
        }
        if (config.getBoolean("characters.kill-character-on-death")) {
            registerListeners(PlayerDeathListener(this))
        }
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
