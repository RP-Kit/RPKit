package com.seventh_root.elysium.core.bukkit.listener;

import com.seventh_root.elysium.core.bukkit.ElysiumCoreBukkit;
import com.seventh_root.elysium.core.bukkit.plugin.ElysiumBukkitPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;

public class PluginEnableListener implements Listener {

    private final ElysiumCoreBukkit plugin;

    public PluginEnableListener(ElysiumCoreBukkit plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        if (event.getPlugin() != plugin) {
            if (event.getPlugin() instanceof ElysiumBukkitPlugin) {
                ElysiumBukkitPlugin elysiumBukkitPlugin = (ElysiumBukkitPlugin) event.getPlugin();
                plugin.initializePlugin(elysiumBukkitPlugin);
            }
        }
    }

}
