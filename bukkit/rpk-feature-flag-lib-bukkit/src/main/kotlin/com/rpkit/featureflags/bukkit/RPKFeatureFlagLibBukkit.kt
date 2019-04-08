package com.rpkit.featureflags.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import org.bstats.bukkit.Metrics


class RPKFeatureFlagLibBukkit: RPKBukkitPlugin() {

    override fun onEnable() {
        Metrics(this)
    }

}