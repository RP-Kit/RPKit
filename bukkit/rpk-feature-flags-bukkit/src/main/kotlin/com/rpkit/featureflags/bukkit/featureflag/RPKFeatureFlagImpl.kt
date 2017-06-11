package com.rpkit.featureflags.bukkit.featureflag

import com.rpkit.featureflags.bukkit.RPKFeatureFlagsBukkit
import com.rpkit.featureflags.bukkit.database.table.RPKProfileFeatureFlagTable
import com.rpkit.players.bukkit.profile.RPKProfile


class RPKFeatureFlagImpl(
        private val plugin: RPKFeatureFlagsBukkit,
        override var id: Int = 0,
        override val name: String,
        override var isEnabledByDefault: Boolean
) : RPKFeatureFlag {

    override fun isEnabledFor(profile: RPKProfile): Boolean {
        return plugin.core.database.getTable(RPKProfileFeatureFlagTable::class).get(profile, this)?.enabled?:isEnabledByDefault
    }

    override fun setEnabledFor(profile: RPKProfile, enabled: Boolean) {
        val profileFeatureFlagTable = plugin.core.database.getTable(RPKProfileFeatureFlagTable::class)
        if (isEnabledFor(profile) != enabled) {
            var playerFeatureFlag = profileFeatureFlagTable.get(profile, this)
            if (playerFeatureFlag == null) {
                playerFeatureFlag = RPKProfileFeatureFlag(profile = profile, featureFlag = this, enabled = enabled)
                profileFeatureFlagTable.insert(playerFeatureFlag)
            } else {
                playerFeatureFlag.enabled = enabled
                profileFeatureFlagTable.update(playerFeatureFlag)
            }
        }
    }

}