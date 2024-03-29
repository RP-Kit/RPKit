/*
 * This file is generated by jOOQ.
 */
package com.rpkit.permissions.bukkit.database.jooq.tables;


import com.rpkit.permissions.bukkit.database.jooq.RpkitPermissions;
import com.rpkit.permissions.bukkit.database.jooq.tables.records.RpkitProfileGroupRecord;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row3;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class RpkitProfileGroup extends TableImpl<RpkitProfileGroupRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of
     * <code>rpkit_permissions.rpkit_profile_group</code>
     */
    public static final RpkitProfileGroup RPKIT_PROFILE_GROUP = new RpkitProfileGroup();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<RpkitProfileGroupRecord> getRecordType() {
        return RpkitProfileGroupRecord.class;
    }

    /**
     * The column <code>rpkit_permissions.rpkit_profile_group.profile_id</code>.
     */
    public final TableField<RpkitProfileGroupRecord, Integer> PROFILE_ID = createField(DSL.name("profile_id"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>rpkit_permissions.rpkit_profile_group.group_name</code>.
     */
    public final TableField<RpkitProfileGroupRecord, String> GROUP_NAME = createField(DSL.name("group_name"), SQLDataType.VARCHAR(256).nullable(false), this, "");

    /**
     * The column <code>rpkit_permissions.rpkit_profile_group.priority</code>.
     */
    public final TableField<RpkitProfileGroupRecord, Integer> PRIORITY = createField(DSL.name("priority"), SQLDataType.INTEGER.nullable(false), this, "");

    private RpkitProfileGroup(Name alias, Table<RpkitProfileGroupRecord> aliased) {
        this(alias, aliased, null);
    }

    private RpkitProfileGroup(Name alias, Table<RpkitProfileGroupRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>rpkit_permissions.rpkit_profile_group</code>
     * table reference
     */
    public RpkitProfileGroup(String alias) {
        this(DSL.name(alias), RPKIT_PROFILE_GROUP);
    }

    /**
     * Create an aliased <code>rpkit_permissions.rpkit_profile_group</code>
     * table reference
     */
    public RpkitProfileGroup(Name alias) {
        this(alias, RPKIT_PROFILE_GROUP);
    }

    /**
     * Create a <code>rpkit_permissions.rpkit_profile_group</code> table
     * reference
     */
    public RpkitProfileGroup() {
        this(DSL.name("rpkit_profile_group"), null);
    }

    public <O extends Record> RpkitProfileGroup(Table<O> child, ForeignKey<O, RpkitProfileGroupRecord> key) {
        super(child, key, RPKIT_PROFILE_GROUP);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : RpkitPermissions.RPKIT_PERMISSIONS;
    }

    @Override
    public RpkitProfileGroup as(String alias) {
        return new RpkitProfileGroup(DSL.name(alias), this);
    }

    @Override
    public RpkitProfileGroup as(Name alias) {
        return new RpkitProfileGroup(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public RpkitProfileGroup rename(String name) {
        return new RpkitProfileGroup(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public RpkitProfileGroup rename(Name name) {
        return new RpkitProfileGroup(name, null);
    }

    // -------------------------------------------------------------------------
    // Row3 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row3<Integer, String, Integer> fieldsRow() {
        return (Row3) super.fieldsRow();
    }
}
