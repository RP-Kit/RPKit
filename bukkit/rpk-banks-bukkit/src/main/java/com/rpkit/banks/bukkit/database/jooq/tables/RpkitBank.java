/*
 * This file is generated by jOOQ.
 */
package com.rpkit.banks.bukkit.database.jooq.tables;


import com.rpkit.banks.bukkit.database.jooq.Keys;
import com.rpkit.banks.bukkit.database.jooq.RpkitBanks;
import com.rpkit.banks.bukkit.database.jooq.tables.records.RpkitBankRecord;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row3;
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
public class RpkitBank extends TableImpl<RpkitBankRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>rpkit_banks.rpkit_bank</code>
     */
    public static final RpkitBank RPKIT_BANK = new RpkitBank();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<RpkitBankRecord> getRecordType() {
        return RpkitBankRecord.class;
    }

    /**
     * The column <code>rpkit_banks.rpkit_bank.character_id</code>.
     */
    public final TableField<RpkitBankRecord, Integer> CHARACTER_ID = createField(DSL.name("character_id"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>rpkit_banks.rpkit_bank.currency_name</code>.
     */
    public final TableField<RpkitBankRecord, String> CURRENCY_NAME = createField(DSL.name("currency_name"), SQLDataType.VARCHAR(256).nullable(false), this, "");

    /**
     * The column <code>rpkit_banks.rpkit_bank.balance</code>.
     */
    public final TableField<RpkitBankRecord, Integer> BALANCE = createField(DSL.name("balance"), SQLDataType.INTEGER.nullable(false), this, "");

    private RpkitBank(Name alias, Table<RpkitBankRecord> aliased) {
        this(alias, aliased, null);
    }

    private RpkitBank(Name alias, Table<RpkitBankRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>rpkit_banks.rpkit_bank</code> table reference
     */
    public RpkitBank(String alias) {
        this(DSL.name(alias), RPKIT_BANK);
    }

    /**
     * Create an aliased <code>rpkit_banks.rpkit_bank</code> table reference
     */
    public RpkitBank(Name alias) {
        this(alias, RPKIT_BANK);
    }

    /**
     * Create a <code>rpkit_banks.rpkit_bank</code> table reference
     */
    public RpkitBank() {
        this(DSL.name("rpkit_bank"), null);
    }

    public <O extends Record> RpkitBank(Table<O> child, ForeignKey<O, RpkitBankRecord> key) {
        super(child, key, RPKIT_BANK);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : RpkitBanks.RPKIT_BANKS;
    }

    @Override
    public UniqueKey<RpkitBankRecord> getPrimaryKey() {
        return Keys.KEY_RPKIT_BANK_PRIMARY;
    }

    @Override
    public RpkitBank as(String alias) {
        return new RpkitBank(DSL.name(alias), this);
    }

    @Override
    public RpkitBank as(Name alias) {
        return new RpkitBank(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public RpkitBank rename(String name) {
        return new RpkitBank(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public RpkitBank rename(Name name) {
        return new RpkitBank(name, null);
    }

    // -------------------------------------------------------------------------
    // Row3 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row3<Integer, String, Integer> fieldsRow() {
        return (Row3) super.fieldsRow();
    }
}
