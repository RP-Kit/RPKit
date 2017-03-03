package com.rpkit.featureflags.bukkit.featureflag

import com.rpkit.featureflags.bukkit.RPKFeatureFlagsBukkit
import com.rpkit.featureflags.bukkit.database.table.RPKPlayerFeatureFlagTable
import com.rpkit.players.bukkit.player.RPKPlayer


class RPKFeatureFlagImpl(
        private val plugin: RPKFeatureFlagsBukkit,
        override var id: Int = 0,
        override val name: String,
        override var isEnabledByDefault: Boolean
) : RPKFeatureFlag {

    override fun isEnabledFor(player: RPKPlayer): Boolean {
        return plugin.core.database.getTable(RPKPlayerFeatureFlagTable::class).get(player, this)?.enabled?:isEnabledByDefault
    }

    override fun setEnabledFor(player: RPKPlayer, enabled: Boolean) {
        val playerFeatureFlagTable = plugin.core.database.getTable(RPKPlayerFeatureFlagTable::class)
        if (isEnabledFor(player) != enabled) {
            var playerFeatureFlag = playerFeatureFlagTable.get(player, this)
            if (playerFeatureFlag == null) {
                playerFeatureFlag = RPKPlayerFeatureFlag(player = player, featureFlag = this, enabled = enabled)
                playerFeatureFlagTable.insert(playerFeatureFlag)
            } else {
                playerFeatureFlag.enabled = enabled
                playerFeatureFlagTable.update(playerFeatureFlag)
            }
        }
    }

}