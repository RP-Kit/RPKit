package com.rpkit.statbuilds.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import org.bstats.bukkit.Metrics

class RPKStatBuildLibBukkit: RPKBukkitPlugin() {

    override fun onEnable() {
        Metrics(this)
    }

}