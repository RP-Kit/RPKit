/*
 * This file is generated by jOOQ.
 */
package com.rpkit.professions.bukkit.database.jooq.tables.records;


import com.rpkit.professions.bukkit.database.jooq.tables.RpkitCharacterProfession;

import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Row2;
import org.jooq.impl.TableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class RpkitCharacterProfessionRecord extends TableRecordImpl<RpkitCharacterProfessionRecord> implements Record2<Integer, String> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for
     * <code>rpkit_professions.rpkit_character_profession.character_id</code>.
     */
    public void setCharacterId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for
     * <code>rpkit_professions.rpkit_character_profession.character_id</code>.
     */
    public Integer getCharacterId() {
        return (Integer) get(0);
    }

    /**
     * Setter for
     * <code>rpkit_professions.rpkit_character_profession.profession</code>.
     */
    public void setProfession(String value) {
        set(1, value);
    }

    /**
     * Getter for
     * <code>rpkit_professions.rpkit_character_profession.profession</code>.
     */
    public String getProfession() {
        return (String) get(1);
    }

    // -------------------------------------------------------------------------
    // Record2 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row2<Integer, String> fieldsRow() {
        return (Row2) super.fieldsRow();
    }

    @Override
    public Row2<Integer, String> valuesRow() {
        return (Row2) super.valuesRow();
    }

    @Override
    public Field<Integer> field1() {
        return RpkitCharacterProfession.RPKIT_CHARACTER_PROFESSION.CHARACTER_ID;
    }

    @Override
    public Field<String> field2() {
        return RpkitCharacterProfession.RPKIT_CHARACTER_PROFESSION.PROFESSION;
    }

    @Override
    public Integer component1() {
        return getCharacterId();
    }

    @Override
    public String component2() {
        return getProfession();
    }

    @Override
    public Integer value1() {
        return getCharacterId();
    }

    @Override
    public String value2() {
        return getProfession();
    }

    @Override
    public RpkitCharacterProfessionRecord value1(Integer value) {
        setCharacterId(value);
        return this;
    }

    @Override
    public RpkitCharacterProfessionRecord value2(String value) {
        setProfession(value);
        return this;
    }

    @Override
    public RpkitCharacterProfessionRecord values(Integer value1, String value2) {
        value1(value1);
        value2(value2);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached RpkitCharacterProfessionRecord
     */
    public RpkitCharacterProfessionRecord() {
        super(RpkitCharacterProfession.RPKIT_CHARACTER_PROFESSION);
    }

    /**
     * Create a detached, initialised RpkitCharacterProfessionRecord
     */
    public RpkitCharacterProfessionRecord(Integer characterId, String profession) {
        super(RpkitCharacterProfession.RPKIT_CHARACTER_PROFESSION);

        setCharacterId(characterId);
        setProfession(profession);
    }
}
