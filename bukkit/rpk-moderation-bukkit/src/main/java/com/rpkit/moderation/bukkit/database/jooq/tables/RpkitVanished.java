/*
 * This file is generated by jOOQ.
 */
package com.rpkit.moderation.bukkit.database.jooq.tables;


import com.rpkit.moderation.bukkit.database.jooq.Keys;
import com.rpkit.moderation.bukkit.database.jooq.RpkitModeration;
import com.rpkit.moderation.bukkit.database.jooq.tables.records.RpkitVanishedRecord;

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
public class RpkitVanished extends TableImpl<RpkitVanishedRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>rpkit_moderation.rpkit_vanished</code>
     */
    public static final RpkitVanished RPKIT_VANISHED = new RpkitVanished();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<RpkitVanishedRecord> getRecordType() {
        return RpkitVanishedRecord.class;
    }

    /**
     * The column
     * <code>rpkit_moderation.rpkit_vanished.minecraft_profile_id</code>.
     */
    public final TableField<RpkitVanishedRecord, Integer> MINECRAFT_PROFILE_ID = createField(DSL.name("minecraft_profile_id"), SQLDataType.INTEGER.nullable(false), this, "");

    private RpkitVanished(Name alias, Table<RpkitVanishedRecord> aliased) {
        this(alias, aliased, null);
    }

    private RpkitVanished(Name alias, Table<RpkitVanishedRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>rpkit_moderation.rpkit_vanished</code> table
     * reference
     */
    public RpkitVanished(String alias) {
        this(DSL.name(alias), RPKIT_VANISHED);
    }

    /**
     * Create an aliased <code>rpkit_moderation.rpkit_vanished</code> table
     * reference
     */
    public RpkitVanished(Name alias) {
        this(alias, RPKIT_VANISHED);
    }

    /**
     * Create a <code>rpkit_moderation.rpkit_vanished</code> table reference
     */
    public RpkitVanished() {
        this(DSL.name("rpkit_vanished"), null);
    }

    public <O extends Record> RpkitVanished(Table<O> child, ForeignKey<O, RpkitVanishedRecord> key) {
        super(child, key, RPKIT_VANISHED);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : RpkitModeration.RPKIT_MODERATION;
    }

    @Override
    public UniqueKey<RpkitVanishedRecord> getPrimaryKey() {
        return Keys.KEY_RPKIT_VANISHED_PRIMARY;
    }

    @Override
    public RpkitVanished as(String alias) {
        return new RpkitVanished(DSL.name(alias), this);
    }

    @Override
    public RpkitVanished as(Name alias) {
        return new RpkitVanished(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public RpkitVanished rename(String name) {
        return new RpkitVanished(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public RpkitVanished rename(Name name) {
        return new RpkitVanished(name, null);
    }

    // -------------------------------------------------------------------------
    // Row1 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row1<Integer> fieldsRow() {
        return (Row1) super.fieldsRow();
    }
}
