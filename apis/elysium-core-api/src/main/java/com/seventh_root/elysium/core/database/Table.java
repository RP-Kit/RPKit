package com.seventh_root.elysium.core.database;

import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;

public abstract class Table<T extends TableRow> {

    private final Database database;
    private final String name;
    private final Class<T> type;

    public Table(Database database, String name, Class<T> type) {
        this.database = database;
        this.name = name;
        this.type = type;
        create();
    }

    public Table(Database database, Class<T> type) {
        this(database, UPPER_CAMEL.to(LOWER_UNDERSCORE, type.getSimpleName()), type);
    }

    public Database getDatabase() {
        return database;
    }

    public String getName() {
        return name;
    }

    public Class<T> getType() {
        return type;
    }

    public abstract void create();

    public abstract int insert(T object);

    public abstract void update(T object);

    public abstract T get(int id);

    public abstract void delete(T object);

}
