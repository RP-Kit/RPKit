package com.rpkit.rolling.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin


class RPKRollingBukkit: RPKBukkitPlugin() {

    override fun onEnable() {
        saveDefaultConfig()
    }

    override fun registerCommands() {
        getCommand("roll")?.setExecutor(RollCommand(this))
    }

    override fun setDefaultMessages() {
        messages.setDefault("roll", "&f\$character/\$player rolled &7\$roll &ffrom &7\$dice")
        messages.setDefault("roll-invalid-parse", "&cFailed to parse your roll.")
        messages.setDefault("roll-usage", "&cUsage: /roll [roll]")
        messages.setDefault("not-from-console", "&cYou must be a player to perform that command.")
        messages.setDefault("no-character", "&cYou must have a character to perform that command.")
    }

}
