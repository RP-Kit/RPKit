package com.rpkit.blocklog.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import org.bstats.bukkit.Metrics


class RPKBlockLogLibBukkit: RPKBukkitPlugin() {

    override fun onEnable() {
        Metrics(this)
    }

}