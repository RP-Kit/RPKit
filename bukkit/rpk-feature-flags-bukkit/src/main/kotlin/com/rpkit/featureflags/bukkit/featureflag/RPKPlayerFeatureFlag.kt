package com.rpkit.featureflags.bukkit.featureflag

import com.rpkit.core.database.Entity
import com.rpkit.players.bukkit.player.RPKPlayer


class RPKPlayerFeatureFlag(
        override var id: Int = 0,
        val player: RPKPlayer,
        val featureFlag: RPKFeatureFlag,
        var enabled: Boolean
) : Entity
