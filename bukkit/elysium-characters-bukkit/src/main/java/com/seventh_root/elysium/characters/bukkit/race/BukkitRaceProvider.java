package com.seventh_root.elysium.characters.bukkit.race;

import com.seventh_root.elysium.api.character.RaceProvider;
import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit;
import com.seventh_root.elysium.characters.bukkit.database.table.BukkitRaceTable;
import com.seventh_root.elysium.core.database.Table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BukkitRaceProvider implements RaceProvider<BukkitRace> {

    private final ElysiumCharactersBukkit plugin;

    public BukkitRaceProvider(ElysiumCharactersBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public BukkitRace getRace(int id) {
        return plugin.getCore().getDatabase().getTable(BukkitRace.class).get(id);
    }

    @Override
    public BukkitRace getRace(String name) {
        Table<BukkitRace> table = plugin.getCore().getDatabase().getTable(BukkitRace.class);
        if (table instanceof BukkitRaceTable) {
            BukkitRaceTable bukkitRaceTable = (BukkitRaceTable) table;
            return bukkitRaceTable.get(name);
        }
        return null;
    }

    @Override
    public Collection<? extends BukkitRace> getRaces() {
        try (
                Connection connection = plugin.getCore().getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT id, name FROM bukkit_race"
                )
        ) {
            ResultSet resultSet = statement.executeQuery();
            List<BukkitRace> races = new ArrayList<>();
            while (resultSet.next()) {
                races.add(new BukkitRace(resultSet.getInt("id"), resultSet.getString("name")));
            }
            return races;
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    @Override
    public void addRace(BukkitRace race) {
        plugin.getCore().getDatabase().getTable(BukkitRace.class).insert(race);
    }

    @Override
    public void removeRace(BukkitRace race) {
        plugin.getCore().getDatabase().getTable(BukkitRace.class).delete(race);
    }

}
