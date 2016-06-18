package com.seventh_root.elysium.core.bukkit.listener

import com.seventh_root.elysium.core.bukkit.ElysiumCoreBukkit
import com.seventh_root.elysium.core.bukkit.plugin.ElysiumBukkitPlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginEnableEvent

class PluginEnableListener(private val plugin: ElysiumCoreBukkit): Listener {

    @EventHandler
    fun onPluginEnable(event: PluginEnableEvent) {
        if (event.plugin !== plugin) {
            if (event.plugin is ElysiumBukkitPlugin) {
                val elysiumBukkitPlugin = event.plugin as ElysiumBukkitPlugin
                plugin.initializePlugin(elysiumBukkitPlugin)
            }
        }
    }

}
