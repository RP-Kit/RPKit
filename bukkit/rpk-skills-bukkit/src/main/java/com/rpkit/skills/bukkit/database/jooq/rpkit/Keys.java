/*
 * This file is generated by jOOQ.
*/
package com.rpkit.skills.bukkit.database.jooq.rpkit;


import com.rpkit.skills.bukkit.database.jooq.rpkit.tables.RpkitSkillCooldown;
import com.rpkit.skills.bukkit.database.jooq.rpkit.tables.records.RpkitSkillCooldownRecord;

import javax.annotation.Generated;

import org.jooq.Identity;
import org.jooq.UniqueKey;
import org.jooq.impl.AbstractKeys;


/**
 * A class modelling foreign key relationships between tables of the <code>rpkit</code> 
 * schema
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.9.2"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // IDENTITY definitions
    // -------------------------------------------------------------------------

    public static final Identity<RpkitSkillCooldownRecord, Integer> IDENTITY_RPKIT_SKILL_COOLDOWN = Identities0.IDENTITY_RPKIT_SKILL_COOLDOWN;

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<RpkitSkillCooldownRecord> KEY_RPKIT_SKILL_COOLDOWN_PRIMARY = UniqueKeys0.KEY_RPKIT_SKILL_COOLDOWN_PRIMARY;

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------


    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class Identities0 extends AbstractKeys {
        public static Identity<RpkitSkillCooldownRecord, Integer> IDENTITY_RPKIT_SKILL_COOLDOWN = createIdentity(RpkitSkillCooldown.RPKIT_SKILL_COOLDOWN, RpkitSkillCooldown.RPKIT_SKILL_COOLDOWN.ID);
    }

    private static class UniqueKeys0 extends AbstractKeys {
        public static final UniqueKey<RpkitSkillCooldownRecord> KEY_RPKIT_SKILL_COOLDOWN_PRIMARY = createUniqueKey(RpkitSkillCooldown.RPKIT_SKILL_COOLDOWN, "KEY_rpkit_skill_cooldown_PRIMARY", RpkitSkillCooldown.RPKIT_SKILL_COOLDOWN.ID);
    }
}