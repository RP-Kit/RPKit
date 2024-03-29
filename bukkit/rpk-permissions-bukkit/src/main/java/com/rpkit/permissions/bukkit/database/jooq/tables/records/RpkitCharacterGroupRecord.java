/*
 * This file is generated by jOOQ.
 */
package com.rpkit.permissions.bukkit.database.jooq.tables.records;


import com.rpkit.permissions.bukkit.database.jooq.tables.RpkitCharacterGroup;

import org.jooq.Field;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.TableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class RpkitCharacterGroupRecord extends TableRecordImpl<RpkitCharacterGroupRecord> implements Record3<Integer, String, Integer> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for
     * <code>rpkit_permissions.rpkit_character_group.character_id</code>.
     */
    public void setCharacterId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for
     * <code>rpkit_permissions.rpkit_character_group.character_id</code>.
     */
    public Integer getCharacterId() {
        return (Integer) get(0);
    }

    /**
     * Setter for
     * <code>rpkit_permissions.rpkit_character_group.group_name</code>.
     */
    public void setGroupName(String value) {
        set(1, value);
    }

    /**
     * Getter for
     * <code>rpkit_permissions.rpkit_character_group.group_name</code>.
     */
    public String getGroupName() {
        return (String) get(1);
    }

    /**
     * Setter for <code>rpkit_permissions.rpkit_character_group.priority</code>.
     */
    public void setPriority(Integer value) {
        set(2, value);
    }

    /**
     * Getter for <code>rpkit_permissions.rpkit_character_group.priority</code>.
     */
    public Integer getPriority() {
        return (Integer) get(2);
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row3<Integer, String, Integer> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    @Override
    public Row3<Integer, String, Integer> valuesRow() {
        return (Row3) super.valuesRow();
    }

    @Override
    public Field<Integer> field1() {
        return RpkitCharacterGroup.RPKIT_CHARACTER_GROUP.CHARACTER_ID;
    }

    @Override
    public Field<String> field2() {
        return RpkitCharacterGroup.RPKIT_CHARACTER_GROUP.GROUP_NAME;
    }

    @Override
    public Field<Integer> field3() {
        return RpkitCharacterGroup.RPKIT_CHARACTER_GROUP.PRIORITY;
    }

    @Override
    public Integer component1() {
        return getCharacterId();
    }

    @Override
    public String component2() {
        return getGroupName();
    }

    @Override
    public Integer component3() {
        return getPriority();
    }

    @Override
    public Integer value1() {
        return getCharacterId();
    }

    @Override
    public String value2() {
        return getGroupName();
    }

    @Override
    public Integer value3() {
        return getPriority();
    }

    @Override
    public RpkitCharacterGroupRecord value1(Integer value) {
        setCharacterId(value);
        return this;
    }

    @Override
    public RpkitCharacterGroupRecord value2(String value) {
        setGroupName(value);
        return this;
    }

    @Override
    public RpkitCharacterGroupRecord value3(Integer value) {
        setPriority(value);
        return this;
    }

    @Override
    public RpkitCharacterGroupRecord values(Integer value1, String value2, Integer value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached RpkitCharacterGroupRecord
     */
    public RpkitCharacterGroupRecord() {
        super(RpkitCharacterGroup.RPKIT_CHARACTER_GROUP);
    }

    /**
     * Create a detached, initialised RpkitCharacterGroupRecord
     */
    public RpkitCharacterGroupRecord(Integer characterId, String groupName, Integer priority) {
        super(RpkitCharacterGroup.RPKIT_CHARACTER_GROUP);

        setCharacterId(characterId);
        setGroupName(groupName);
        setPriority(priority);
    }
}
