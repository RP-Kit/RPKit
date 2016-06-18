package com.seventh_root.elysium.characters.bukkit

import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacterProvider
import com.seventh_root.elysium.characters.bukkit.character.field.*
import com.seventh_root.elysium.characters.bukkit.command.character.CharacterCommand
import com.seventh_root.elysium.characters.bukkit.command.gender.GenderCommand
import com.seventh_root.elysium.characters.bukkit.command.race.RaceCommand
import com.seventh_root.elysium.characters.bukkit.database.table.ElysiumCharacterTable
import com.seventh_root.elysium.characters.bukkit.database.table.ElysiumGenderTable
import com.seventh_root.elysium.characters.bukkit.database.table.ElysiumRaceTable
import com.seventh_root.elysium.characters.bukkit.gender.ElysiumGenderProvider
import com.seventh_root.elysium.characters.bukkit.listener.PlayerDeathListener
import com.seventh_root.elysium.characters.bukkit.listener.PlayerInteractEntityListener
import com.seventh_root.elysium.characters.bukkit.listener.PlayerJoinListener
import com.seventh_root.elysium.characters.bukkit.listener.PlayerMoveListener
import com.seventh_root.elysium.characters.bukkit.race.ElysiumRaceProvider
import com.seventh_root.elysium.core.bukkit.plugin.ElysiumBukkitPlugin
import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.service.ServiceProvider
import java.sql.SQLException

class ElysiumCharactersBukkit: ElysiumBukkitPlugin() {

    private lateinit var characterProvider: ElysiumCharacterProvider
    private lateinit var genderProvider: ElysiumGenderProvider
    private lateinit var raceProvider: ElysiumRaceProvider
    private lateinit var characterCardFieldProvider: ElysiumCharacterCardFieldProvider
    override lateinit var serviceProviders: Array<ServiceProvider>

    override fun onEnable() {
        saveDefaultConfig()
        characterProvider = ElysiumCharacterProvider(this)
        genderProvider = ElysiumGenderProvider(this)
        raceProvider = ElysiumRaceProvider(this)
        characterCardFieldProvider = ElysiumCharacterCardFieldProvider()
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
        database.addTable(ElysiumGenderTable(database))
        database.addTable(ElysiumRaceTable(database))
        database.addTable(ElysiumCharacterTable(database, this))
    }
}
