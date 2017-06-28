/*
 * This file is generated by jOOQ.
*/
package com.rpkit.payments.bukkit.database.jooq.rpkit.tables;


import com.rpkit.payments.bukkit.database.jooq.rpkit.Keys;
import com.rpkit.payments.bukkit.database.jooq.rpkit.Rpkit;
import com.rpkit.payments.bukkit.database.jooq.rpkit.tables.records.RpkitPaymentGroupMemberRecord;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Identity;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.9.2"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class RpkitPaymentGroupMember extends TableImpl<RpkitPaymentGroupMemberRecord> {

    private static final long serialVersionUID = -136198549;

    /**
     * The reference instance of <code>rpkit.rpkit_payment_group_member</code>
     */
    public static final RpkitPaymentGroupMember RPKIT_PAYMENT_GROUP_MEMBER = new RpkitPaymentGroupMember();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<RpkitPaymentGroupMemberRecord> getRecordType() {
        return RpkitPaymentGroupMemberRecord.class;
    }

    /**
     * The column <code>rpkit.rpkit_payment_group_member.id</code>.
     */
    public final TableField<RpkitPaymentGroupMemberRecord, Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>rpkit.rpkit_payment_group_member.payment_group_id</code>.
     */
    public final TableField<RpkitPaymentGroupMemberRecord, Integer> PAYMENT_GROUP_ID = createField("payment_group_id", org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * The column <code>rpkit.rpkit_payment_group_member.character_id</code>.
     */
    public final TableField<RpkitPaymentGroupMemberRecord, Integer> CHARACTER_ID = createField("character_id", org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * Create a <code>rpkit.rpkit_payment_group_member</code> table reference
     */
    public RpkitPaymentGroupMember() {
        this("rpkit_payment_group_member", null);
    }

    /**
     * Create an aliased <code>rpkit.rpkit_payment_group_member</code> table reference
     */
    public RpkitPaymentGroupMember(String alias) {
        this(alias, RPKIT_PAYMENT_GROUP_MEMBER);
    }

    private RpkitPaymentGroupMember(String alias, Table<RpkitPaymentGroupMemberRecord> aliased) {
        this(alias, aliased, null);
    }

    private RpkitPaymentGroupMember(String alias, Table<RpkitPaymentGroupMemberRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, "");
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
    public Identity<RpkitPaymentGroupMemberRecord, Integer> getIdentity() {
        return Keys.IDENTITY_RPKIT_PAYMENT_GROUP_MEMBER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<RpkitPaymentGroupMemberRecord> getPrimaryKey() {
        return Keys.KEY_RPKIT_PAYMENT_GROUP_MEMBER_PRIMARY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<RpkitPaymentGroupMemberRecord>> getKeys() {
        return Arrays.<UniqueKey<RpkitPaymentGroupMemberRecord>>asList(Keys.KEY_RPKIT_PAYMENT_GROUP_MEMBER_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RpkitPaymentGroupMember as(String alias) {
        return new RpkitPaymentGroupMember(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public RpkitPaymentGroupMember rename(String name) {
        return new RpkitPaymentGroupMember(name, null);
    }
}
