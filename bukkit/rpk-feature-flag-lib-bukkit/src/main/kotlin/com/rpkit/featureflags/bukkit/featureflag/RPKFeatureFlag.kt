package com.rpkit.featureflags.bukkit.featureflag

import com.rpkit.core.database.Entity
import com.rpkit.players.bukkit.player.RPKPlayer


interface RPKFeatureFlag: Entity {
    val name: String
    var isEnabledByDefault: Boolean
    fun isEnabledFor(player: RPKPlayer): Boolean
    fun setEnabledFor(player: RPKPlayer, enabled: Boolean)
}