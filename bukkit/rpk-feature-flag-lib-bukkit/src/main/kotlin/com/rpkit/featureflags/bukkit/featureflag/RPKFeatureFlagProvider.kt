package com.rpkit.featureflags.bukkit.featureflag

import com.rpkit.core.service.ServiceProvider


interface RPKFeatureFlagProvider: ServiceProvider {

    fun getFeatureFlag(id: Int): RPKFeatureFlag?
    fun getFeatureFlag(name: String): RPKFeatureFlag?
    fun addFeatureFlag(featureFlag: RPKFeatureFlag)
    fun removeFeatureFlag(featureFlag: RPKFeatureFlag)

}