/*
 * This file is generated by jOOQ.
 */
package com.rpkit.payments.bukkit.database.jooq.tables;


import com.rpkit.payments.bukkit.database.jooq.RpkitPayments;
import com.rpkit.payments.bukkit.database.jooq.tables.records.RpkitPaymentGroupOwnerRecord;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row2;
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
public class RpkitPaymentGroupOwner extends TableImpl<RpkitPaymentGroupOwnerRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of
     * <code>rpkit_payments.rpkit_payment_group_owner</code>
     */
    public static final RpkitPaymentGroupOwner RPKIT_PAYMENT_GROUP_OWNER = new RpkitPaymentGroupOwner();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<RpkitPaymentGroupOwnerRecord> getRecordType() {
        return RpkitPaymentGroupOwnerRecord.class;
    }

    /**
     * The column
     * <code>rpkit_payments.rpkit_payment_group_owner.payment_group_id</code>.
     */
    public final TableField<RpkitPaymentGroupOwnerRecord, Integer> PAYMENT_GROUP_ID = createField(DSL.name("payment_group_id"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column
     * <code>rpkit_payments.rpkit_payment_group_owner.character_id</code>.
     */
    public final TableField<RpkitPaymentGroupOwnerRecord, Integer> CHARACTER_ID = createField(DSL.name("character_id"), SQLDataType.INTEGER.nullable(false), this, "");

    private RpkitPaymentGroupOwner(Name alias, Table<RpkitPaymentGroupOwnerRecord> aliased) {
        this(alias, aliased, null);
    }

    private RpkitPaymentGroupOwner(Name alias, Table<RpkitPaymentGroupOwnerRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>rpkit_payments.rpkit_payment_group_owner</code>
     * table reference
     */
    public RpkitPaymentGroupOwner(String alias) {
        this(DSL.name(alias), RPKIT_PAYMENT_GROUP_OWNER);
    }

    /**
     * Create an aliased <code>rpkit_payments.rpkit_payment_group_owner</code>
     * table reference
     */
    public RpkitPaymentGroupOwner(Name alias) {
        this(alias, RPKIT_PAYMENT_GROUP_OWNER);
    }

    /**
     * Create a <code>rpkit_payments.rpkit_payment_group_owner</code> table
     * reference
     */
    public RpkitPaymentGroupOwner() {
        this(DSL.name("rpkit_payment_group_owner"), null);
    }

    public <O extends Record> RpkitPaymentGroupOwner(Table<O> child, ForeignKey<O, RpkitPaymentGroupOwnerRecord> key) {
        super(child, key, RPKIT_PAYMENT_GROUP_OWNER);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : RpkitPayments.RPKIT_PAYMENTS;
    }

    @Override
    public RpkitPaymentGroupOwner as(String alias) {
        return new RpkitPaymentGroupOwner(DSL.name(alias), this);
    }

    @Override
    public RpkitPaymentGroupOwner as(Name alias) {
        return new RpkitPaymentGroupOwner(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public RpkitPaymentGroupOwner rename(String name) {
        return new RpkitPaymentGroupOwner(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public RpkitPaymentGroupOwner rename(Name name) {
        return new RpkitPaymentGroupOwner(name, null);
    }

    // -------------------------------------------------------------------------
    // Row2 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row2<Integer, Integer> fieldsRow() {
        return (Row2) super.fieldsRow();
    }
}
