package com.rpkit.featureflags.bukkit.featureflag

import com.rpkit.core.service.ServiceProvider
import com.rpkit.players.bukkit.profile.RPKProfile


interface RPKFeatureFlagProvider: ServiceProvider {

    fun getFeatureFlag(id: Int): RPKFeatureFlag?
    fun getFeatureFlag(name: String): RPKFeatureFlag?
    fun addFeatureFlag(featureFlag: RPKFeatureFlag)
    fun removeFeatureFlag(featureFlag: RPKFeatureFlag)
    fun updateFeatureFlag(featureFlag: RPKFeatureFlag)
    fun setFeatureFlag(profile: RPKProfile, featureFlag: RPKFeatureFlag, enabled: Boolean)

}