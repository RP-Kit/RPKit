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

    private lateinit var characterService: RPKCharacterService
    private lateinit var raceService: RPKRaceService
    private lateinit var characterCardFieldService: RPKCharacterCardFieldService
    private lateinit var newCharacterCooldownService: RPKNewCharacterCooldownService

    override fun onEnable() {
        System.setProperty("com.rpkit.characters.bukkit.shadow.impl.org.jooq.no-logo", "true")

        Metrics(this, 4382)
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
    }

    override fun registerCommands() {
        getCommand("character")?.setExecutor(CharacterCommand(this))
        getCommand("race")?.setExecutor(RaceCommand(this))
    }

    override fun registerListeners() {
        registerListeners(PlayerJoinListener(this), PlayerInteractEntityListener(this), PlayerMoveListener(this))
        if (config.getBoolean("characters.kill-character-on-death")) {
            registerListeners(PlayerDeathListener(this))
        }
    }

    override fun setDefaultMessages() {
        messages.setDefault("character-usage", "&cUsage: /character [set|card|switch|list|new|delete]")
        messages.setDefault("character-set-usage", "&cUsage: /character set [name|gender|age|race|description|dead]")
        messages.setDefault("character-set-age-prompt", "&fWhat age is your character? &7(Type cancel to cancel)")
        messages.setDefault("character-set-age-invalid-validation", "&cAge must be between 0 and 10000 inclusive.")
        messages.setDefault("character-set-age-invalid-number", "&cAge must be a number.")
        messages.setDefault("character-set-age-valid", "&aAge set.")
        messages.setDefault("character-set-dead-prompt", "&fIs your character dead? &7(Type cancel to cancel)")
        messages.setDefault("character-set-dead-invalid-boolean", "&cThat's not a valid answer.")
        messages.setDefault("character-set-dead-valid", "&aDead set.")
        messages.setDefault("character-set-description-prompt", "&fPlease enter some text to append to your character's description, or type \"end\" to end. &7(Type cancel to cancel)")
        messages.setDefault("character-set-description-valid", "&aDescription set.")
        messages.setDefault("character-set-profile-prompt", "&fWhat profile do you want to assign this character to? &7(Type cancel to cancel)")
        messages.setDefault("character-set-profile-invalid-profile", "&cThere is no profile by that name.")
        messages.setDefault("character-set-profile-invalid-no-discriminator", "&cProfile name must be in the format \"name#discriminator\".")
        messages.setDefault("character-set-profile-invalid-discriminator", "&cDiscriminator must be a number.")
        messages.setDefault("character-set-profile-valid", "&aYour character was assigned to a different profile. Please create a new character or switch to an old one.")
        messages.setDefault("character-set-name-prompt", "&fWhat is your character's name? &7(Type cancel to cancel)")
        messages.setDefault("character-set-name-valid", "&aName set.")
        messages.setDefault("character-set-gender-prompt", "&fWhat is your character's gender? &7(Type cancel to cancel)")
        messages.setDefault("character-set-gender-not-set", "&cGender not set.")
        messages.setDefault("character-set-gender-valid", "&aGender set.")
        messages.setDefault("character-set-race-prompt", "&fWhat is your character's race? &7(Type cancel to cancel)")
        messages.setDefault("character-set-race-invalid-race", "&cThat's not a valid race.")
        messages.setDefault("character-set-race-valid", "&aRace set.")
        messages.setDefault("character-hide-usage", "&cUsage: /character hide [field]")
        messages.setDefault("character-hide-age-valid", "&aAge hidden.")
        messages.setDefault("character-hide-description-valid", "&aDescription hidden.")
        messages.setDefault("character-hide-profile-valid", "&aProfile hidden.")
        messages.setDefault("character-hide-name-valid", "&aName hidden.")
        messages.setDefault("character-hide-gender-valid", "&aGender hidden.")
        messages.setDefault("character-hide-race-valid", "&aRace hidden.")
        messages.setDefault("character-unhide-usage", "&cUsage: /character unhide [field]")
        messages.setDefault("character-unhide-age-valid", "&aAge unhidden.")
        messages.setDefault("character-unhide-description-valid", "&aDescription unhidden.")
        messages.setDefault("character-unhide-profile-valid", "&aProfile unhidden.")
        messages.setDefault("character-unhide-name-valid", "&aName unhidden.")
        messages.setDefault("character-unhide-gender-valid", "&aGender unhidden.")
        messages.setDefault("character-unhide-race-valid", "&aRace unhidden.")
        messages.setDefault("character-card-owner", listOf(
                "&7\$name (&a&l\$edit(name)&7/&a&l\$hide(name)&7)",
                "&7Profile: &f\$profile",
                "&7Gender: &f\$gender &7(&a&l\$edit(gender)&7/&a&l\$hide(gender)&7)",
                "&7Age: &f\$age &7(&a&l\$edit(age)&7/&a&l\$hide(age)&7)",
                "&7Race: &f\$race &7(&a&l\$edit(race)&7/&a&l\$hide(race)&7)",
                "&7Description: &f\$description &7(&a&l\$edit(description)&7/&a&l\$hide(description)&7)",
                "&7Dead: &f\$dead &7(&a&l\$edit(dead)&7)",
                "&7Health: &f\$health&7/&f\$max-health",
                "&7Food: &f\$food&7/&f\$max-food",
                "&7Thirst: &f\$thirst&7/&f\$max-thirst"
        ))
        messages.setDefault("character-card-not-owner", listOf(
                "&7\$name",
                "&7Profile: &f\$profile",
                "&7Gender: &f\$gender",
                "&7Age: &f\$age",
                "&7Race: &f\$race",
                "&7Description: &f\$description",
                "&7Dead: &f\$dead",
                "&7Health: &f\$health",
                "&7Food: &f\$food&7/&f\$max-food",
                "&7Thirst: &f\$thirst&7/&f\$max-thirst"
        ))
        messages.setDefault("character-list-title", "&fYour characters")
        messages.setDefault("character-list-item", "&f- \$character")
        messages.setDefault("character-switch-prompt", "&fWhat is the name of the character you would like to switch to? &7(Type cancel to cancel)")
        messages.setDefault("character-switch-invalid-character", "&cYou do not have a character by that name.")
        messages.setDefault("character-switch-invalid-character-other-account", "&cThat character is currently being played on another account.")
        messages.setDefault("character-switch-valid", "&aCharacter switched.")
        messages.setDefault("character-new-valid", "&aCharacter created.")
        messages.setDefault("character-new-invalid-cooldown", "&cYou may not create another character until the cooldown has passed.")
        messages.setDefault("character-delete-prompt", "&fWhat is the name of the character you would like to delete? &7(Type cancel to cancel)")
        messages.setDefault("character-delete-invalid-character", "&cYou do not have a character by that name.")
        messages.setDefault("character-delete-confirmation", "&fThis operation will be permanent, and you will not be able to recover your character. Are you sure you wish to continue?")
        messages.setDefault("character-delete-confirmation-invalid-boolean", "&cThat's not a valid answer.")
        messages.setDefault("character-delete-valid", "&aCharacter deleted.")
        messages.setDefault("race-usage", "&cUsage: /race [add|remove|list]")
        messages.setDefault("race-add-prompt", "&fWhat is the name of the race you would like to add? &7(Type cancel to cancel)")
        messages.setDefault("race-add-invalid-race", "&cThat race already exists.")
        messages.setDefault("race-add-valid", "&aRace added.")
        messages.setDefault("race-remove-prompt", "&fWhat is the name of the race you would like to remove? &7(Type cancel to cancel)")
        messages.setDefault("race-remove-invalid-race", "&cThat's not a valid race.")
        messages.setDefault("race-remove-valid", "&aRace removed.")
        messages.setDefault("race-list-title", "&fRaces")
        messages.setDefault("race-list-item", "&f- &7\$race")
        messages.setDefault("not-from-console", "&cYou may not use this command from console.")
        messages.setDefault("operation-cancelled", "&cOperation cancelled.")
        messages.setDefault("no-character", "&cYou do not currently have an active character. Please create one with /character new, or switch to an old one using /character switch.")
        messages.setDefault("no-character-other", "&cThis player does not currently have a character.")
        messages.setDefault("no-profile", "&cYour Minecraft profile is not linked to a profile. Please link it on the server's web UI.")
        messages.setDefault("no-minecraft-profile", "&cA Minecraft profile has not been created for you, or was unable to be retrieved. Please try relogging, and contact the server owner if this error persists.")
        messages.setDefault("no-permission-character-card-self", "&cYou do not have permission to view your own character card.")
        messages.setDefault("no-permission-character-card-other", "&cYou do not have permission to view other people's character cards.")
        messages.setDefault("no-permission-character-list", "&cYou do not have permission to view your character list.")
        messages.setDefault("no-permission-character-new", "&cYou do not have permission to create new characters.")
        messages.setDefault("no-permission-character-set-age", "&cYou do not have permission to set your character's age.")
        messages.setDefault("no-permission-character-set-dead", "&cYou do not have permission to set your character's dead state.")
        messages.setDefault("no-permission-character-set-dead-yes", "&cYou do not have permission to set your character to be dead.")
        messages.setDefault("no-permission-character-set-dead-no", "&cYou do not have permission to set your character to be not dead.")
        messages.setDefault("no-permission-character-set-description", "&cYou do not have permission to set your character's description.")
        messages.setDefault("no-permission-character-set-gender", "&cYou do not have permission to set your character's gender.")
        messages.setDefault("no-permission-character-set-name", "&cYou do not have permission to set your character's name.")
        messages.setDefault("no-permission-character-set-race", "&cYou do not have permission to set your character's race.")
        messages.setDefault("no-permission-character-hide-age", "&cYou do not have permission to hide your character's age.")
        messages.setDefault("no-permission-character-hide-description", "&cYou do not have permission to hide your character's description.")
        messages.setDefault("no-permission-character-hide-gender", "&cYou do not have permission to hide your character's gender.")
        messages.setDefault("no-permission-character-hide-name", "&cYou do not have permission to hide your character's name.")
        messages.setDefault("no-permission-character-hide-profile", "&cYou do not have permission to hide your character's profile.")
        messages.setDefault("no-permission-character-hide-race", "&cYou do not have permission to hide your character's race.")
        messages.setDefault("no-permission-character-unhide-age", "&cYou do not have permission to unhide your character's age.")
        messages.setDefault("no-permission-character-unhide-description", "&cYou do not have permission to unhide your character's description.")
        messages.setDefault("no-permission-character-unhide-gender", "&cYou do not have permission to unhide your character's gender.")
        messages.setDefault("no-permission-character-unhide-name", "&cYou do not have permission to unhide your character's name.")
        messages.setDefault("no-permission-character-unhide-profile", "&cYou do not have permission to unhide your character's profile.")
        messages.setDefault("no-permission-character-unhide-race", "&cYou do not have permission to unhide your character's race.")
        messages.setDefault("no-permission-character-switch", "&cYou do not have permission to switch characters.")
        messages.setDefault("no-permission-character-delete", "&cYou do not have permission to delete characters.")
        messages.setDefault("no-permission-gender-add", "&cYou do not have permission to add genders.")
        messages.setDefault("no-permission-gender-remove", "&cYou do not have permission to remove genders.")
        messages.setDefault("no-permission-gender-list", "&cYou do not have permission to list genders.")
        messages.setDefault("no-permission-race-add", "&cYou do not have permission to add races.")
        messages.setDefault("no-permission-race-remove", "&cYou do not have permission to remove races.")
        messages.setDefault("no-permission-race-list", "&cYou do not have permission to list races.")
        messages.setDefault("dead-character", "&cYou are dead and can not move.")
        messages.setDefault("no-profile-service", "&cThere is no profile service available.")
        messages.setDefault("no-minecraft-profile-service", "&cThere is no Minecraft profile service available.")
        messages.setDefault("no-character-service", "&cThere is no character service available.")
        messages.setDefault("no-character-card-field-service", "&cThere is no character card field service available.")
        messages.setDefault("no-new-character-cooldown-service", "&cThere is no new character cooldown service available.")
        messages.setDefault("no-race-service", "&cThere is no race service available.")
    }
}
