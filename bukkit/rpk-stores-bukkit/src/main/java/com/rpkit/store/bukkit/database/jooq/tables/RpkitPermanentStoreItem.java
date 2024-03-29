/*
 * This file is generated by jOOQ.
 */
package com.rpkit.store.bukkit.database.jooq.tables;


import com.rpkit.store.bukkit.database.jooq.Keys;
import com.rpkit.store.bukkit.database.jooq.RpkitStores;
import com.rpkit.store.bukkit.database.jooq.tables.records.RpkitPermanentStoreItemRecord;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
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
public class RpkitPermanentStoreItem extends TableImpl<RpkitPermanentStoreItemRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of
     * <code>rpkit_stores.rpkit_permanent_store_item</code>
     */
    public static final RpkitPermanentStoreItem RPKIT_PERMANENT_STORE_ITEM = new RpkitPermanentStoreItem();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<RpkitPermanentStoreItemRecord> getRecordType() {
        return RpkitPermanentStoreItemRecord.class;
    }

    /**
     * The column <code>rpkit_stores.rpkit_permanent_store_item.id</code>.
     */
    public final TableField<RpkitPermanentStoreItemRecord, Integer> ID = createField(DSL.name("id"), SQLDataType.INTEGER.nullable(false).identity(true), this, "");

    /**
     * The column
     * <code>rpkit_stores.rpkit_permanent_store_item.store_item_id</code>.
     */
    public final TableField<RpkitPermanentStoreItemRecord, Integer> STORE_ITEM_ID = createField(DSL.name("store_item_id"), SQLDataType.INTEGER.nullable(false), this, "");

    private RpkitPermanentStoreItem(Name alias, Table<RpkitPermanentStoreItemRecord> aliased) {
        this(alias, aliased, null);
    }

    private RpkitPermanentStoreItem(Name alias, Table<RpkitPermanentStoreItemRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>rpkit_stores.rpkit_permanent_store_item</code>
     * table reference
     */
    public RpkitPermanentStoreItem(String alias) {
        this(DSL.name(alias), RPKIT_PERMANENT_STORE_ITEM);
    }

    /**
     * Create an aliased <code>rpkit_stores.rpkit_permanent_store_item</code>
     * table reference
     */
    public RpkitPermanentStoreItem(Name alias) {
        this(alias, RPKIT_PERMANENT_STORE_ITEM);
    }

    /**
     * Create a <code>rpkit_stores.rpkit_permanent_store_item</code> table
     * reference
     */
    public RpkitPermanentStoreItem() {
        this(DSL.name("rpkit_permanent_store_item"), null);
    }

    public <O extends Record> RpkitPermanentStoreItem(Table<O> child, ForeignKey<O, RpkitPermanentStoreItemRecord> key) {
        super(child, key, RPKIT_PERMANENT_STORE_ITEM);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : RpkitStores.RPKIT_STORES;
    }

    @Override
    public Identity<RpkitPermanentStoreItemRecord, Integer> getIdentity() {
        return (Identity<RpkitPermanentStoreItemRecord, Integer>) super.getIdentity();
    }

    @Override
    public UniqueKey<RpkitPermanentStoreItemRecord> getPrimaryKey() {
        return Keys.KEY_RPKIT_PERMANENT_STORE_ITEM_PRIMARY;
    }

    @Override
    public RpkitPermanentStoreItem as(String alias) {
        return new RpkitPermanentStoreItem(DSL.name(alias), this);
    }

    @Override
    public RpkitPermanentStoreItem as(Name alias) {
        return new RpkitPermanentStoreItem(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public RpkitPermanentStoreItem rename(String name) {
        return new RpkitPermanentStoreItem(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public RpkitPermanentStoreItem rename(Name name) {
        return new RpkitPermanentStoreItem(name, null);
    }

    // -------------------------------------------------------------------------
    // Row2 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row2<Integer, Integer> fieldsRow() {
        return (Row2) super.fieldsRow();
    }
}
