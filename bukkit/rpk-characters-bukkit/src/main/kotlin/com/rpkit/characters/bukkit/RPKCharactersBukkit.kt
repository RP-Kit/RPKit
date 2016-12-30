/*
 * Copyright 2016 Ross Binden
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
import com.rpkit.characters.bukkit.database.table.RPKRaceTable
import com.rpkit.characters.bukkit.gender.RPKGenderProvider
import com.rpkit.characters.bukkit.gender.RPKGenderProviderImpl
import com.rpkit.characters.bukkit.listener.PlayerDeathListener
import com.rpkit.characters.bukkit.listener.PlayerInteractEntityListener
import com.rpkit.characters.bukkit.listener.PlayerJoinListener
import com.rpkit.characters.bukkit.listener.PlayerMoveListener
import com.rpkit.characters.bukkit.race.RPKRaceProvider
import com.rpkit.characters.bukkit.race.RPKRaceProviderImpl
import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import java.sql.SQLException

/**
 * RPK characters plugin default implementation.
 */
class RPKCharactersBukkit: RPKBukkitPlugin() {

    private lateinit var characterProvider: RPKCharacterProvider
    private lateinit var genderProvider: RPKGenderProvider
    private lateinit var raceProvider: RPKRaceProvider
    private lateinit var characterCardFieldProvider: RPKCharacterCardFieldProvider

    override fun onEnable() {
        saveDefaultConfig()
        characterProvider = RPKCharacterProviderImpl(this)
        genderProvider = RPKGenderProviderImpl(this)
        raceProvider = RPKRaceProviderImpl(this)
        characterCardFieldProvider = RPKCharacterCardFieldProviderImpl()
        serviceProviders = arrayOf(
                characterProvider,
                genderProvider,
                raceProvider,
                characterCardFieldProvider
        )
        characterCardFieldProvider.characterCardFields.add(NameField())
        characterCardFieldProvider.characterCardFields.add(PlayerField())
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
    }
}
