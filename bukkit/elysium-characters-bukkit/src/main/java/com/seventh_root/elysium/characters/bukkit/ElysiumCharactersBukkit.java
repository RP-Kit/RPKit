package com.seventh_root.elysium.characters.bukkit;

import com.seventh_root.elysium.api.character.CharacterProvider;
import com.seventh_root.elysium.api.character.GenderProvider;
import com.seventh_root.elysium.api.character.RaceProvider;
import com.seventh_root.elysium.characters.bukkit.character.BukkitCharacterProvider;
import com.seventh_root.elysium.characters.bukkit.command.character.CharacterCommand;
import com.seventh_root.elysium.characters.bukkit.command.gender.GenderCommand;
import com.seventh_root.elysium.characters.bukkit.command.race.RaceCommand;
import com.seventh_root.elysium.characters.bukkit.database.table.BukkitCharacterTable;
import com.seventh_root.elysium.characters.bukkit.database.table.BukkitGenderTable;
import com.seventh_root.elysium.characters.bukkit.database.table.BukkitRaceTable;
import com.seventh_root.elysium.characters.bukkit.gender.BukkitGenderProvider;
import com.seventh_root.elysium.characters.bukkit.listener.PlayerDeathListener;
import com.seventh_root.elysium.characters.bukkit.listener.PlayerInteractEntityListener;
import com.seventh_root.elysium.characters.bukkit.listener.PlayerMoveListener;
import com.seventh_root.elysium.characters.bukkit.race.BukkitRaceProvider;
import com.seventh_root.elysium.core.bukkit.plugin.ElysiumBukkitPlugin;
import com.seventh_root.elysium.core.database.Database;
import com.seventh_root.elysium.core.service.ServiceProvider;

import java.sql.SQLException;

public class ElysiumCharactersBukkit extends ElysiumBukkitPlugin {

    private CharacterProvider characterProvider;
    private GenderProvider genderProvider;
    private RaceProvider raceProvider;
    private ServiceProvider[] providers;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        characterProvider = new BukkitCharacterProvider(this);
        genderProvider = new BukkitGenderProvider(this);
        raceProvider = new BukkitRaceProvider(this);
        providers = new ServiceProvider[] {
                characterProvider,
                genderProvider,
                raceProvider
        };
    }

    public void registerCommands() {
        getCommand("character").setExecutor(new CharacterCommand(this));
        getCommand("gender").setExecutor(new GenderCommand(this));
        getCommand("race").setExecutor(new RaceCommand(this));
    }

    @Override
    public void registerListeners() {
        registerListeners(new PlayerInteractEntityListener(this), new PlayerMoveListener(this));
        if (getConfig().getBoolean("characters.kill-character-on-death")) {
            registerListeners(new PlayerDeathListener(this));
        }
    }

    @Override
    public void createTables(Database database) throws SQLException {
        database.addTable(new BukkitGenderTable(database));
        database.addTable(new BukkitRaceTable(database));
        database.addTable(new BukkitCharacterTable(database, this));
    }

    @Override
    public ServiceProvider[] getServiceProviders() {
        return providers;
    }
}
