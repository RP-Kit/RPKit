/*
 * This file is generated by jOOQ.
 */
package com.rpkit.professions.bukkit.database.jooq;


import com.rpkit.professions.bukkit.database.jooq.tables.RpkitCharacterProfessionChangeCooldown;
import com.rpkit.professions.bukkit.database.jooq.tables.RpkitCharacterProfessionExperience;
import com.rpkit.professions.bukkit.database.jooq.tables.RpkitProfessionHidden;
import com.rpkit.professions.bukkit.database.jooq.tables.records.RpkitCharacterProfessionChangeCooldownRecord;
import com.rpkit.professions.bukkit.database.jooq.tables.records.RpkitCharacterProfessionExperienceRecord;
import com.rpkit.professions.bukkit.database.jooq.tables.records.RpkitProfessionHiddenRecord;

import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;


/**
 * A class modelling foreign key relationships and constraints of tables in
 * rpkit_professions.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<RpkitCharacterProfessionChangeCooldownRecord> KEY_RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN_PRIMARY = Internal.createUniqueKey(RpkitCharacterProfessionChangeCooldown.RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN, DSL.name("KEY_rpkit_character_profession_change_cooldown_PRIMARY"), new TableField[] { RpkitCharacterProfessionChangeCooldown.RPKIT_CHARACTER_PROFESSION_CHANGE_COOLDOWN.CHARACTER_ID }, true);
    public static final UniqueKey<RpkitCharacterProfessionExperienceRecord> KEY_RPKIT_CHARACTER_PROFESSION_EXPERIENCE_PRIMARY = Internal.createUniqueKey(RpkitCharacterProfessionExperience.RPKIT_CHARACTER_PROFESSION_EXPERIENCE, DSL.name("KEY_rpkit_character_profession_experience_PRIMARY"), new TableField[] { RpkitCharacterProfessionExperience.RPKIT_CHARACTER_PROFESSION_EXPERIENCE.CHARACTER_ID, RpkitCharacterProfessionExperience.RPKIT_CHARACTER_PROFESSION_EXPERIENCE.PROFESSION }, true);
    public static final UniqueKey<RpkitProfessionHiddenRecord> KEY_RPKIT_PROFESSION_HIDDEN_PRIMARY = Internal.createUniqueKey(RpkitProfessionHidden.RPKIT_PROFESSION_HIDDEN, DSL.name("KEY_rpkit_profession_hidden_PRIMARY"), new TableField[] { RpkitProfessionHidden.RPKIT_PROFESSION_HIDDEN.CHARACTER_ID }, true);
}
