/*
 * This file is generated by jOOQ.
 */
package com.rpkit.auctions.bukkit.database.jooq.tables;


import com.rpkit.auctions.bukkit.database.jooq.Keys;
import com.rpkit.auctions.bukkit.database.jooq.RpkitAuctions;
import com.rpkit.auctions.bukkit.database.jooq.tables.records.RpkitAuctionRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import java.util.Arrays;
import java.util.List;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class RpkitAuction extends TableImpl<RpkitAuctionRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>rpkit_auctions.rpkit_auction</code>
     */
    public static final RpkitAuction RPKIT_AUCTION = new RpkitAuction();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<RpkitAuctionRecord> getRecordType() {
        return RpkitAuctionRecord.class;
    }

    /**
     * The column <code>rpkit_auctions.rpkit_auction.id</code>.
     */
    public final TableField<RpkitAuctionRecord, Integer> ID = createField(DSL.name("id"), SQLDataType.INTEGER.nullable(false).identity(true), this, "");

    /**
     * The column <code>rpkit_auctions.rpkit_auction.item</code>.
     */
    public final TableField<RpkitAuctionRecord, byte[]> ITEM = createField(DSL.name("item"), SQLDataType.BLOB.nullable(false), this, "");

    /**
     * The column <code>rpkit_auctions.rpkit_auction.currency_name</code>.
     */
    public final TableField<RpkitAuctionRecord, String> CURRENCY_NAME = createField(DSL.name("currency_name"), SQLDataType.VARCHAR(256).nullable(false), this, "");

    /**
     * The column <code>rpkit_auctions.rpkit_auction.world</code>.
     */
    public final TableField<RpkitAuctionRecord, String> WORLD = createField(DSL.name("world"), SQLDataType.VARCHAR(256).defaultValue(DSL.inline("NULL", SQLDataType.VARCHAR)), this, "");

    /**
     * The column <code>rpkit_auctions.rpkit_auction.x</code>.
     */
    public final TableField<RpkitAuctionRecord, Double> X = createField(DSL.name("x"), SQLDataType.DOUBLE.defaultValue(DSL.inline("NULL", SQLDataType.DOUBLE)), this, "");

    /**
     * The column <code>rpkit_auctions.rpkit_auction.y</code>.
     */
    public final TableField<RpkitAuctionRecord, Double> Y = createField(DSL.name("y"), SQLDataType.DOUBLE.defaultValue(DSL.inline("NULL", SQLDataType.DOUBLE)), this, "");

    /**
     * The column <code>rpkit_auctions.rpkit_auction.z</code>.
     */
    public final TableField<RpkitAuctionRecord, Double> Z = createField(DSL.name("z"), SQLDataType.DOUBLE.defaultValue(DSL.inline("NULL", SQLDataType.DOUBLE)), this, "");

    /**
     * The column <code>rpkit_auctions.rpkit_auction.yaw</code>.
     */
    public final TableField<RpkitAuctionRecord, Double> YAW = createField(DSL.name("yaw"), SQLDataType.DOUBLE.defaultValue(DSL.inline("NULL", SQLDataType.DOUBLE)), this, "");

    /**
     * The column <code>rpkit_auctions.rpkit_auction.pitch</code>.
     */
    public final TableField<RpkitAuctionRecord, Double> PITCH = createField(DSL.name("pitch"), SQLDataType.DOUBLE.defaultValue(DSL.inline("NULL", SQLDataType.DOUBLE)), this, "");

    /**
     * The column <code>rpkit_auctions.rpkit_auction.character_id</code>.
     */
    public final TableField<RpkitAuctionRecord, Integer> CHARACTER_ID = createField(DSL.name("character_id"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>rpkit_auctions.rpkit_auction.duration</code>.
     */
    public final TableField<RpkitAuctionRecord, Long> DURATION = createField(DSL.name("duration"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>rpkit_auctions.rpkit_auction.end_time</code>.
     */
    public final TableField<RpkitAuctionRecord, Long> END_TIME = createField(DSL.name("end_time"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>rpkit_auctions.rpkit_auction.start_price</code>.
     */
    public final TableField<RpkitAuctionRecord, Integer> START_PRICE = createField(DSL.name("start_price"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>rpkit_auctions.rpkit_auction.buy_out_price</code>.
     */
    public final TableField<RpkitAuctionRecord, Integer> BUY_OUT_PRICE = createField(DSL.name("buy_out_price"), SQLDataType.INTEGER.defaultValue(DSL.inline("NULL", SQLDataType.INTEGER)), this, "");

    /**
     * The column <code>rpkit_auctions.rpkit_auction.no_sell_price</code>.
     */
    public final TableField<RpkitAuctionRecord, Integer> NO_SELL_PRICE = createField(DSL.name("no_sell_price"), SQLDataType.INTEGER.defaultValue(DSL.inline("NULL", SQLDataType.INTEGER)), this, "");

    /**
     * The column <code>rpkit_auctions.rpkit_auction.minimum_bid_increment</code>.
     */
    public final TableField<RpkitAuctionRecord, Integer> MINIMUM_BID_INCREMENT = createField(DSL.name("minimum_bid_increment"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>rpkit_auctions.rpkit_auction.bidding_open</code>.
     */
    public final TableField<RpkitAuctionRecord, Boolean> BIDDING_OPEN = createField(DSL.name("bidding_open"), SQLDataType.BOOLEAN.nullable(false), this, "");

    private RpkitAuction(Name alias, Table<RpkitAuctionRecord> aliased) {
        this(alias, aliased, null);
    }

    private RpkitAuction(Name alias, Table<RpkitAuctionRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>rpkit_auctions.rpkit_auction</code> table reference
     */
    public RpkitAuction(String alias) {
        this(DSL.name(alias), RPKIT_AUCTION);
    }

    /**
     * Create an aliased <code>rpkit_auctions.rpkit_auction</code> table reference
     */
    public RpkitAuction(Name alias) {
        this(alias, RPKIT_AUCTION);
    }

    /**
     * Create a <code>rpkit_auctions.rpkit_auction</code> table reference
     */
    public RpkitAuction() {
        this(DSL.name("rpkit_auction"), null);
    }

    public <O extends Record> RpkitAuction(Table<O> child, ForeignKey<O, RpkitAuctionRecord> key) {
        super(child, key, RPKIT_AUCTION);
    }

    @Override
    public Schema getSchema() {
        return RpkitAuctions.RPKIT_AUCTIONS;
    }

    @Override
    public Identity<RpkitAuctionRecord, Integer> getIdentity() {
        return (Identity<RpkitAuctionRecord, Integer>) super.getIdentity();
    }

    @Override
    public UniqueKey<RpkitAuctionRecord> getPrimaryKey() {
        return Keys.KEY_RPKIT_AUCTION_PRIMARY;
    }

    @Override
    public List<UniqueKey<RpkitAuctionRecord>> getKeys() {
        return Arrays.<UniqueKey<RpkitAuctionRecord>>asList(Keys.KEY_RPKIT_AUCTION_PRIMARY);
    }

    @Override
    public RpkitAuction as(String alias) {
        return new RpkitAuction(DSL.name(alias), this);
    }

    @Override
    public RpkitAuction as(Name alias) {
        return new RpkitAuction(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public RpkitAuction rename(String name) {
        return new RpkitAuction(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public RpkitAuction rename(Name name) {
        return new RpkitAuction(name, null);
    }

    // -------------------------------------------------------------------------
    // Row17 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row17<Integer, byte[], String, String, Double, Double, Double, Double, Double, Integer, Long, Long, Integer, Integer, Integer, Integer, Boolean> fieldsRow() {
        return (Row17) super.fieldsRow();
    }
}
