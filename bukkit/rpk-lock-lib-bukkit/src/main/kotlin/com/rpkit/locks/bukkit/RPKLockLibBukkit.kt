package com.rpkit.locks.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import org.bstats.bukkit.Metrics


class RPKLockLibBukkit: RPKBukkitPlugin() {
    override fun onEnable() {
        Metrics(this)
    }
}