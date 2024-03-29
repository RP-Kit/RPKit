/*
 * This file is generated by jOOQ.
 */
package com.rpkit.professions.bukkit.database.jooq.tables;


import com.rpkit.professions.bukkit.database.jooq.Keys;
import com.rpkit.professions.bukkit.database.jooq.RpkitProfessions;
import com.rpkit.professions.bukkit.database.jooq.tables.records.RpkitProfessionHiddenRecord;

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
public class RpkitProfessionHidden extends TableImpl<RpkitProfessionHiddenRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of
     * <code>rpkit_professions.rpkit_profession_hidden</code>
     */
    public static final RpkitProfessionHidden RPKIT_PROFESSION_HIDDEN = new RpkitProfessionHidden();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<RpkitProfessionHiddenRecord> getRecordType() {
        return RpkitProfessionHiddenRecord.class;
    }

    /**
     * The column
     * <code>rpkit_professions.rpkit_profession_hidden.character_id</code>.
     */
    public final TableField<RpkitProfessionHiddenRecord, Integer> CHARACTER_ID = createField(DSL.name("character_id"), SQLDataType.INTEGER.nullable(false), this, "");

    private RpkitProfessionHidden(Name alias, Table<RpkitProfessionHiddenRecord> aliased) {
        this(alias, aliased, null);
    }

    private RpkitProfessionHidden(Name alias, Table<RpkitProfessionHiddenRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>rpkit_professions.rpkit_profession_hidden</code>
     * table reference
     */
    public RpkitProfessionHidden(String alias) {
        this(DSL.name(alias), RPKIT_PROFESSION_HIDDEN);
    }

    /**
     * Create an aliased <code>rpkit_professions.rpkit_profession_hidden</code>
     * table reference
     */
    public RpkitProfessionHidden(Name alias) {
        this(alias, RPKIT_PROFESSION_HIDDEN);
    }

    /**
     * Create a <code>rpkit_professions.rpkit_profession_hidden</code> table
     * reference
     */
    public RpkitProfessionHidden() {
        this(DSL.name("rpkit_profession_hidden"), null);
    }

    public <O extends Record> RpkitProfessionHidden(Table<O> child, ForeignKey<O, RpkitProfessionHiddenRecord> key) {
        super(child, key, RPKIT_PROFESSION_HIDDEN);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : RpkitProfessions.RPKIT_PROFESSIONS;
    }

    @Override
    public UniqueKey<RpkitProfessionHiddenRecord> getPrimaryKey() {
        return Keys.KEY_RPKIT_PROFESSION_HIDDEN_PRIMARY;
    }

    @Override
    public RpkitProfessionHidden as(String alias) {
        return new RpkitProfessionHidden(DSL.name(alias), this);
    }

    @Override
    public RpkitProfessionHidden as(Name alias) {
        return new RpkitProfessionHidden(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public RpkitProfessionHidden rename(String name) {
        return new RpkitProfessionHidden(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public RpkitProfessionHidden rename(Name name) {
        return new RpkitProfessionHidden(name, null);
    }

    // -------------------------------------------------------------------------
    // Row1 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row1<Integer> fieldsRow() {
        return (Row1) super.fieldsRow();
    }
}
