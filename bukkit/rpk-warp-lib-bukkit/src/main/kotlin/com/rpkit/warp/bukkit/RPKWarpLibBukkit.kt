package com.rpkit.warp.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import org.bstats.bukkit.Metrics


class RPKWarpLibBukkit: RPKBukkitPlugin() {
    override fun onEnable() {
        Metrics(this)
    }
}