package com.rpkit.dailyquote.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import org.bstats.bukkit.Metrics


class RPKDailyQuoteLibBukkit : RPKBukkitPlugin() {
    override fun onEnable() {
        Metrics(this)
    }
}