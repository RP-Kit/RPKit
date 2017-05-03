package com.rpkit.featureflags.bukkit.featureflag

import com.rpkit.core.database.Entity
import com.rpkit.players.bukkit.profile.RPKProfile


interface RPKFeatureFlag: Entity {
    val name: String
    var isEnabledByDefault: Boolean
    fun isEnabledFor(profile: RPKProfile): Boolean
    fun setEnabledFor(profile: RPKProfile, enabled: Boolean)
}