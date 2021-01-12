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
package com.rpkit.selection.bukkit.database.jooq.tables.records;


import com.rpkit.selection.bukkit.database.jooq.tables.RpkitSelection;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record8;
import org.jooq.Row8;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class RpkitSelectionRecord extends UpdatableRecordImpl<RpkitSelectionRecord> implements Record8<Integer, String, Integer, Integer, Integer, Integer, Integer, Integer> {

    private static final long serialVersionUID = -937755394;

    /**
     * Setter for <code>rpkit_selection.rpkit_selection.minecraft_profile_id</code>.
     */
    public void setMinecraftProfileId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>rpkit_selection.rpkit_selection.minecraft_profile_id</code>.
     */
    public Integer getMinecraftProfileId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>rpkit_selection.rpkit_selection.world</code>.
     */
    public void setWorld(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>rpkit_selection.rpkit_selection.world</code>.
     */
    public String getWorld() {
        return (String) get(1);
    }

    /**
     * Setter for <code>rpkit_selection.rpkit_selection.x_1</code>.
     */
    public void setX_1(Integer value) {
        set(2, value);
    }

    /**
     * Getter for <code>rpkit_selection.rpkit_selection.x_1</code>.
     */
    public Integer getX_1() {
        return (Integer) get(2);
    }

    /**
     * Setter for <code>rpkit_selection.rpkit_selection.y_1</code>.
     */
    public void setY_1(Integer value) {
        set(3, value);
    }

    /**
     * Getter for <code>rpkit_selection.rpkit_selection.y_1</code>.
     */
    public Integer getY_1() {
        return (Integer) get(3);
    }

    /**
     * Setter for <code>rpkit_selection.rpkit_selection.z_1</code>.
     */
    public void setZ_1(Integer value) {
        set(4, value);
    }

    /**
     * Getter for <code>rpkit_selection.rpkit_selection.z_1</code>.
     */
    public Integer getZ_1() {
        return (Integer) get(4);
    }

    /**
     * Setter for <code>rpkit_selection.rpkit_selection.x_2</code>.
     */
    public void setX_2(Integer value) {
        set(5, value);
    }

    /**
     * Getter for <code>rpkit_selection.rpkit_selection.x_2</code>.
     */
    public Integer getX_2() {
        return (Integer) get(5);
    }

    /**
     * Setter for <code>rpkit_selection.rpkit_selection.y_2</code>.
     */
    public void setY_2(Integer value) {
        set(6, value);
    }

    /**
     * Getter for <code>rpkit_selection.rpkit_selection.y_2</code>.
     */
    public Integer getY_2() {
        return (Integer) get(6);
    }

    /**
     * Setter for <code>rpkit_selection.rpkit_selection.z_2</code>.
     */
    public void setZ_2(Integer value) {
        set(7, value);
    }

    /**
     * Getter for <code>rpkit_selection.rpkit_selection.z_2</code>.
     */
    public Integer getZ_2() {
        return (Integer) get(7);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Integer> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record8 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row8<Integer, String, Integer, Integer, Integer, Integer, Integer, Integer> fieldsRow() {
        return (Row8) super.fieldsRow();
    }

    @Override
    public Row8<Integer, String, Integer, Integer, Integer, Integer, Integer, Integer> valuesRow() {
        return (Row8) super.valuesRow();
    }

    @Override
    public Field<Integer> field1() {
        return RpkitSelection.RPKIT_SELECTION_.MINECRAFT_PROFILE_ID;
    }

    @Override
    public Field<String> field2() {
        return RpkitSelection.RPKIT_SELECTION_.WORLD;
    }

    @Override
    public Field<Integer> field3() {
        return RpkitSelection.RPKIT_SELECTION_.X_1;
    }

    @Override
    public Field<Integer> field4() {
        return RpkitSelection.RPKIT_SELECTION_.Y_1;
    }

    @Override
    public Field<Integer> field5() {
        return RpkitSelection.RPKIT_SELECTION_.Z_1;
    }

    @Override
    public Field<Integer> field6() {
        return RpkitSelection.RPKIT_SELECTION_.X_2;
    }

    @Override
    public Field<Integer> field7() {
        return RpkitSelection.RPKIT_SELECTION_.Y_2;
    }

    @Override
    public Field<Integer> field8() {
        return RpkitSelection.RPKIT_SELECTION_.Z_2;
    }

    @Override
    public Integer component1() {
        return getMinecraftProfileId();
    }

    @Override
    public String component2() {
        return getWorld();
    }

    @Override
    public Integer component3() {
        return getX_1();
    }

    @Override
    public Integer component4() {
        return getY_1();
    }

    @Override
    public Integer component5() {
        return getZ_1();
    }

    @Override
    public Integer component6() {
        return getX_2();
    }

    @Override
    public Integer component7() {
        return getY_2();
    }

    @Override
    public Integer component8() {
        return getZ_2();
    }

    @Override
    public Integer value1() {
        return getMinecraftProfileId();
    }

    @Override
    public String value2() {
        return getWorld();
    }

    @Override
    public Integer value3() {
        return getX_1();
    }

    @Override
    public Integer value4() {
        return getY_1();
    }

    @Override
    public Integer value5() {
        return getZ_1();
    }

    @Override
    public Integer value6() {
        return getX_2();
    }

    @Override
    public Integer value7() {
        return getY_2();
    }

    @Override
    public Integer value8() {
        return getZ_2();
    }

    @Override
    public RpkitSelectionRecord value1(Integer value) {
        setMinecraftProfileId(value);
        return this;
    }

    @Override
    public RpkitSelectionRecord value2(String value) {
        setWorld(value);
        return this;
    }

    @Override
    public RpkitSelectionRecord value3(Integer value) {
        setX_1(value);
        return this;
    }

    @Override
    public RpkitSelectionRecord value4(Integer value) {
        setY_1(value);
        return this;
    }

    @Override
    public RpkitSelectionRecord value5(Integer value) {
        setZ_1(value);
        return this;
    }

    @Override
    public RpkitSelectionRecord value6(Integer value) {
        setX_2(value);
        return this;
    }

    @Override
    public RpkitSelectionRecord value7(Integer value) {
        setY_2(value);
        return this;
    }

    @Override
    public RpkitSelectionRecord value8(Integer value) {
        setZ_2(value);
        return this;
    }

    @Override
    public RpkitSelectionRecord values(Integer value1, String value2, Integer value3, Integer value4, Integer value5, Integer value6, Integer value7, Integer value8) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached RpkitSelectionRecord
     */
    public RpkitSelectionRecord() {
        super(RpkitSelection.RPKIT_SELECTION_);
    }

    /**
     * Create a detached, initialised RpkitSelectionRecord
     */
    public RpkitSelectionRecord(Integer minecraftProfileId, String world, Integer x_1, Integer y_1, Integer z_1, Integer x_2, Integer y_2, Integer z_2) {
        super(RpkitSelection.RPKIT_SELECTION_);

        set(0, minecraftProfileId);
        set(1, world);
        set(2, x_1);
        set(3, y_1);
        set(4, z_1);
        set(5, x_2);
        set(6, y_2);
        set(7, z_2);
    }
}