package com.rpkit.experience.bukkit.listener

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.experience.bukkit.RPKExperienceBukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginEnableEvent


class PluginEnableListener(private val plugin: RPKExperienceBukkit): Listener {

    @EventHandler
    fun onPluginEnable(event: PluginEnableEvent) {
        if (event.plugin is RPKBukkitPlugin) {
            plugin.attemptCharacterCardFieldInitialisation()
        }
    }

}