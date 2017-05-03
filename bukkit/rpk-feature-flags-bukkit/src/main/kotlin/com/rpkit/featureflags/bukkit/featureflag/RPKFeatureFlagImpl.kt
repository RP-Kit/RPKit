package com.rpkit.featureflags.bukkit.featureflag

import com.rpkit.featureflags.bukkit.RPKFeatureFlagsBukkit
import com.rpkit.featureflags.bukkit.database.table.RPKPlayerFeatureFlagTable
import com.rpkit.players.bukkit.profile.RPKProfile


class RPKFeatureFlagImpl(
        private val plugin: RPKFeatureFlagsBukkit,
        override var id: Int = 0,
        override val name: String,
        override var isEnabledByDefault: Boolean
) : RPKFeatureFlag {

    override fun isEnabledFor(profile: RPKProfile): Boolean {
        return plugin.core.database.getTable(RPKPlayerFeatureFlagTable::class).get(profile, this)?.enabled?:isEnabledByDefault
    }

    override fun setEnabledFor(profile: RPKProfile, enabled: Boolean) {
        val playerFeatureFlagTable = plugin.core.database.getTable(RPKPlayerFeatureFlagTable::class)
        if (isEnabledFor(profile) != enabled) {
            var playerFeatureFlag = playerFeatureFlagTable.get(profile, this)
            if (playerFeatureFlag == null) {
                playerFeatureFlag = RPKProfileFeatureFlag(profile = profile, featureFlag = this, enabled = enabled)
                playerFeatureFlagTable.insert(playerFeatureFlag)
            } else {
                playerFeatureFlag.enabled = enabled
                playerFeatureFlagTable.update(playerFeatureFlag)
            }
        }
    }

}