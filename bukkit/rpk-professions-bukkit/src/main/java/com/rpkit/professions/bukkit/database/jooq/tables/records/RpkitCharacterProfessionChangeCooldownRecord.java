/*
 * This file is generated by jOOQ.
 */
package com.rpkit.professions.bukkit.database.jooq.tables.records;


import com.rpkit.professions.bukkit.database.jooq.tables.RpkitCharacterProfessionChangeCooldown;

import java.time.LocalDateTime;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Row2;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class RpkitCharacterProfessionChangeCooldownRecord extends UpdatableRecordImpl<RpkitCharacterProfessionChangeCooldownRecord> implements Record2<Integer, LocalDateTime> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for
     * <code>rpkit_professions.rpkit_character_profession_change_cooldown.character_id</code>.
     */
    public void setCharacterId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for
     * <code>rpkit_professions.rpkit_character_profession_change_cooldown.character_id</code>.
     */
    public Integer getCharacterId() {
        return (Integer) get(0);
    }

    /**
     * Setter for
     * <code>rpkit_professions.rpkit_character_profession_change_cooldown.cooldown_end_time</code>.
     */
    public void setCooldownEndTime(LocalDateTime value) {
        set(1, value);
    }

    /**
     * Getter for
     * <code>rpkit_professions.rpkit_character_profession_change_cooldown.cooldown_end_time</code>.
     */
    public LocalDateTime getCooldownEndTime() {
        return (LocalDateTime) get(1);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Integer> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record2 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row2<Integer, LocalDateTime> fieldsRow() {
        return (Row2) super.fieldsRow();
    }

    @Override
    public Row2<Integer, LocalDateTime> valuesRow() {
        return (Row2) super.valuesRow();
    }

    @Override
    public Field<Integer> field1() {
        return RpkitCharacterProfessionChangeCooldown.RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.CHARACTER_ID;
    }

    @Override
    public Field<LocalDateTime> field2() {
        return RpkitCharacterProfessionChangeCooldown.RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.COOLDOWN_END_TIME;
    }

    @Override
    public Integer component1() {
        return getCharacterId();
    }

    @Override
    public LocalDateTime component2() {
        return getCooldownEndTime();
    }

    @Override
    public Integer value1() {
        return getCharacterId();
    }

    @Override
    public LocalDateTime value2() {
        return getCooldownEndTime();
    }

    @Override
    public RpkitCharacterProfessionChangeCooldownRecord value1(Integer value) {
        setCharacterId(value);
        return this;
    }

    @Override
    public RpkitCharacterProfessionChangeCooldownRecord value2(LocalDateTime value) {
        setCooldownEndTime(value);
        return this;
    }

    @Override
    public RpkitCharacterProfessionChangeCooldownRecord values(Integer value1, LocalDateTime value2) {
        value1(value1);
        value2(value2);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached RpkitCharacterProfessionChangeCooldownRecord
     */
    public RpkitCharacterProfessionChangeCooldownRecord() {
        super(RpkitCharacterProfessionChangeCooldown.RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN);
    }

    /**
     * Create a detached, initialised
     * RpkitCharacterProfessionChangeCooldownRecord
     */
    public RpkitCharacterProfessionChangeCooldownRecord(Integer characterId, LocalDateTime cooldownEndTime) {
        super(RpkitCharacterProfessionChangeCooldown.RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN);

        setCharacterId(characterId);
        setCooldownEndTime(cooldownEndTime);
    }
}
