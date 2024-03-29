/*
 * This file is generated by jOOQ.
 */
package com.rpkit.experience.bukkit.database.jooq;


import java.util.Arrays;
import java.util.List;

import org.jooq.Catalog;
import org.jooq.Table;
import org.jooq.impl.SchemaImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class RpkitExperience extends SchemaImpl {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>rpkit_experience</code>
     */
    public static final RpkitExperience RPKIT_EXPERIENCE = new RpkitExperience();

    /**
     * The table <code>rpkit_experience.rpkit_experience</code>.
     */
    public final com.rpkit.experience.bukkit.database.jooq.tables.RpkitExperience RPKIT_EXPERIENCE_ = com.rpkit.experience.bukkit.database.jooq.tables.RpkitExperience.RPKIT_EXPERIENCE_;

    /**
     * No further instances allowed
     */
    private RpkitExperience() {
        super("rpkit_experience", null);
    }


    @Override
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Table<?>> getTables() {
        return Arrays.asList(
            com.rpkit.experience.bukkit.database.jooq.tables.RpkitExperience.RPKIT_EXPERIENCE_
        );
    }
}
