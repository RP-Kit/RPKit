package com.rpkit.drink.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import org.bstats.bukkit.Metrics


class RPKDrinkLibBukkit: RPKBukkitPlugin() {
    override fun onEnable() {
        Metrics(this)
    }
}