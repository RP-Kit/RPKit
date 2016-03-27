package com.seventh_root.elysium.characters.bukkit.database.table;

import com.seventh_root.elysium.characters.bukkit.race.BukkitRace;
import com.seventh_root.elysium.core.database.Database;
import com.seventh_root.elysium.core.database.Table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class BukkitRaceTable extends Table<BukkitRace> {

    public BukkitRaceTable(Database database) throws SQLException {
        super(database, BukkitRace.class);
    }

    @Override
    public void create() {
        try (
                Connection connection = getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS bukkit_race(" +
                                "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                                "name VARCHAR(256)" +
                        ")"
                )
        ) {
            statement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public int insert(BukkitRace race) {
        try (
                Connection connection = getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO bukkit_race(name) VALUES(?)",
                        RETURN_GENERATED_KEYS
                )
        ) {
            statement.setString(1, race.getName());
            statement.executeUpdate();
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                int id = generatedKeys.getInt(1);
                race.setId(id);
                return id;
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return 0;
    }

    @Override
    public void update(BukkitRace race) {
        try (
                Connection connection = getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "UPDATE bukkit_race SET name = ? WHERE id = ?"
                )
        ) {
            statement.setString(1, race.getName());
            statement.setInt(2, race.getId());
            statement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public BukkitRace get(int id) {
        try (
                Connection connection = getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT id, name FROM bukkit_race WHERE id = ?"
                )
        ) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return new BukkitRace(resultSet.getInt("id"), resultSet.getString("name"));
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public BukkitRace get(String name) {
        try (
                Connection connection = getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT id, name FROM bukkit_race WHERE name = ?"
                )
        ) {
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return new BukkitRace(resultSet.getInt("id"), resultSet.getString("name"));
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    @Override
    public void delete(BukkitRace race) {
        try (
                Connection connection = getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM bukkit_race WHERE id = ?"
                )
        ) {
            statement.setInt(1, race.getId());
            statement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

}
