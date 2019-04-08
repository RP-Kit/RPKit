package com.rpkit.experience.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import org.bstats.bukkit.Metrics

class RPKExperienceLibBukkit: RPKBukkitPlugin() {
    override fun onEnable() {
        Metrics(this)
    }
}
