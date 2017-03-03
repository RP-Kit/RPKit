package com.rpkit.featureflags.bukkit.featureflag

import com.rpkit.featureflags.bukkit.RPKFeatureFlagsBukkit
import com.rpkit.featureflags.bukkit.database.table.RPKFeatureFlagTable


class RPKFeatureFlagProviderImpl(private val plugin: RPKFeatureFlagsBukkit): RPKFeatureFlagProvider {

    override fun getFeatureFlag(id: Int): RPKFeatureFlag? {
        return plugin.core.database.getTable(RPKFeatureFlagTable::class)[id]
    }

    override fun getFeatureFlag(name: String): RPKFeatureFlag? {
        return plugin.core.database.getTable(RPKFeatureFlagTable::class).get(name)
    }

    override fun addFeatureFlag(featureFlag: RPKFeatureFlag) {
        plugin.core.database.getTable(RPKFeatureFlagTable::class).insert(featureFlag)
    }

    override fun removeFeatureFlag(featureFlag: RPKFeatureFlag) {
        plugin.core.database.getTable(RPKFeatureFlagTable::class).delete(featureFlag)
    }



}