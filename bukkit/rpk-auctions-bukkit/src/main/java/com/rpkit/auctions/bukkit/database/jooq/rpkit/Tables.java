/*
 * This file is generated by jOOQ.
*/
package com.rpkit.auctions.bukkit.database.jooq.rpkit;


import com.rpkit.auctions.bukkit.database.jooq.rpkit.tables.RpkitAuction;
import com.rpkit.auctions.bukkit.database.jooq.rpkit.tables.RpkitBid;

import javax.annotation.Generated;


/**
 * Convenience access to all tables in rpkit
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.9.2"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Tables {

    /**
     * The table <code>rpkit.rpkit_auction</code>.
     */
    public static final RpkitAuction RPKIT_AUCTION = RpkitAuction.RPKIT_AUCTION;

    /**
     * The table <code>rpkit.rpkit_bid</code>.
     */
    public static final RpkitBid RPKIT_BID = RpkitBid.RPKIT_BID;
}
