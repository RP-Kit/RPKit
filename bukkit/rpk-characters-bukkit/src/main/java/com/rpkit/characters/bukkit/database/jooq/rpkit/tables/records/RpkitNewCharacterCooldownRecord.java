/*
 * This file is generated by jOOQ.
*/
package com.rpkit.characters.bukkit.database.jooq.rpkit.tables.records;


import com.rpkit.characters.bukkit.database.jooq.rpkit.tables.RpkitNewCharacterCooldown;

import java.sql.Date;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.UpdatableRecordImpl;


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
public class RpkitNewCharacterCooldownRecord extends UpdatableRecordImpl<RpkitNewCharacterCooldownRecord> implements Record3<Integer, Integer, Date> {

    private static final long serialVersionUID = -1422236001;

    /**
     * Setter for <code>rpkit.rpkit_new_character_cooldown.id</code>.
     */
    public void setId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>rpkit.rpkit_new_character_cooldown.id</code>.
     */
    public Integer getId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>rpkit.rpkit_new_character_cooldown.profile_id</code>.
     */
    public void setProfileId(Integer value) {
        set(1, value);
    }

    /**
     * Getter for <code>rpkit.rpkit_new_character_cooldown.profile_id</code>.
     */
    public Integer getProfileId() {
        return (Integer) get(1);
    }

    /**
     * Setter for <code>rpkit.rpkit_new_character_cooldown.cooldown_timestamp</code>.
     */
    public void setCooldownTimestamp(Date value) {
        set(2, value);
    }

    /**
     * Getter for <code>rpkit.rpkit_new_character_cooldown.cooldown_timestamp</code>.
     */
    public Date getCooldownTimestamp() {
        return (Date) get(2);
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
    // Record3 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row3<Integer, Integer, Date> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row3<Integer, Integer, Date> valuesRow() {
        return (Row3) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field1() {
        return RpkitNewCharacterCooldown.RPKIT_NEW_CHARACTER_COOLDOWN.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field2() {
        return RpkitNewCharacterCooldown.RPKIT_NEW_CHARACTER_COOLDOWN.PROFILE_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Date> field3() {
        return RpkitNewCharacterCooldown.RPKIT_NEW_CHARACTER_COOLDOWN.COOLDOWN_TIMESTAMP;
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
        return getProfileId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date value3() {
        return getCooldownTimestamp();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RpkitNewCharacterCooldownRecord value1(Integer value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RpkitNewCharacterCooldownRecord value2(Integer value) {
        setProfileId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RpkitNewCharacterCooldownRecord value3(Date value) {
        setCooldownTimestamp(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RpkitNewCharacterCooldownRecord values(Integer value1, Integer value2, Date value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached RpkitNewCharacterCooldownRecord
     */
    public RpkitNewCharacterCooldownRecord() {
        super(RpkitNewCharacterCooldown.RPKIT_NEW_CHARACTER_COOLDOWN);
    }

    /**
     * Create a detached, initialised RpkitNewCharacterCooldownRecord
     */
    public RpkitNewCharacterCooldownRecord(Integer id, Integer profileId, Date cooldownTimestamp) {
        super(RpkitNewCharacterCooldown.RPKIT_NEW_CHARACTER_COOLDOWN);

        set(0, id);
        set(1, profileId);
        set(2, cooldownTimestamp);
    }
}
