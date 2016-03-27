package com.seventh_root.elysium.characters.bukkit.database.table;

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit;
import com.seventh_root.elysium.characters.bukkit.character.BukkitCharacter;
import com.seventh_root.elysium.characters.bukkit.gender.BukkitGenderProvider;
import com.seventh_root.elysium.characters.bukkit.race.BukkitRaceProvider;
import com.seventh_root.elysium.core.database.Database;
import com.seventh_root.elysium.core.database.Table;
import com.seventh_root.elysium.players.bukkit.BukkitPlayerProvider;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.INTEGER;

public class BukkitCharacterTable extends Table<BukkitCharacter> {

    private ElysiumCharactersBukkit plugin;

    public BukkitCharacterTable(Database database, ElysiumCharactersBukkit plugin) throws SQLException {
        super(database, BukkitCharacter.class);
        this.plugin = plugin;
    }

    @Override
    public void create() {
        try (
                Connection connection = getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS bukkit_character (" +
                                "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                                "player_id INTEGER," +
                                "name VARCHAR(256)," +
                                "gender_id INTEGER," +
                                "age INTEGER," +
                                "race_id INTEGER," +
                                "description VARCHAR(1024)," +
                                "dead BOOLEAN," +
                                "world VARCHAR(256)," +
                                "x DOUBLE," +
                                "y DOUBLE," +
                                "z DOUBLE," +
                                "yaw REAL," +
                                "pitch REAL," +
                                "helmet BLOB," +
                                "chestplate BLOB," +
                                "leggings BLOB," +
                                "boots BLOB," +
                                "inventory_contents BLOB," +
                                "health DOUBLE," +
                                "max_health DOUBLE," +
                                "mana INTEGER," +
                                "max_mana INTEGER," +
                                "food_level INTEGER," +
                                "thirst_level INTEGER," +
                                "FOREIGN KEY(player_id) REFERENCES bukkit_player(id)," +
                                "FOREIGN KEY(gender_id) REFERENCES bukkit_gender(id)," +
                                "FOREIGN KEY(race_id) REFERENCES bukkit_race(id)" +
                        ")"
                )
        ) {
            statement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        try (
                Connection connection = getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS player_character(" +
                                "player_id INTEGER," +
                                "character_id INTEGER," +
                                "UNIQUE(player_id)," +
                                "FOREIGN KEY(player_id) REFERENCES bukkit_player(id)," +
                                "FOREIGN KEY(character_id) REFERENCES bukkit_character(id)" +
                        ")"
                )
        ) {
            statement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public int insert(BukkitCharacter character) {
        try (
                Connection connection = getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO bukkit_character(player_id, name, gender_id, age, race_id, description, dead, world, x, y, z, yaw, pitch, helmet, chestplate, leggings, boots, inventory_contents, health, max_health, mana, max_mana, food_level, thirst_level) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        RETURN_GENERATED_KEYS
                )
        ) {
            statement.setInt(1, character.getPlayer().getId());
            statement.setString(2, character.getName());
            if (character.getGender() != null) {
                statement.setInt(3, character.getGender().getId());
            } else {
                statement.setNull(3, INTEGER);
            }
            statement.setInt(4, character.getAge());
            if (character.getRace() != null) {
                statement.setInt(5, character.getRace().getId());
            } else {
                statement.setNull(5, INTEGER);
            }
            statement.setString(6, character.getDescription());
            statement.setBoolean(7, character.isDead());
            statement.setString(8, character.getLocation().getWorld().getName());
            statement.setDouble(9, character.getLocation().getX());
            statement.setDouble(10, character.getLocation().getY());
            statement.setDouble(11, character.getLocation().getZ());
            statement.setFloat(12, character.getLocation().getYaw());
            statement.setFloat(13, character.getLocation().getPitch());
            statement.setBytes(14, serializeItemStack(character.getHelmet()));
            statement.setBytes(15, serializeItemStack(character.getChestplate()));
            statement.setBytes(16, serializeItemStack(character.getLeggings()));
            statement.setBytes(17, serializeItemStack(character.getBoots()));
            statement.setBytes(18, serializeInventory(character.getInventoryContents()));
            statement.setDouble(19, character.getHealth());
            statement.setDouble(20, character.getMaxHealth());
            statement.setInt(21, character.getMana());
            statement.setInt(22, character.getMaxMana());
            statement.setInt(23, character.getFoodLevel());
            statement.setInt(24, character.getThirstLevel());
            statement.executeUpdate();
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                int id = generatedKeys.getInt(1);
                character.setId(id);
                return id;
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return 0;
    }

    private byte[] serializeItemStack(ItemStack itemStack) {
        try (
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                BukkitObjectOutputStream bukkitObjectOutputStream = new BukkitObjectOutputStream(byteArrayOutputStream)
        ) {
            bukkitObjectOutputStream.writeObject(itemStack);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    private ItemStack deserializeItemStack(byte[] bytes) {
        try (
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                BukkitObjectInputStream bukkitObjectInputStream = new BukkitObjectInputStream(byteArrayInputStream)
        ) {
            return (ItemStack) bukkitObjectInputStream.readObject();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    private byte[] serializeInventory(ItemStack[] inventoryContents) {
        try (
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                BukkitObjectOutputStream bukkitObjectOutputStream = new BukkitObjectOutputStream(byteArrayOutputStream)
        ) {
            bukkitObjectOutputStream.writeObject(inventoryContents);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    private ItemStack[] deserializeInventory(byte[] bytes) {
        try (
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                BukkitObjectInputStream bukkitObjectInputStream = new BukkitObjectInputStream(byteArrayInputStream)
        ) {
            return (ItemStack[]) bukkitObjectInputStream.readObject();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    @Override
    public void update(BukkitCharacter character) {
        try (
                Connection connection = getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "UPDATE bukkit_character SET player_id = ?, name = ?, gender_id = ?, age = ?, race_id = ?, description = ?, dead = ?, world = ?, x = ?, y = ?, z = ?, yaw = ?, pitch = ?, helmet = ?, chestplate = ?, leggings = ?, boots = ?, inventory_contents = ?, health = ?, max_health = ?, mana = ?, max_mana = ?, food_level = ?, thirst_level = ? WHERE id = ?"
                )
        ) {
            statement.setInt(1, character.getPlayer().getId());
            statement.setString(2, character.getName());
            if (character.getGender() != null) {
                statement.setInt(3, character.getGender().getId());
            } else {
                statement.setNull(3, INTEGER);
            }
            statement.setInt(4, character.getAge());
            if (character.getRace() != null) {
                statement.setInt(5, character.getRace().getId());
            } else {
                statement.setNull(5, INTEGER);
            }
            statement.setString(6, character.getDescription());
            statement.setBoolean(7, character.isDead());
            statement.setString(8, character.getLocation().getWorld().getName());
            statement.setDouble(9, character.getLocation().getX());
            statement.setDouble(10, character.getLocation().getY());
            statement.setDouble(11, character.getLocation().getZ());
            statement.setFloat(12, character.getLocation().getYaw());
            statement.setFloat(13, character.getLocation().getPitch());
            statement.setBytes(14, serializeItemStack(character.getHelmet()));
            statement.setBytes(15, serializeItemStack(character.getChestplate()));
            statement.setBytes(16, serializeItemStack(character.getLeggings()));
            statement.setBytes(17, serializeItemStack(character.getBoots()));
            statement.setBytes(18, serializeInventory(character.getInventoryContents()));
            statement.setDouble(19, character.getHealth());
            statement.setDouble(20, character.getMaxHealth());
            statement.setInt(21, character.getMana());
            statement.setInt(22, character.getMaxMana());
            statement.setInt(23, character.getFoodLevel());
            statement.setInt(24, character.getThirstLevel());
            statement.setInt(25, character.getId());
            statement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public BukkitCharacter get(int id) {
        try (
                Connection connection = getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT id, player_id, name, gender_id, age, race_id, description, dead, world, x, y, z, yaw, pitch, helmet, chestplate, leggings, boots, inventory_contents, health, max_health, mana, max_mana, food_level, thirst_level FROM bukkit_character WHERE id = ?"
                )
        ) {
            BukkitPlayerProvider playerProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitPlayerProvider.class);
            BukkitGenderProvider genderProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitGenderProvider.class);
            BukkitRaceProvider raceProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitRaceProvider.class);
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return new BukkitCharacter.Builder(plugin)
                        .id(resultSet.getInt("id"))
                        .player(playerProvider.getPlayer(resultSet.getInt("player_id")))
                        .name(resultSet.getString("name"))
                        .gender(genderProvider.getGender(resultSet.getInt("gender_id")))
                        .age(resultSet.getInt("age"))
                        .race(raceProvider.getRace(resultSet.getInt("race_id")))
                        .description(resultSet.getString("description"))
                        .dead(resultSet.getBoolean("dead"))
                        .location(
                                new Location(
                                        Bukkit.getWorld(resultSet.getString("world")),
                                        resultSet.getDouble("x"),
                                        resultSet.getDouble("y"),
                                        resultSet.getDouble("z"),
                                        resultSet.getFloat("yaw"),
                                        resultSet.getFloat("pitch")
                                )
                        )
                        .helmet(deserializeItemStack(resultSet.getBytes("helmet")))
                        .chestplate(deserializeItemStack(resultSet.getBytes("chestplate")))
                        .leggings(deserializeItemStack(resultSet.getBytes("leggings")))
                        .boots(deserializeItemStack(resultSet.getBytes("boots")))
                        .inventoryContents(deserializeInventory(resultSet.getBytes("inventory_contents")))
                        .health(resultSet.getDouble("health"))
                        .maxHealth(resultSet.getDouble("max_health"))
                        .mana(resultSet.getInt("mana"))
                        .maxMana(resultSet.getInt("max_mana"))
                        .foodLevel(resultSet.getInt("food_level"))
                        .thirstLevel(resultSet.getInt("thirst_level"))
                        .build();
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    @Override
    public void delete(BukkitCharacter character) {
        try (
                Connection connection = getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM bukkit_character WHERE id = ?"
                )
        ) {
            statement.setInt(1, character.getId());
            statement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

}
