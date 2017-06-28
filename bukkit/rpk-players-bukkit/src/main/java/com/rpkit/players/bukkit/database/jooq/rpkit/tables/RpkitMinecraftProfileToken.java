/*
 * This file is generated by jOOQ.
*/
package com.rpkit.players.bukkit.database.jooq.rpkit.tables;


import com.rpkit.players.bukkit.database.jooq.rpkit.Keys;
import com.rpkit.players.bukkit.database.jooq.rpkit.Rpkit;
import com.rpkit.players.bukkit.database.jooq.rpkit.tables.records.RpkitMinecraftProfileTokenRecord;

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
public class RpkitMinecraftProfileToken extends TableImpl<RpkitMinecraftProfileTokenRecord> {

    private static final long serialVersionUID = 737640436;

    /**
     * The reference instance of <code>rpkit.rpkit_minecraft_profile_token</code>
     */
    public static final RpkitMinecraftProfileToken RPKIT_MINECRAFT_PROFILE_TOKEN = new RpkitMinecraftProfileToken();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<RpkitMinecraftProfileTokenRecord> getRecordType() {
        return RpkitMinecraftProfileTokenRecord.class;
    }

    /**
     * The column <code>rpkit.rpkit_minecraft_profile_token.id</code>.
     */
    public final TableField<RpkitMinecraftProfileTokenRecord, Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>rpkit.rpkit_minecraft_profile_token.minecraft_profile_id</code>.
     */
    public final TableField<RpkitMinecraftProfileTokenRecord, Integer> MINECRAFT_PROFILE_ID = createField("minecraft_profile_id", org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * The column <code>rpkit.rpkit_minecraft_profile_token.token</code>.
     */
    public final TableField<RpkitMinecraftProfileTokenRecord, String> TOKEN = createField("token", org.jooq.impl.SQLDataType.VARCHAR.length(36), this, "");

    /**
     * Create a <code>rpkit.rpkit_minecraft_profile_token</code> table reference
     */
    public RpkitMinecraftProfileToken() {
        this("rpkit_minecraft_profile_token", null);
    }

    /**
     * Create an aliased <code>rpkit.rpkit_minecraft_profile_token</code> table reference
     */
    public RpkitMinecraftProfileToken(String alias) {
        this(alias, RPKIT_MINECRAFT_PROFILE_TOKEN);
    }

    private RpkitMinecraftProfileToken(String alias, Table<RpkitMinecraftProfileTokenRecord> aliased) {
        this(alias, aliased, null);
    }

    private RpkitMinecraftProfileToken(String alias, Table<RpkitMinecraftProfileTokenRecord> aliased, Field<?>[] parameters) {
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
    public Identity<RpkitMinecraftProfileTokenRecord, Integer> getIdentity() {
        return Keys.IDENTITY_RPKIT_MINECRAFT_PROFILE_TOKEN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<RpkitMinecraftProfileTokenRecord> getPrimaryKey() {
        return Keys.KEY_RPKIT_MINECRAFT_PROFILE_TOKEN_PRIMARY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<RpkitMinecraftProfileTokenRecord>> getKeys() {
        return Arrays.<UniqueKey<RpkitMinecraftProfileTokenRecord>>asList(Keys.KEY_RPKIT_MINECRAFT_PROFILE_TOKEN_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RpkitMinecraftProfileToken as(String alias) {
        return new RpkitMinecraftProfileToken(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public RpkitMinecraftProfileToken rename(String name) {
        return new RpkitMinecraftProfileToken(name, null);
    }
}
