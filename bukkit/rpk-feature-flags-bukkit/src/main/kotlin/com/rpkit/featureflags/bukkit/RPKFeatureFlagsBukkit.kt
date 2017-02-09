package com.rpkit.featureflags.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.featureflags.bukkit.database.table.RPKFeatureFlagTable
import com.rpkit.featureflags.bukkit.database.table.RPKPlayerFeatureFlagTable
import com.rpkit.featureflags.bukkit.featureflag.RPKFeatureFlagProviderImpl


class RPKFeatureFlagsBukkit: RPKBukkitPlugin() {

    override fun onEnable() {
        serviceProviders = arrayOf(
                RPKFeatureFlagProviderImpl(this)
        )
    }

    override fun createTables(database: Database) {
        database.addTable(RPKFeatureFlagTable(database, this))
        database.addTable(RPKPlayerFeatureFlagTable(database, this))
    }

}