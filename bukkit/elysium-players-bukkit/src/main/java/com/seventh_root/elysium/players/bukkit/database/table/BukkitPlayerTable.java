package com.seventh_root.elysium.players.bukkit.database.table;

import com.seventh_root.elysium.core.database.Database;
import com.seventh_root.elysium.core.database.Table;
import com.seventh_root.elysium.players.bukkit.BukkitPlayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class BukkitPlayerTable extends Table<BukkitPlayer> {

    public BukkitPlayerTable(Database database) throws SQLException {
        super(database, BukkitPlayer.class);
    }

    @Override
    public void create() {
        try (
                Connection connection = getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS bukkit_player(" +
                                "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                                "minecraft_uuid VARCHAR(36)" +
                        ")"
                )
        ) {
            statement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public int insert(BukkitPlayer player) {
        try (
                Connection connection = getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO bukkit_player(minecraft_uuid) VALUES(?)",
                        RETURN_GENERATED_KEYS
                )
        ) {
            statement.setString(1, player.getBukkitPlayer().getUniqueId().toString());
            statement.executeUpdate();
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                int id = generatedKeys.getInt(1);
                player.setId(id);
                return id;
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return 0;
    }

    @Override
    public void update(BukkitPlayer player) {
        try (
                Connection connection = getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "UPDATE bukkit_player SET minecraft_uuid = ? WHERE id = ?"
                )
        ) {
            statement.setString(1, player.getBukkitPlayer().getUniqueId().toString());
            statement.setInt(2, player.getId());
            statement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public BukkitPlayer get(int id) {
        try (
                Connection connection = getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT id, minecraft_uuid FROM bukkit_player WHERE id = ?"
                )
        ) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return new BukkitPlayer(resultSet.getInt("id"), Bukkit.getOfflinePlayer(UUID.fromString(resultSet.getString("minecraft_uuid"))));
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public BukkitPlayer get(OfflinePlayer bukkitPlayer) {
        try (
                Connection connection = getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT id, minecraft_uuid FROM bukkit_player WHERE minecraft_uuid = ?"
                )
        ) {
            statement.setString(1, bukkitPlayer.getUniqueId().toString());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return new BukkitPlayer(resultSet.getInt("id"), Bukkit.getOfflinePlayer(UUID.fromString(resultSet.getString("minecraft_uuid"))));
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    @Override
    public void delete(BukkitPlayer player) {
        try (
                Connection connection = getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM bukkit_player WHERE id = ?"
                )
        ) {
            statement.setInt(1, player.getId());
            statement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

}
