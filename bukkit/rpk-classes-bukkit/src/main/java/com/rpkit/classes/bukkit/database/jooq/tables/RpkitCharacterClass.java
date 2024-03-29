/*
 * This file is generated by jOOQ.
 */
package com.rpkit.classes.bukkit.database.jooq.tables;


import com.rpkit.classes.bukkit.database.jooq.Keys;
import com.rpkit.classes.bukkit.database.jooq.RpkitClasses;
import com.rpkit.classes.bukkit.database.jooq.tables.records.RpkitCharacterClassRecord;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row2;
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
public class RpkitCharacterClass extends TableImpl<RpkitCharacterClassRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of
     * <code>rpkit_classes.rpkit_character_class</code>
     */
    public static final RpkitCharacterClass RPKIT_CHARACTER_CLASS = new RpkitCharacterClass();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<RpkitCharacterClassRecord> getRecordType() {
        return RpkitCharacterClassRecord.class;
    }

    /**
     * The column <code>rpkit_classes.rpkit_character_class.character_id</code>.
     */
    public final TableField<RpkitCharacterClassRecord, Integer> CHARACTER_ID = createField(DSL.name("character_id"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>rpkit_classes.rpkit_character_class.class_name</code>.
     */
    public final TableField<RpkitCharacterClassRecord, String> CLASS_NAME = createField(DSL.name("class_name"), SQLDataType.VARCHAR(256).nullable(false), this, "");

    private RpkitCharacterClass(Name alias, Table<RpkitCharacterClassRecord> aliased) {
        this(alias, aliased, null);
    }

    private RpkitCharacterClass(Name alias, Table<RpkitCharacterClassRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>rpkit_classes.rpkit_character_class</code> table
     * reference
     */
    public RpkitCharacterClass(String alias) {
        this(DSL.name(alias), RPKIT_CHARACTER_CLASS);
    }

    /**
     * Create an aliased <code>rpkit_classes.rpkit_character_class</code> table
     * reference
     */
    public RpkitCharacterClass(Name alias) {
        this(alias, RPKIT_CHARACTER_CLASS);
    }

    /**
     * Create a <code>rpkit_classes.rpkit_character_class</code> table reference
     */
    public RpkitCharacterClass() {
        this(DSL.name("rpkit_character_class"), null);
    }

    public <O extends Record> RpkitCharacterClass(Table<O> child, ForeignKey<O, RpkitCharacterClassRecord> key) {
        super(child, key, RPKIT_CHARACTER_CLASS);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : RpkitClasses.RPKIT_CLASSES;
    }

    @Override
    public UniqueKey<RpkitCharacterClassRecord> getPrimaryKey() {
        return Keys.KEY_RPKIT_CHARACTER_CLASS_PRIMARY;
    }

    @Override
    public RpkitCharacterClass as(String alias) {
        return new RpkitCharacterClass(DSL.name(alias), this);
    }

    @Override
    public RpkitCharacterClass as(Name alias) {
        return new RpkitCharacterClass(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public RpkitCharacterClass rename(String name) {
        return new RpkitCharacterClass(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public RpkitCharacterClass rename(Name name) {
        return new RpkitCharacterClass(name, null);
    }

    // -------------------------------------------------------------------------
    // Row2 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row2<Integer, String> fieldsRow() {
        return (Row2) super.fieldsRow();
    }
}
