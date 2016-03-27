package com.seventh_root.elysium.characters.bukkit.database.table;

import com.seventh_root.elysium.characters.bukkit.gender.BukkitGender;
import com.seventh_root.elysium.core.database.Database;
import com.seventh_root.elysium.core.database.Table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class BukkitGenderTable extends Table<BukkitGender> {

    public BukkitGenderTable(Database database) throws SQLException {
        super(database, BukkitGender.class);
    }

    @Override
    public void create() {
        try (
                Connection connection = getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS bukkit_gender(" +
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
    public int insert(BukkitGender gender) {
        try (
                Connection connection = getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO bukkit_gender(name) VALUES(?)",
                        RETURN_GENERATED_KEYS
                )
        ) {
            statement.setString(1, gender.getName());
            statement.executeUpdate();
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                int id = generatedKeys.getInt(1);
                gender.setId(id);
                return id;
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return 0;
    }

    @Override
    public void update(BukkitGender gender) {
        try (
                Connection connection = getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "UPDATE bukkit_gender SET name = ? WHERE id = ?"
                )
        ) {
            statement.setString(1, gender.getName());
            statement.setInt(2, gender.getId());
            statement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public BukkitGender get(int id) {
        try (
                Connection connection = getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT id, name FROM bukkit_gender WHERE id = ?"
                )
        ) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return new BukkitGender(resultSet.getInt("id"), resultSet.getString("name"));
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public BukkitGender get(String name) {
        try (
                Connection connection = getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT id, name FROM bukkit_gender WHERE name = ?"
                )
        ) {
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return new BukkitGender(resultSet.getInt("id"), resultSet.getString("name"));
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    @Override
    public void delete(BukkitGender gender) {
        try (
                Connection connection = getDatabase().createConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM bukkit_gender WHERE id = ?"
                )
        ) {
            statement.setInt(1, gender.getId());
            statement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

}
