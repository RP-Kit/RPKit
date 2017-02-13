package com.rpkit.rolling.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin


class RPKRollingBukkit: RPKBukkitPlugin() {

    override fun onEnable() {
        saveDefaultConfig()
    }

    override fun registerCommands() {
        getCommand("roll").executor = RollCommand(this)
    }

}
