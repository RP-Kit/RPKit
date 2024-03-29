/*
 * This file is generated by jOOQ.
 */
package com.rpkit.store.bukkit.database.jooq.tables.records;


import com.rpkit.store.bukkit.database.jooq.tables.RpkitConsumablePurchase;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class RpkitConsumablePurchaseRecord extends UpdatableRecordImpl<RpkitConsumablePurchaseRecord> implements Record3<Integer, Integer, Integer> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>rpkit_stores.rpkit_consumable_purchase.id</code>.
     */
    public void setId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>rpkit_stores.rpkit_consumable_purchase.id</code>.
     */
    public Integer getId() {
        return (Integer) get(0);
    }

    /**
     * Setter for
     * <code>rpkit_stores.rpkit_consumable_purchase.purchase_id</code>.
     */
    public void setPurchaseId(Integer value) {
        set(1, value);
    }

    /**
     * Getter for
     * <code>rpkit_stores.rpkit_consumable_purchase.purchase_id</code>.
     */
    public Integer getPurchaseId() {
        return (Integer) get(1);
    }

    /**
     * Setter for
     * <code>rpkit_stores.rpkit_consumable_purchase.remaining_uses</code>.
     */
    public void setRemainingUses(Integer value) {
        set(2, value);
    }

    /**
     * Getter for
     * <code>rpkit_stores.rpkit_consumable_purchase.remaining_uses</code>.
     */
    public Integer getRemainingUses() {
        return (Integer) get(2);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Integer> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row3<Integer, Integer, Integer> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    @Override
    public Row3<Integer, Integer, Integer> valuesRow() {
        return (Row3) super.valuesRow();
    }

    @Override
    public Field<Integer> field1() {
        return RpkitConsumablePurchase.RPKIT_CONSUMABLE_PURCHASE.ID;
    }

    @Override
    public Field<Integer> field2() {
        return RpkitConsumablePurchase.RPKIT_CONSUMABLE_PURCHASE.PURCHASE_ID;
    }

    @Override
    public Field<Integer> field3() {
        return RpkitConsumablePurchase.RPKIT_CONSUMABLE_PURCHASE.REMAINING_USES;
    }

    @Override
    public Integer component1() {
        return getId();
    }

    @Override
    public Integer component2() {
        return getPurchaseId();
    }

    @Override
    public Integer component3() {
        return getRemainingUses();
    }

    @Override
    public Integer value1() {
        return getId();
    }

    @Override
    public Integer value2() {
        return getPurchaseId();
    }

    @Override
    public Integer value3() {
        return getRemainingUses();
    }

    @Override
    public RpkitConsumablePurchaseRecord value1(Integer value) {
        setId(value);
        return this;
    }

    @Override
    public RpkitConsumablePurchaseRecord value2(Integer value) {
        setPurchaseId(value);
        return this;
    }

    @Override
    public RpkitConsumablePurchaseRecord value3(Integer value) {
        setRemainingUses(value);
        return this;
    }

    @Override
    public RpkitConsumablePurchaseRecord values(Integer value1, Integer value2, Integer value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached RpkitConsumablePurchaseRecord
     */
    public RpkitConsumablePurchaseRecord() {
        super(RpkitConsumablePurchase.RPKIT_CONSUMABLE_PURCHASE);
    }

    /**
     * Create a detached, initialised RpkitConsumablePurchaseRecord
     */
    public RpkitConsumablePurchaseRecord(Integer id, Integer purchaseId, Integer remainingUses) {
        super(RpkitConsumablePurchase.RPKIT_CONSUMABLE_PURCHASE);

        setId(id);
        setPurchaseId(purchaseId);
        setRemainingUses(remainingUses);
    }
}
