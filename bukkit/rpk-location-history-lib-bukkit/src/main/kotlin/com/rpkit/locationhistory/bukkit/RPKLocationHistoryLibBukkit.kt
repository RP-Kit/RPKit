package com.rpkit.locationhistory.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import org.bstats.bukkit.Metrics


class RPKLocationHistoryLibBukkit: RPKBukkitPlugin() {
    override fun onEnable() {
        Metrics(this)
    }
}