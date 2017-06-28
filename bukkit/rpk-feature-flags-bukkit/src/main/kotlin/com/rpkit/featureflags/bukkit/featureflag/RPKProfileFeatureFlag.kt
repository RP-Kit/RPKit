package com.rpkit.featureflags.bukkit.featureflag

import com.rpkit.core.database.Entity
import com.rpkit.players.bukkit.profile.RPKProfile


class RPKProfileFeatureFlag(
        override var id: Int = 0,
        val profile: RPKProfile,
        val featureFlag: RPKFeatureFlag,
        var enabled: Boolean
) : Entity
