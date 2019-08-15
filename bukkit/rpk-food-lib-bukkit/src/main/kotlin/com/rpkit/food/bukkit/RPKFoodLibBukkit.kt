package com.rpkit.food.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import org.bstats.bukkit.Metrics


class RPKFoodLibBukkit : RPKBukkitPlugin() {
    override fun onEnable() {
        Metrics(this)
    }
}