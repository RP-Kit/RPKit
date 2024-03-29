/*
 * This file is generated by jOOQ.
 */
package com.rpkit.moderation.bukkit.database.jooq;


import com.rpkit.moderation.bukkit.database.jooq.tables.RpkitTicket;
import com.rpkit.moderation.bukkit.database.jooq.tables.RpkitVanished;
import com.rpkit.moderation.bukkit.database.jooq.tables.RpkitWarning;
import com.rpkit.moderation.bukkit.database.jooq.tables.records.RpkitTicketRecord;
import com.rpkit.moderation.bukkit.database.jooq.tables.records.RpkitVanishedRecord;
import com.rpkit.moderation.bukkit.database.jooq.tables.records.RpkitWarningRecord;

import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;


/**
 * A class modelling foreign key relationships and constraints of tables in
 * rpkit_moderation.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<RpkitTicketRecord> KEY_RPKIT_TICKET_PRIMARY = Internal.createUniqueKey(RpkitTicket.RPKIT_TICKET, DSL.name("KEY_rpkit_ticket_PRIMARY"), new TableField[] { RpkitTicket.RPKIT_TICKET.ID }, true);
    public static final UniqueKey<RpkitVanishedRecord> KEY_RPKIT_VANISHED_PRIMARY = Internal.createUniqueKey(RpkitVanished.RPKIT_VANISHED, DSL.name("KEY_rpkit_vanished_PRIMARY"), new TableField[] { RpkitVanished.RPKIT_VANISHED.MINECRAFT_PROFILE_ID }, true);
    public static final UniqueKey<RpkitWarningRecord> KEY_RPKIT_WARNING_PRIMARY = Internal.createUniqueKey(RpkitWarning.RPKIT_WARNING, DSL.name("KEY_rpkit_warning_PRIMARY"), new TableField[] { RpkitWarning.RPKIT_WARNING.ID }, true);
}
