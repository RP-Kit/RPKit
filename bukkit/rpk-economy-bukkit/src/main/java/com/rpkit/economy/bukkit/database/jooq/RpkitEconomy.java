/*
 * This file is generated by jOOQ.
 */
package com.rpkit.economy.bukkit.database.jooq;


import com.rpkit.economy.bukkit.database.jooq.tables.RpkitMoneyHidden;
import com.rpkit.economy.bukkit.database.jooq.tables.RpkitWallet;

import java.util.Arrays;
import java.util.List;

import org.jooq.Catalog;
import org.jooq.Table;
import org.jooq.impl.SchemaImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class RpkitEconomy extends SchemaImpl {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>rpkit_economy</code>
     */
    public static final RpkitEconomy RPKIT_ECONOMY = new RpkitEconomy();

    /**
     * The table <code>rpkit_economy.rpkit_money_hidden</code>.
     */
    public final RpkitMoneyHidden RPKIT_MONEY_HIDDEN = RpkitMoneyHidden.RPKIT_MONEY_HIDDEN;

    /**
     * The table <code>rpkit_economy.rpkit_wallet</code>.
     */
    public final RpkitWallet RPKIT_WALLET = RpkitWallet.RPKIT_WALLET;

    /**
     * No further instances allowed
     */
    private RpkitEconomy() {
        super("rpkit_economy", null);
    }


    @Override
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Table<?>> getTables() {
        return Arrays.asList(
            RpkitMoneyHidden.RPKIT_MONEY_HIDDEN,
            RpkitWallet.RPKIT_WALLET
        );
    }
}
