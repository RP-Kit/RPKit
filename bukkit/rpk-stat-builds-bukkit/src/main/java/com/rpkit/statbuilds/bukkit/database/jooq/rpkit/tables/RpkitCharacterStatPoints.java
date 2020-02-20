/*
 * This file is generated by jOOQ.
 */
package com.rpkit.statbuilds.bukkit.database.jooq.rpkit.tables;


import com.rpkit.statbuilds.bukkit.database.jooq.rpkit.Indexes;
import com.rpkit.statbuilds.bukkit.database.jooq.rpkit.Keys;
import com.rpkit.statbuilds.bukkit.database.jooq.rpkit.Rpkit;
import com.rpkit.statbuilds.bukkit.database.jooq.rpkit.tables.records.RpkitCharacterStatPointsRecord;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.11"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class RpkitCharacterStatPoints extends TableImpl<RpkitCharacterStatPointsRecord> {

    private static final long serialVersionUID = 569589914;

    /**
     * The reference instance of <code>rpkit.rpkit_character_stat_points</code>
     */
    public static final RpkitCharacterStatPoints RPKIT_CHARACTER_STAT_POINTS = new RpkitCharacterStatPoints();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<RpkitCharacterStatPointsRecord> getRecordType() {
        return RpkitCharacterStatPointsRecord.class;
    }

    /**
     * The column <code>rpkit.rpkit_character_stat_points.id</code>.
     */
    public final TableField<RpkitCharacterStatPointsRecord, Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER.nullable(false).identity(true), this, "");

    /**
     * The column <code>rpkit.rpkit_character_stat_points.character_id</code>.
     */
    public final TableField<RpkitCharacterStatPointsRecord, Integer> CHARACTER_ID = createField("character_id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>rpkit.rpkit_character_stat_points.stat_attribute</code>.
     */
    public final TableField<RpkitCharacterStatPointsRecord, String> STAT_ATTRIBUTE = createField("stat_attribute", org.jooq.impl.SQLDataType.VARCHAR(256).nullable(false), this, "");

    /**
     * The column <code>rpkit.rpkit_character_stat_points.points</code>.
     */
    public final TableField<RpkitCharacterStatPointsRecord, Integer> POINTS = createField("points", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * Create a <code>rpkit.rpkit_character_stat_points</code> table reference
     */
    public RpkitCharacterStatPoints() {
        this(DSL.name("rpkit_character_stat_points"), null);
    }

    /**
     * Create an aliased <code>rpkit.rpkit_character_stat_points</code> table reference
     */
    public RpkitCharacterStatPoints(String alias) {
        this(DSL.name(alias), RPKIT_CHARACTER_STAT_POINTS);
    }

    /**
     * Create an aliased <code>rpkit.rpkit_character_stat_points</code> table reference
     */
    public RpkitCharacterStatPoints(Name alias) {
        this(alias, RPKIT_CHARACTER_STAT_POINTS);
    }

    private RpkitCharacterStatPoints(Name alias, Table<RpkitCharacterStatPointsRecord> aliased) {
        this(alias, aliased, null);
    }

    private RpkitCharacterStatPoints(Name alias, Table<RpkitCharacterStatPointsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> RpkitCharacterStatPoints(Table<O> child, ForeignKey<O, RpkitCharacterStatPointsRecord> key) {
        super(child, key, RPKIT_CHARACTER_STAT_POINTS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return Rpkit.RPKIT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.RPKIT_CHARACTER_STAT_POINTS_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity<RpkitCharacterStatPointsRecord, Integer> getIdentity() {
        return Keys.IDENTITY_RPKIT_CHARACTER_STAT_POINTS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<RpkitCharacterStatPointsRecord> getPrimaryKey() {
        return Keys.KEY_RPKIT_CHARACTER_STAT_POINTS_PRIMARY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<RpkitCharacterStatPointsRecord>> getKeys() {
        return Arrays.<UniqueKey<RpkitCharacterStatPointsRecord>>asList(Keys.KEY_RPKIT_CHARACTER_STAT_POINTS_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RpkitCharacterStatPoints as(String alias) {
        return new RpkitCharacterStatPoints(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RpkitCharacterStatPoints as(Name alias) {
        return new RpkitCharacterStatPoints(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public RpkitCharacterStatPoints rename(String name) {
        return new RpkitCharacterStatPoints(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public RpkitCharacterStatPoints rename(Name name) {
        return new RpkitCharacterStatPoints(name, null);
    }
}