/*
 * This file is generated by jOOQ.
*/
package com.rpkit.essentials.bukkit.database.jooq.rpkit.tables;


import com.rpkit.essentials.bukkit.database.jooq.rpkit.Keys;
import com.rpkit.essentials.bukkit.database.jooq.rpkit.Rpkit;
import com.rpkit.essentials.bukkit.database.jooq.rpkit.tables.records.RpkitLogMessagesEnabledRecord;

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
public class RpkitLogMessagesEnabled extends TableImpl<RpkitLogMessagesEnabledRecord> {

    private static final long serialVersionUID = 525842279;

    /**
     * The reference instance of <code>rpkit.rpkit_log_messages_enabled</code>
     */
    public static final RpkitLogMessagesEnabled RPKIT_LOG_MESSAGES_ENABLED = new RpkitLogMessagesEnabled();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<RpkitLogMessagesEnabledRecord> getRecordType() {
        return RpkitLogMessagesEnabledRecord.class;
    }

    /**
     * The column <code>rpkit.rpkit_log_messages_enabled.id</code>.
     */
    public final TableField<RpkitLogMessagesEnabledRecord, Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>rpkit.rpkit_log_messages_enabled.minecraft_profile_id</code>.
     */
    public final TableField<RpkitLogMessagesEnabledRecord, Integer> MINECRAFT_PROFILE_ID = createField("minecraft_profile_id", org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * The column <code>rpkit.rpkit_log_messages_enabled.enabled</code>.
     */
    public final TableField<RpkitLogMessagesEnabledRecord, Byte> ENABLED = createField("enabled", org.jooq.impl.SQLDataType.TINYINT, this, "");

    /**
     * Create a <code>rpkit.rpkit_log_messages_enabled</code> table reference
     */
    public RpkitLogMessagesEnabled() {
        this("rpkit_log_messages_enabled", null);
    }

    /**
     * Create an aliased <code>rpkit.rpkit_log_messages_enabled</code> table reference
     */
    public RpkitLogMessagesEnabled(String alias) {
        this(alias, RPKIT_LOG_MESSAGES_ENABLED);
    }

    private RpkitLogMessagesEnabled(String alias, Table<RpkitLogMessagesEnabledRecord> aliased) {
        this(alias, aliased, null);
    }

    private RpkitLogMessagesEnabled(String alias, Table<RpkitLogMessagesEnabledRecord> aliased, Field<?>[] parameters) {
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
    public Identity<RpkitLogMessagesEnabledRecord, Integer> getIdentity() {
        return Keys.IDENTITY_RPKIT_LOG_MESSAGES_ENABLED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<RpkitLogMessagesEnabledRecord> getPrimaryKey() {
        return Keys.KEY_RPKIT_LOG_MESSAGES_ENABLED_PRIMARY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<RpkitLogMessagesEnabledRecord>> getKeys() {
        return Arrays.<UniqueKey<RpkitLogMessagesEnabledRecord>>asList(Keys.KEY_RPKIT_LOG_MESSAGES_ENABLED_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RpkitLogMessagesEnabled as(String alias) {
        return new RpkitLogMessagesEnabled(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public RpkitLogMessagesEnabled rename(String name) {
        return new RpkitLogMessagesEnabled(name, null);
    }
}
