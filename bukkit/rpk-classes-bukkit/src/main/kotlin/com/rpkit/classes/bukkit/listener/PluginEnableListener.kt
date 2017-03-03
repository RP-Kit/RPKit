package com.rpkit.classes.bukkit.listener

import com.rpkit.classes.bukkit.RPKClassesBukkit
import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginEnableEvent


class PluginEnableListener(private val plugin: RPKClassesBukkit): Listener {

    @EventHandler
    fun onPluginEnable(event: PluginEnableEvent) {
        if (plugin is RPKBukkitPlugin) {
            plugin.attemptStatRegistration()
            plugin.attemptCharacterCardFieldRegistration()
        }
    }

}