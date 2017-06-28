/*
 * This file is generated by jOOQ.
*/
package com.rpkit.banks.bukkit.database.jooq.rpkit.tables.records;


import com.rpkit.banks.bukkit.database.jooq.rpkit.tables.RpkitBank;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record4;
import org.jooq.Row4;
import org.jooq.impl.UpdatableRecordImpl;

import javax.annotation.Generated;


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
public class RpkitBankRecord extends UpdatableRecordImpl<RpkitBankRecord> implements Record4<Integer, Integer, Integer, Integer> {

    private static final long serialVersionUID = 1335036102;

    /**
     * Setter for <code>rpkit.rpkit_bank.id</code>.
     */
    public void setId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>rpkit.rpkit_bank.id</code>.
     */
    public Integer getId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>rpkit.rpkit_bank.character_id</code>.
     */
    public void setCharacterId(Integer value) {
        set(1, value);
    }

    /**
     * Getter for <code>rpkit.rpkit_bank.character_id</code>.
     */
    public Integer getCharacterId() {
        return (Integer) get(1);
    }

    /**
     * Setter for <code>rpkit.rpkit_bank.currency_id</code>.
     */
    public void setCurrencyId(Integer value) {
        set(2, value);
    }

    /**
     * Getter for <code>rpkit.rpkit_bank.currency_id</code>.
     */
    public Integer getCurrencyId() {
        return (Integer) get(2);
    }

    /**
     * Setter for <code>rpkit.rpkit_bank.balance</code>.
     */
    public void setBalance(Integer value) {
        set(3, value);
    }

    /**
     * Getter for <code>rpkit.rpkit_bank.balance</code>.
     */
    public Integer getBalance() {
        return (Integer) get(3);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record1<Integer> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record4 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row4<Integer, Integer, Integer, Integer> fieldsRow() {
        return (Row4) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row4<Integer, Integer, Integer, Integer> valuesRow() {
        return (Row4) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field1() {
        return RpkitBank.RPKIT_BANK.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field2() {
        return RpkitBank.RPKIT_BANK.CHARACTER_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field3() {
        return RpkitBank.RPKIT_BANK.CURRENCY_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field4() {
        return RpkitBank.RPKIT_BANK.BALANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value1() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value2() {
        return getCharacterId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value3() {
        return getCurrencyId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value4() {
        return getBalance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RpkitBankRecord value1(Integer value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RpkitBankRecord value2(Integer value) {
        setCharacterId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RpkitBankRecord value3(Integer value) {
        setCurrencyId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RpkitBankRecord value4(Integer value) {
        setBalance(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RpkitBankRecord values(Integer value1, Integer value2, Integer value3, Integer value4) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached RpkitBankRecord
     */
    public RpkitBankRecord() {
        super(RpkitBank.RPKIT_BANK);
    }

    /**
     * Create a detached, initialised RpkitBankRecord
     */
    public RpkitBankRecord(Integer id, Integer characterId, Integer currencyId, Integer balance) {
        super(RpkitBank.RPKIT_BANK);

        set(0, id);
        set(1, characterId);
        set(2, currencyId);
        set(3, balance);
    }
}
