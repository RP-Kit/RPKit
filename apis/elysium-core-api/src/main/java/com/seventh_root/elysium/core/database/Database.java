package com.seventh_root.elysium.core.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;

public class Database {

    private final String url;
    private final String userName;
    private final String password;
    private final Map<String, Table<?>> tables;

    public Database(String url, String userName, String password) {
        this.url = url;
        this.userName = userName;
        this.password = password;
        tables = new HashMap<>();
    }

    public Database(String url) {
        this(url, null, null);
    }

    public String getURL() {
        return url;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public Connection createConnection() throws SQLException {
        if (getUserName() == null && getPassword() == null) {
            return DriverManager.getConnection(getURL());
        } else {
            return DriverManager.getConnection(getURL(), getUserName(), getPassword());
        }
    }

    public void addTable(Table<?> table) {
        tables.put(table.getName(), table);
        table.create();
    }

    public Table<?> getTable(String name) {
        return tables.get(name);
    }

    @SuppressWarnings("unchecked")
    public <T extends TableRow> Table<T> getTable(Class<T> type) {
        Table<?> table = tables.get(UPPER_CAMEL.to(LOWER_UNDERSCORE, type.getSimpleName()));
        if (table != null)
            return (Table<T>) table;
        else
            return null;
    }

}
