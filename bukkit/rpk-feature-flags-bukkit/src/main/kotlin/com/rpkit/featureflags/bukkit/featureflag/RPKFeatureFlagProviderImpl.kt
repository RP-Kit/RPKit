package com.rpkit.featureflags.bukkit.featureflag

import com.rpkit.featureflags.bukkit.RPKFeatureFlagsBukkit
import com.rpkit.featureflags.bukkit.database.table.RPKFeatureFlagTable
import com.rpkit.featureflags.bukkit.database.table.RPKProfileFeatureFlagTable
import com.rpkit.featureflags.bukkit.event.featureflag.RPKBukkitFeatureFlagCreateEvent
import com.rpkit.featureflags.bukkit.event.featureflag.RPKBukkitFeatureFlagDeleteEvent
import com.rpkit.featureflags.bukkit.event.featureflag.RPKBukkitFeatureFlagSetEvent
import com.rpkit.featureflags.bukkit.event.featureflag.RPKBukkitFeatureFlagUpdateEvent
import com.rpkit.players.bukkit.profile.RPKProfile


class RPKFeatureFlagProviderImpl(private val plugin: RPKFeatureFlagsBukkit): RPKFeatureFlagProvider {

    override fun getFeatureFlag(id: Int): RPKFeatureFlag? {
        return plugin.core.database.getTable(RPKFeatureFlagTable::class)[id]
    }

    override fun getFeatureFlag(name: String): RPKFeatureFlag? {
        return plugin.core.database.getTable(RPKFeatureFlagTable::class).get(name)
    }

    override fun addFeatureFlag(featureFlag: RPKFeatureFlag) {
        val event = RPKBukkitFeatureFlagCreateEvent(featureFlag)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.core.database.getTable(RPKFeatureFlagTable::class).insert(event.featureFlag)
    }

    override fun removeFeatureFlag(featureFlag: RPKFeatureFlag) {
        val event = RPKBukkitFeatureFlagDeleteEvent(featureFlag)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.core.database.getTable(RPKFeatureFlagTable::class).delete(event.featureFlag)
    }

    override fun updateFeatureFlag(featureFlag: RPKFeatureFlag) {
        val event = RPKBukkitFeatureFlagUpdateEvent(featureFlag)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.core.database.getTable(RPKFeatureFlagTable::class).update(event.featureFlag)
    }

    override fun setFeatureFlag(profile: RPKProfile, featureFlag: RPKFeatureFlag, enabled: Boolean) {
        val event = RPKBukkitFeatureFlagSetEvent(profile, featureFlag, enabled)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        val profileFeatureFlagTable = plugin.core.database.getTable(RPKProfileFeatureFlagTable::class)
        var profileFeatureFlag = profileFeatureFlagTable.get(event.profile, event.featureFlag)
        if (profileFeatureFlag == null) {
            profileFeatureFlag = RPKProfileFeatureFlag(
                    profile = event.profile,
                    featureFlag = event.featureFlag,
                    enabled = event.enabled
            )
            profileFeatureFlagTable.insert(profileFeatureFlag)
        } else {
            profileFeatureFlag.enabled = event.enabled
            profileFeatureFlagTable.update(profileFeatureFlag)
        }
    }

}