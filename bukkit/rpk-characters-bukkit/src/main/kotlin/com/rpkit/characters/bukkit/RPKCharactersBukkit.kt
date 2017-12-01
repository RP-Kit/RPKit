/*
 * Copyright 2017 Ross Binden
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

import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.characters.bukkit.character.RPKCharacterProviderImpl
import com.rpkit.characters.bukkit.character.field.*
import com.rpkit.characters.bukkit.command.character.CharacterCommand
import com.rpkit.characters.bukkit.command.gender.GenderCommand
import com.rpkit.characters.bukkit.command.race.RaceCommand
import com.rpkit.characters.bukkit.database.table.RPKCharacterTable
import com.rpkit.characters.bukkit.database.table.RPKGenderTable
import com.rpkit.characters.bukkit.database.table.RPKNewCharacterCooldownTable
import com.rpkit.characters.bukkit.database.table.RPKRaceTable
import com.rpkit.characters.bukkit.gender.RPKGenderProvider
import com.rpkit.characters.bukkit.gender.RPKGenderProviderImpl
import com.rpkit.characters.bukkit.listener.PlayerDeathListener
import com.rpkit.characters.bukkit.listener.PlayerInteractEntityListener
import com.rpkit.characters.bukkit.listener.PlayerJoinListener
import com.rpkit.characters.bukkit.listener.PlayerMoveListener
import com.rpkit.characters.bukkit.newcharactercooldown.RPKNewCharacterCooldownProvider
import com.rpkit.characters.bukkit.race.RPKRaceProvider
import com.rpkit.characters.bukkit.race.RPKRaceProviderImpl
import com.rpkit.characters.bukkit.servlet.CharacterServlet
import com.rpkit.characters.bukkit.servlet.CharactersServlet
import com.rpkit.characters.bukkit.servlet.api.v1.CharacterAPIServlet
import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.core.web.NavigationLink
import java.sql.SQLException

/**
 * RPK characters plugin default implementation.
 */
class RPKCharactersBukkit: RPKBukkitPlugin() {

    private lateinit var characterProvider: RPKCharacterProvider
    private lateinit var genderProvider: RPKGenderProvider
    private lateinit var raceProvider: RPKRaceProvider
    private lateinit var characterCardFieldProvider: RPKCharacterCardFieldProvider
    private lateinit var newCharacterCooldownProvider: RPKNewCharacterCooldownProvider

    override fun onEnable() {
        saveDefaultConfig()
        characterProvider = RPKCharacterProviderImpl(this)
        genderProvider = RPKGenderProviderImpl(this)
        raceProvider = RPKRaceProviderImpl(this)
        characterCardFieldProvider = RPKCharacterCardFieldProviderImpl()
        newCharacterCooldownProvider = RPKNewCharacterCooldownProvider(this)
        serviceProviders = arrayOf(
                characterProvider,
                genderProvider,
                raceProvider,
                characterCardFieldProvider,
                newCharacterCooldownProvider
        )
        characterCardFieldProvider.characterCardFields.add(NameField())
        characterCardFieldProvider.characterCardFields.add(PlayerField())
        characterCardFieldProvider.characterCardFields.add(ProfileField())
        characterCardFieldProvider.characterCardFields.add(GenderField())
        characterCardFieldProvider.characterCardFields.add(AgeField())
        characterCardFieldProvider.characterCardFields.add(RaceField())
        characterCardFieldProvider.characterCardFields.add(DescriptionField())
        characterCardFieldProvider.characterCardFields.add(DeadField())
        characterCardFieldProvider.characterCardFields.add(HealthField())
        characterCardFieldProvider.characterCardFields.add(MaxHealthField())
        characterCardFieldProvider.characterCardFields.add(ManaField())
        characterCardFieldProvider.characterCardFields.add(MaxManaField())
        characterCardFieldProvider.characterCardFields.add(FoodField())
        characterCardFieldProvider.characterCardFields.add(MaxFoodField())
        characterCardFieldProvider.characterCardFields.add(ThirstField())
        characterCardFieldProvider.characterCardFields.add(MaxThirstField())
        servlets = arrayOf(
                CharactersServlet(this),
                CharacterServlet(this),
                CharacterAPIServlet(this)
        )
    }

    override fun onPostEnable() {
        core.web.navigationBar.add(NavigationLink("Characters", "/characters/"))
    }

    override fun registerCommands() {
        getCommand("character").executor = CharacterCommand(this)
        getCommand("gender").executor = GenderCommand(this)
        getCommand("race").executor = RaceCommand(this)
    }

    override fun registerListeners() {
        registerListeners(PlayerJoinListener(this), PlayerInteractEntityListener(this), PlayerMoveListener(this))
        if (config.getBoolean("characters.kill-character-on-death")) {
            registerListeners(PlayerDeathListener(this))
        }
    }

    @Throws(SQLException::class)
    override fun createTables(database: Database) {
        database.addTable(RPKGenderTable(database))
        database.addTable(RPKRaceTable(database))
        database.addTable(RPKCharacterTable(database, this))
        database.addTable(RPKNewCharacterCooldownTable(database, this))
    }

    override fun setDefaultMessages() {
        messages.setDefault("character-usage", "&cUsage: /character [set|card|switch|list|new|delete]")
        messages.setDefault("character-set-usage", "&cUsage: /character set [player|name|gender|age|race|description|dead]")
        messages.setDefault("character-set-age-prompt", "&fWhat age is your character? &7(Type cancel to cancel)")
        messages.setDefault("character-set-age-invalid-validation", "&cAge must be between 0 and 10000 inclusive.")
        messages.setDefault("character-set-age-invalid-number", "&cAge must be a number.")
        messages.setDefault("character-set-age-valid", "&aAge set.")
        messages.setDefault("character-set-dead-prompt", "&fIs your character dead? &7(Type cancel to cancel)")
        messages.setDefault("character-set-dead-invalid-boolean", "&cThat's not a valid answer.")
        messages.setDefault("character-set-dead-valid", "&aDead set.")
        messages.setDefault("character-set-description-prompt", "&fPlease enter some text to append to your character's description, or type \"end\" to end. &7(Type cancel to cancel)")
        messages.setDefault("character-set-description-valid", "&aDescription set.")
        messages.setDefault("character-set-player-prompt", "&fWhat player do you want to assign this character to? &7(Type cancel to cancel)")
        messages.setDefault("character-set-player-invalid-player", "&cThat player is not online.")
        messages.setDefault("character-set-player-valid", "&aYour character was assigned to a different player. You will now be moved to a new character.")
        messages.setDefault("character-set-profile-prompt", "&fWhat profile do you want to assign this character to? &7(Type cancel to cancel)")
        messages.setDefault("character-set-profile-invalid-profile", "&cThere is no profile by that name.")
        messages.setDefault("character-set-profile-valid", "&aYour character was assigned to a different profile. Please create a new character or switch to an old one.")
        messages.setDefault("character-set-name-prompt", "&fWhat is your character's name? &7(Type cancel to cancel)")
        messages.setDefault("character-set-name-valid", "&aName set.")
        messages.setDefault("character-set-gender-prompt", "&fWhat is your character's gender? &7(Type cancel to cancel)")
        messages.setDefault("character-set-gender-invalid-gender", "&cThat's not a valid gender.")
        messages.setDefault("character-set-gender-valid", "&aGender set.")
        messages.setDefault("character-set-race-prompt", "&fWhat is your character's race? &7(Type cancel to cancel)")
        messages.setDefault("character-set-race-invalid-race", "&cThat's not a valid race.")
        messages.setDefault("character-set-race-valid", "&aRace set.")
        messages.setDefault("character-hide-usage", "&cUsage: /character hide [field]")
        messages.setDefault("character-hide-age-valid", "&aAge hidden.")
        messages.setDefault("character-hide-description-valid", "&aDescription hidden.")
        messages.setDefault("character-hide-player-valid", "&aPlayer hidden.")
        messages.setDefault("character-hide-profile-valid", "&aProfile hidden.")
        messages.setDefault("character-hide-name-valid", "&aName hidden.")
        messages.setDefault("character-hide-gender-valid", "&aGender hidden.")
        messages.setDefault("character-hide-race-valid", "&aRace hidden.")
        messages.setDefault("character-unhide-usage", "&cUsage: /character unhide [field]")
        messages.setDefault("character-unhide-age-valid", "&aAge unhidden.")
        messages.setDefault("character-unhide-description-valid", "&aDescription unhidden.")
        messages.setDefault("character-unhide-player-valid", "&aPlayer unhidden.")
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
        messages.setDefault("gender-usage", "&cUsage: /gender [add|remove|list]")
        messages.setDefault("gender-add-prompt", "&fWhat is the name of the gender you would like to add? &7(Type cancel to cancel)")
        messages.setDefault("gender-add-invalid-gender", "&cThat gender already exists.")
        messages.setDefault("gender-add-valid", "&aGender added.")
        messages.setDefault("gender-remove-prompt", "&fWhat is the name of the gender you would like to remove? &7(Type cancel to cancel)")
        messages.setDefault("gender-remove-invalid-gender", "&cThat's not a valid gender.")
        messages.setDefault("gender-remove-valid", "&aGender removed.")
        messages.setDefault("gender-list-title", "&fGenders")
        messages.setDefault("gender-list-item", "&f- &7\$gender")
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
        messages.setDefault("no-permission-character-set-player", "&cYou do not have permission to transfer your character to another player.")
        messages.setDefault("no-permission-character-set-race", "&cYou do not have permission to set your character's race.")
        messages.setDefault("no-permission-character-hide-age", "&cYou do not have permission to hide your character's age.")
        messages.setDefault("no-permission-character-hide-description", "&cYou do not have permission to hide your character's description.")
        messages.setDefault("no-permission-character-hide-gender", "&cYou do not have permission to hide your character's gender.")
        messages.setDefault("no-permission-character-hide-name", "&cYou do not have permission to hide your character's name.")
        messages.setDefault("no-permission-character-hide-player", "&cYou do not have permission to hide your character's player.")
        messages.setDefault("no-permission-character-hide-profile", "&cYou do not have permission to hide your character's profile.")
        messages.setDefault("no-permission-character-hide-race", "&cYou do not have permission to hide your character's race.")
        messages.setDefault("no-permission-character-unhide-age", "&cYou do not have permission to unhide your character's age.")
        messages.setDefault("no-permission-character-unhide-description", "&cYou do not have permission to unhide your character's description.")
        messages.setDefault("no-permission-character-unhide-gender", "&cYou do not have permission to unhide your character's gender.")
        messages.setDefault("no-permission-character-unhide-name", "&cYou do not have permission to unhide your character's name.")
        messages.setDefault("no-permission-character-unhide-player", "&cYou do not have permission to unhide your character's player.")
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
    }
}
