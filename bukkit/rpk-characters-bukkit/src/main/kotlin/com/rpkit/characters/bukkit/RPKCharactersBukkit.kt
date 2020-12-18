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

package com.rpkit.characters.bukkit

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.characters.bukkit.character.RPKCharacterServiceImpl
import com.rpkit.characters.bukkit.character.field.AgeField
import com.rpkit.characters.bukkit.character.field.DeadField
import com.rpkit.characters.bukkit.character.field.DescriptionField
import com.rpkit.characters.bukkit.character.field.FoodField
import com.rpkit.characters.bukkit.character.field.GenderField
import com.rpkit.characters.bukkit.character.field.HealthField
import com.rpkit.characters.bukkit.character.field.ManaField
import com.rpkit.characters.bukkit.character.field.MaxFoodField
import com.rpkit.characters.bukkit.character.field.MaxHealthField
import com.rpkit.characters.bukkit.character.field.MaxManaField
import com.rpkit.characters.bukkit.character.field.MaxThirstField
import com.rpkit.characters.bukkit.character.field.NameField
import com.rpkit.characters.bukkit.character.field.ProfileField
import com.rpkit.characters.bukkit.character.field.RPKCharacterCardFieldService
import com.rpkit.characters.bukkit.character.field.RPKCharacterCardFieldServiceImpl
import com.rpkit.characters.bukkit.character.field.RaceField
import com.rpkit.characters.bukkit.character.field.ThirstField
import com.rpkit.characters.bukkit.command.character.CharacterCommand
import com.rpkit.characters.bukkit.command.race.RaceCommand
import com.rpkit.characters.bukkit.database.table.RPKCharacterTable
import com.rpkit.characters.bukkit.database.table.RPKNewCharacterCooldownTable
import com.rpkit.characters.bukkit.database.table.RPKRaceTable
import com.rpkit.characters.bukkit.listener.PlayerDeathListener
import com.rpkit.characters.bukkit.listener.PlayerInteractEntityListener
import com.rpkit.characters.bukkit.listener.PlayerJoinListener
import com.rpkit.characters.bukkit.listener.PlayerMoveListener
import com.rpkit.characters.bukkit.messages.CharactersMessages
import com.rpkit.characters.bukkit.newcharactercooldown.RPKNewCharacterCooldownService
import com.rpkit.characters.bukkit.race.RPKRaceService
import com.rpkit.characters.bukkit.race.RPKRaceServiceImpl
import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.core.database.DatabaseConnectionProperties
import com.rpkit.core.database.DatabaseMigrationProperties
import com.rpkit.core.database.UnsupportedDatabaseDialectException
import com.rpkit.core.service.Services
import org.bstats.bukkit.Metrics
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

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
        messages.saveDefaultMessagesConfig()

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
        database.addTable(RPKRaceTable(database, this))
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
    }

    fun registerCommands() {
        getCommand("character")?.setExecutor(CharacterCommand(this))
        getCommand("race")?.setExecutor(RaceCommand(this))
    }

    fun registerListeners() {
        registerListeners(PlayerJoinListener(this), PlayerInteractEntityListener(this), PlayerMoveListener(this))
        if (config.getBoolean("characters.kill-character-on-death")) {
            registerListeners(PlayerDeathListener(this))
        }
    }
}
