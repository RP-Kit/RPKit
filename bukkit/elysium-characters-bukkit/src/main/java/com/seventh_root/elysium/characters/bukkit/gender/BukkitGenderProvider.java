package com.seventh_root.elysium.characters.bukkit.gender;

import com.seventh_root.elysium.api.character.GenderProvider;
import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit;
import com.seventh_root.elysium.characters.bukkit.database.table.BukkitGenderTable;
import com.seventh_root.elysium.core.database.Table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BukkitGenderProvider implements GenderProvider<BukkitGender> {

    private final ElysiumCharactersBukkit plugin;

    public BukkitGenderProvider(ElysiumCharactersBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public BukkitGender getGender(int id) {
        return plugin.getCore().getDatabase().getTable(BukkitGender.class).get(id);
    }

    public BukkitGender getGender(String name) {
        Table<BukkitGender> table = plugin.getCore().getDatabase().getTable(BukkitGender.class);
        if (table instanceof BukkitGenderTable) {
            BukkitGenderTable bukkitGenderTable = (BukkitGenderTable) table;
            return bukkitGenderTable.get(name);
        }
        return null;
    }

    @Override
    public Collection<? extends BukkitGender> getGenders() {
        try (
                Connection connection = plugin.getCore().getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT id, name FROM bukkit_gender"
                )
        ) {
            ResultSet resultSet = statement.executeQuery();
            List<BukkitGender> genders =new ArrayList<>();
            while (resultSet.next()) {
                genders.add(new BukkitGender(resultSet.getInt("id"), resultSet.getString("name")));
            }
            return genders;
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    @Override
    public void addGender(BukkitGender gender) {
        plugin.getCore().getDatabase().getTable(BukkitGender.class).insert(gender);
    }

    @Override
    public void removeGender(BukkitGender gender) {
        plugin.getCore().getDatabase().getTable(BukkitGender.class).delete(gender);
    }

}
