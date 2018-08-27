package com.rpkit.featureflags.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.featureflags.bukkit.database.table.RPKFeatureFlagTable
import com.rpkit.featureflags.bukkit.database.table.RPKProfileFeatureFlagTable
import com.rpkit.featureflags.bukkit.featureflag.RPKFeatureFlagProviderImpl


class RPKFeatureFlagsBukkit: RPKBukkitPlugin() {

    override fun onEnable() {
        saveDefaultConfig()
        serviceProviders = arrayOf(
                RPKFeatureFlagProviderImpl(this)
        )
    }

    override fun createTables(database: Database) {
        database.addTable(RPKFeatureFlagTable(database, this))
        database.addTable(RPKProfileFeatureFlagTable(database, this))
    }

}