/*
 * This file is generated by jOOQ.
 */
package com.rpkit.languages.bukkit.database.jooq.tables.records;


import com.rpkit.languages.bukkit.database.jooq.tables.RpkitCharacterLanguage;

import org.jooq.Field;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.TableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class RpkitCharacterLanguageRecord extends TableRecordImpl<RpkitCharacterLanguageRecord> implements Record3<Integer, String, Double> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for
     * <code>rpkit_languages.rpkit_character_language.character_id</code>.
     */
    public void setCharacterId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for
     * <code>rpkit_languages.rpkit_character_language.character_id</code>.
     */
    public Integer getCharacterId() {
        return (Integer) get(0);
    }

    /**
     * Setter for
     * <code>rpkit_languages.rpkit_character_language.language_name</code>.
     */
    public void setLanguageName(String value) {
        set(1, value);
    }

    /**
     * Getter for
     * <code>rpkit_languages.rpkit_character_language.language_name</code>.
     */
    public String getLanguageName() {
        return (String) get(1);
    }

    /**
     * Setter for
     * <code>rpkit_languages.rpkit_character_language.understanding</code>.
     */
    public void setUnderstanding(Double value) {
        set(2, value);
    }

    /**
     * Getter for
     * <code>rpkit_languages.rpkit_character_language.understanding</code>.
     */
    public Double getUnderstanding() {
        return (Double) get(2);
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row3<Integer, String, Double> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    @Override
    public Row3<Integer, String, Double> valuesRow() {
        return (Row3) super.valuesRow();
    }

    @Override
    public Field<Integer> field1() {
        return RpkitCharacterLanguage.RPKIT_CHARACTER_LANGUAGE.CHARACTER_ID;
    }

    @Override
    public Field<String> field2() {
        return RpkitCharacterLanguage.RPKIT_CHARACTER_LANGUAGE.LANGUAGE_NAME;
    }

    @Override
    public Field<Double> field3() {
        return RpkitCharacterLanguage.RPKIT_CHARACTER_LANGUAGE.UNDERSTANDING;
    }

    @Override
    public Integer component1() {
        return getCharacterId();
    }

    @Override
    public String component2() {
        return getLanguageName();
    }

    @Override
    public Double component3() {
        return getUnderstanding();
    }

    @Override
    public Integer value1() {
        return getCharacterId();
    }

    @Override
    public String value2() {
        return getLanguageName();
    }

    @Override
    public Double value3() {
        return getUnderstanding();
    }

    @Override
    public RpkitCharacterLanguageRecord value1(Integer value) {
        setCharacterId(value);
        return this;
    }

    @Override
    public RpkitCharacterLanguageRecord value2(String value) {
        setLanguageName(value);
        return this;
    }

    @Override
    public RpkitCharacterLanguageRecord value3(Double value) {
        setUnderstanding(value);
        return this;
    }

    @Override
    public RpkitCharacterLanguageRecord values(Integer value1, String value2, Double value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached RpkitCharacterLanguageRecord
     */
    public RpkitCharacterLanguageRecord() {
        super(RpkitCharacterLanguage.RPKIT_CHARACTER_LANGUAGE);
    }

    /**
     * Create a detached, initialised RpkitCharacterLanguageRecord
     */
    public RpkitCharacterLanguageRecord(Integer characterId, String languageName, Double understanding) {
        super(RpkitCharacterLanguage.RPKIT_CHARACTER_LANGUAGE);

        setCharacterId(characterId);
        setLanguageName(languageName);
        setUnderstanding(understanding);
    }
}
