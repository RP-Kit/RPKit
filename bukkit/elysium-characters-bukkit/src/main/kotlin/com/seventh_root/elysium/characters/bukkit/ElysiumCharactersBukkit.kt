package com.seventh_root.elysium.characters.bukkit

import com.seventh_root.elysium.api.character.CharacterProvider
import com.seventh_root.elysium.api.character.GenderProvider
import com.seventh_root.elysium.api.character.RaceProvider
import com.seventh_root.elysium.characters.bukkit.character.BukkitCharacter
import com.seventh_root.elysium.characters.bukkit.character.BukkitCharacterProvider
import com.seventh_root.elysium.characters.bukkit.command.character.CharacterCommand
import com.seventh_root.elysium.characters.bukkit.command.gender.GenderCommand
import com.seventh_root.elysium.characters.bukkit.command.race.RaceCommand
import com.seventh_root.elysium.characters.bukkit.database.table.BukkitCharacterTable
import com.seventh_root.elysium.characters.bukkit.database.table.BukkitGenderTable
import com.seventh_root.elysium.characters.bukkit.database.table.BukkitRaceTable
import com.seventh_root.elysium.characters.bukkit.gender.BukkitGender
import com.seventh_root.elysium.characters.bukkit.gender.BukkitGenderProvider
import com.seventh_root.elysium.characters.bukkit.listener.PlayerDeathListener
import com.seventh_root.elysium.characters.bukkit.listener.PlayerInteractEntityListener
import com.seventh_root.elysium.characters.bukkit.listener.PlayerMoveListener
import com.seventh_root.elysium.characters.bukkit.race.BukkitRace
import com.seventh_root.elysium.characters.bukkit.race.BukkitRaceProvider
import com.seventh_root.elysium.core.bukkit.plugin.ElysiumBukkitPlugin
import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.service.ServiceProvider
import java.sql.SQLException

class ElysiumCharactersBukkit : ElysiumBukkitPlugin() {

    private var characterProvider: CharacterProvider<BukkitCharacter>? = null
    private var genderProvider: GenderProvider<BukkitGender>? = null
    private var raceProvider: RaceProvider<BukkitRace>? = null
    override lateinit var serviceProviders: Array<ServiceProvider>

    override fun onEnable() {
        saveDefaultConfig()
        characterProvider = BukkitCharacterProvider(this)
        genderProvider = BukkitGenderProvider(this)
        raceProvider = BukkitRaceProvider(this)
        serviceProviders = arrayOf(
                characterProvider as CharacterProvider<BukkitCharacter>,
                genderProvider as GenderProvider<BukkitGender>,
                raceProvider as BukkitRaceProvider
        )
    }

    override fun registerCommands() {
        getCommand("character").executor = CharacterCommand(this)
        getCommand("gender").executor = GenderCommand(this)
        getCommand("race").executor = RaceCommand(this)
    }

    override fun registerListeners() {
        registerListeners(PlayerInteractEntityListener(this), PlayerMoveListener(this))
        if (config.getBoolean("characters.kill-character-on-death")) {
            registerListeners(PlayerDeathListener(this))
        }
    }

    @Throws(SQLException::class)
    override fun createTables(database: Database) {
        database.addTable(BukkitGenderTable(database))
        database.addTable(BukkitRaceTable(database))
        database.addTable(BukkitCharacterTable(database, this))
    }
}
