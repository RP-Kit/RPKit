/*
 * This file is generated by jOOQ.
*/
package com.rpkit.economy.bukkit.database.jooq.rpkit;


import com.rpkit.economy.bukkit.database.jooq.rpkit.tables.MoneyHidden;
import com.rpkit.economy.bukkit.database.jooq.rpkit.tables.RpkitCurrency;
import com.rpkit.economy.bukkit.database.jooq.rpkit.tables.RpkitWallet;

import javax.annotation.Generated;

import org.jooq.Index;
import org.jooq.OrderField;
import org.jooq.impl.AbstractKeys;


/**
 * A class modelling indexes of tables of the <code>rpkit</code> schema.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.10.2"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Indexes {

    // -------------------------------------------------------------------------
    // INDEX definitions
    // -------------------------------------------------------------------------

    public static final Index MONEY_HIDDEN_PRIMARY = Indexes0.MONEY_HIDDEN_PRIMARY;
    public static final Index RPKIT_CURRENCY_PRIMARY = Indexes0.RPKIT_CURRENCY_PRIMARY;
    public static final Index RPKIT_WALLET_PRIMARY = Indexes0.RPKIT_WALLET_PRIMARY;

    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class Indexes0 extends AbstractKeys {
        public static Index MONEY_HIDDEN_PRIMARY = createIndex("PRIMARY", MoneyHidden.MONEY_HIDDEN, new OrderField[] { MoneyHidden.MONEY_HIDDEN.ID }, true);
        public static Index RPKIT_CURRENCY_PRIMARY = createIndex("PRIMARY", RpkitCurrency.RPKIT_CURRENCY, new OrderField[] { RpkitCurrency.RPKIT_CURRENCY.ID }, true);
        public static Index RPKIT_WALLET_PRIMARY = createIndex("PRIMARY", RpkitWallet.RPKIT_WALLET, new OrderField[] { RpkitWallet.RPKIT_WALLET.ID }, true);
    }
}