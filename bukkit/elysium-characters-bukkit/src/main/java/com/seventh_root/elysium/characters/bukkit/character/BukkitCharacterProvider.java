package com.seventh_root.elysium.characters.bukkit.character;

import com.seventh_root.elysium.api.character.CharacterProvider;
import com.seventh_root.elysium.api.player.ElysiumPlayer;
import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit;
import com.seventh_root.elysium.players.bukkit.BukkitPlayer;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BukkitCharacterProvider implements CharacterProvider<BukkitCharacter> {

    private final ElysiumCharactersBukkit plugin;

    public BukkitCharacterProvider(ElysiumCharactersBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public BukkitCharacter getCharacter(int id) {
        return plugin.getCore().getDatabase().getTable(BukkitCharacter.class).get(id);
    }

    @Override
    public BukkitCharacter getActiveCharacter(ElysiumPlayer player) {
        try (
                Connection connection = plugin.getCore().getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT character_id FROM player_character WHERE player_id = ?"
                )
        ) {
            statement.setInt(1, player.getId());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return getCharacter(resultSet.getInt("character_id"));
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    @Override
    public void setActiveCharacter(ElysiumPlayer player, BukkitCharacter character) {
        BukkitCharacter oldCharacter = getActiveCharacter(player);
        if (oldCharacter != null) {
            if (player instanceof BukkitPlayer) {
                OfflinePlayer offlineBukkitPlayer = ((BukkitPlayer) player).getBukkitPlayer();
                if (offlineBukkitPlayer.isOnline()) {
                    Player bukkitPlayer = offlineBukkitPlayer.getPlayer();
                    oldCharacter.setHelmet(bukkitPlayer.getInventory().getHelmet());
                    oldCharacter.setChestplate(bukkitPlayer.getInventory().getChestplate());
                    oldCharacter.setLeggings(bukkitPlayer.getInventory().getLeggings());
                    oldCharacter.setBoots(bukkitPlayer.getInventory().getBoots());
                    oldCharacter.setInventoryContents(bukkitPlayer.getInventory().getContents());
                    oldCharacter.setLocation(bukkitPlayer.getLocation());
                    oldCharacter.setHealth(bukkitPlayer.getHealth());
                    oldCharacter.setFoodLevel(bukkitPlayer.getFoodLevel());
                    updateCharacter(oldCharacter);
                }
            }
        }
        if (character != null) {
            try (
                    Connection connection = plugin.getCore().getDatabase().createConnection();
                    PreparedStatement statement = connection.prepareStatement(
                            "INSERT INTO player_character(player_id, character_id) VALUES(?, ?) ON DUPLICATE KEY UPDATE character_id = VALUES(character_id)"
                    )
            ) {
                statement.setInt(1, player.getId());
                statement.setInt(2, character.getId());
                statement.executeUpdate();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
            if (player instanceof BukkitPlayer) {
                OfflinePlayer offlineBukkitPlayer = ((BukkitPlayer) player).getBukkitPlayer();
                if (offlineBukkitPlayer.isOnline()) {
                    Player bukkitPlayer = offlineBukkitPlayer.getPlayer();
                    bukkitPlayer.getInventory().setHelmet(character.getHelmet());
                    bukkitPlayer.getInventory().setChestplate(character.getChestplate());
                    bukkitPlayer.getInventory().setLeggings(character.getLeggings());
                    bukkitPlayer.getInventory().setBoots(character.getBoots());
                    bukkitPlayer.getInventory().setContents(character.getInventoryContents());
                    bukkitPlayer.teleport(character.getLocation());
                    bukkitPlayer.setHealth(character.getHealth());
                    bukkitPlayer.setMaxHealth(character.getMaxHealth());
                    bukkitPlayer.setFoodLevel(character.getFoodLevel());
                }
            }
        } else if (oldCharacter != null) {
            try (
                    Connection connection = plugin.getCore().getDatabase().createConnection();
                    PreparedStatement statement = connection.prepareStatement(
                            "DELETE FROM player_character WHERE player_id = ? AND character_id = ?"
                    )
            ) {
                statement.setInt(1, player.getId());
                statement.setInt(2, oldCharacter.getId());
                statement.executeUpdate();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }
    }

    @Override
    public Collection<? extends BukkitCharacter> getCharacters(ElysiumPlayer player) {
        try (
                Connection connection = plugin.getCore().getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT id FROM bukkit_character WHERE player_id = ? ORDER BY id"
                )
        ) {
            statement.setInt(1, player.getId());
            ResultSet resultSet = statement.executeQuery();
            List<BukkitCharacter> characters = new ArrayList<>();
            while (resultSet.next()) {
                characters.add(getCharacter(resultSet.getInt("id")));
            }
            return characters;
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    @Override
    public int addCharacter(BukkitCharacter character) {
        return plugin.getCore().getDatabase().getTable(BukkitCharacter.class).insert(character);
    }

    @Override
    public void removeCharacter(BukkitCharacter character) {
        setActiveCharacter(character.getPlayer(), null);
        plugin.getCore().getDatabase().getTable(BukkitCharacter.class).delete(character);
    }

    @Override
    public void updateCharacter(BukkitCharacter character) {
        if (plugin.getConfig().getBoolean("characters.delete-character-on-death") && character.isDead()) {
            removeCharacter(character);
        } else {
            plugin.getCore().getDatabase().getTable(BukkitCharacter.class).update(character);
        }
    }

}
