/*
 * This file is generated by jOOQ.
 */
package com.rpkit.locks.bukkit.database.jooq.tables;


import com.rpkit.locks.bukkit.database.jooq.Keys;
import com.rpkit.locks.bukkit.database.jooq.RpkitLocks;
import com.rpkit.locks.bukkit.database.jooq.tables.records.RpkitPlayerGettingKeyRecord;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row1;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class RpkitPlayerGettingKey extends TableImpl<RpkitPlayerGettingKeyRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of
     * <code>rpkit_locks.rpkit_player_getting_key</code>
     */
    public static final RpkitPlayerGettingKey RPKIT_PLAYER_GETTING_KEY = new RpkitPlayerGettingKey();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<RpkitPlayerGettingKeyRecord> getRecordType() {
        return RpkitPlayerGettingKeyRecord.class;
    }

    /**
     * The column
     * <code>rpkit_locks.rpkit_player_getting_key.minecraft_profile_id</code>.
     */
    public final TableField<RpkitPlayerGettingKeyRecord, Integer> MINECRAFT_PROFILE_ID = createField(DSL.name("minecraft_profile_id"), SQLDataType.INTEGER.nullable(false), this, "");

    private RpkitPlayerGettingKey(Name alias, Table<RpkitPlayerGettingKeyRecord> aliased) {
        this(alias, aliased, null);
    }

    private RpkitPlayerGettingKey(Name alias, Table<RpkitPlayerGettingKeyRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>rpkit_locks.rpkit_player_getting_key</code> table
     * reference
     */
    public RpkitPlayerGettingKey(String alias) {
        this(DSL.name(alias), RPKIT_PLAYER_GETTING_KEY);
    }

    /**
     * Create an aliased <code>rpkit_locks.rpkit_player_getting_key</code> table
     * reference
     */
    public RpkitPlayerGettingKey(Name alias) {
        this(alias, RPKIT_PLAYER_GETTING_KEY);
    }

    /**
     * Create a <code>rpkit_locks.rpkit_player_getting_key</code> table
     * reference
     */
    public RpkitPlayerGettingKey() {
        this(DSL.name("rpkit_player_getting_key"), null);
    }

    public <O extends Record> RpkitPlayerGettingKey(Table<O> child, ForeignKey<O, RpkitPlayerGettingKeyRecord> key) {
        super(child, key, RPKIT_PLAYER_GETTING_KEY);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : RpkitLocks.RPKIT_LOCKS;
    }

    @Override
    public UniqueKey<RpkitPlayerGettingKeyRecord> getPrimaryKey() {
        return Keys.KEY_RPKIT_PLAYER_GETTING_KEY_PRIMARY;
    }

    @Override
    public RpkitPlayerGettingKey as(String alias) {
        return new RpkitPlayerGettingKey(DSL.name(alias), this);
    }

    @Override
    public RpkitPlayerGettingKey as(Name alias) {
        return new RpkitPlayerGettingKey(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public RpkitPlayerGettingKey rename(String name) {
        return new RpkitPlayerGettingKey(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public RpkitPlayerGettingKey rename(Name name) {
        return new RpkitPlayerGettingKey(name, null);
    }

    // -------------------------------------------------------------------------
    // Row1 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row1<Integer> fieldsRow() {
        return (Row1) super.fieldsRow();
    }
}
