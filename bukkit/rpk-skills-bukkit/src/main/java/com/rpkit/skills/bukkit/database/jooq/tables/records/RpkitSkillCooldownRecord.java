/*
 * Copyright 2020 Ren Binden
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * This file is generated by jOOQ.
 */
package com.rpkit.skills.bukkit.database.jooq.tables.records;


import com.rpkit.skills.bukkit.database.jooq.tables.RpkitSkillCooldown;

import java.time.LocalDateTime;

import org.jooq.Field;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.TableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class RpkitSkillCooldownRecord extends TableRecordImpl<RpkitSkillCooldownRecord> implements Record3<Integer, String, LocalDateTime> {

    private static final long serialVersionUID = 1950647631;

    /**
     * Setter for <code>rpkit_skills.rpkit_skill_cooldown.character_id</code>.
     */
    public void setCharacterId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>rpkit_skills.rpkit_skill_cooldown.character_id</code>.
     */
    public Integer getCharacterId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>rpkit_skills.rpkit_skill_cooldown.skill_name</code>.
     */
    public void setSkillName(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>rpkit_skills.rpkit_skill_cooldown.skill_name</code>.
     */
    public String getSkillName() {
        return (String) get(1);
    }

    /**
     * Setter for <code>rpkit_skills.rpkit_skill_cooldown.cooldown_timestamp</code>.
     */
    public void setCooldownTimestamp(LocalDateTime value) {
        set(2, value);
    }

    /**
     * Getter for <code>rpkit_skills.rpkit_skill_cooldown.cooldown_timestamp</code>.
     */
    public LocalDateTime getCooldownTimestamp() {
        return (LocalDateTime) get(2);
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row3<Integer, String, LocalDateTime> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    @Override
    public Row3<Integer, String, LocalDateTime> valuesRow() {
        return (Row3) super.valuesRow();
    }

    @Override
    public Field<Integer> field1() {
        return RpkitSkillCooldown.RPKIT_SKILL_COOLDOWN.CHARACTER_ID;
    }

    @Override
    public Field<String> field2() {
        return RpkitSkillCooldown.RPKIT_SKILL_COOLDOWN.SKILL_NAME;
    }

    @Override
    public Field<LocalDateTime> field3() {
        return RpkitSkillCooldown.RPKIT_SKILL_COOLDOWN.COOLDOWN_TIMESTAMP;
    }

    @Override
    public Integer component1() {
        return getCharacterId();
    }

    @Override
    public String component2() {
        return getSkillName();
    }

    @Override
    public LocalDateTime component3() {
        return getCooldownTimestamp();
    }

    @Override
    public Integer value1() {
        return getCharacterId();
    }

    @Override
    public String value2() {
        return getSkillName();
    }

    @Override
    public LocalDateTime value3() {
        return getCooldownTimestamp();
    }

    @Override
    public RpkitSkillCooldownRecord value1(Integer value) {
        setCharacterId(value);
        return this;
    }

    @Override
    public RpkitSkillCooldownRecord value2(String value) {
        setSkillName(value);
        return this;
    }

    @Override
    public RpkitSkillCooldownRecord value3(LocalDateTime value) {
        setCooldownTimestamp(value);
        return this;
    }

    @Override
    public RpkitSkillCooldownRecord values(Integer value1, String value2, LocalDateTime value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached RpkitSkillCooldownRecord
     */
    public RpkitSkillCooldownRecord() {
        super(RpkitSkillCooldown.RPKIT_SKILL_COOLDOWN);
    }

    /**
     * Create a detached, initialised RpkitSkillCooldownRecord
     */
    public RpkitSkillCooldownRecord(Integer characterId, String skillName, LocalDateTime cooldownTimestamp) {
        super(RpkitSkillCooldown.RPKIT_SKILL_COOLDOWN);

        set(0, characterId);
        set(1, skillName);
        set(2, cooldownTimestamp);
    }
}