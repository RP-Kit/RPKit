package com.seventh_root.elysium.core.bukkit;

import com.seventh_root.elysium.core.ElysiumCore;
import com.seventh_root.elysium.core.bukkit.listener.PluginEnableListener;
import com.seventh_root.elysium.core.bukkit.plugin.ElysiumBukkitPlugin;
import com.seventh_root.elysium.core.database.Database;
import com.seventh_root.elysium.core.service.ServiceProvider;

import java.sql.SQLException;

public class ElysiumCoreBukkit extends ElysiumBukkitPlugin {

    private ElysiumCore core;
    private ServiceProvider[] providers;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        core = new ElysiumCore(getLogger(), new Database(getConfig().getString("database.url"), getConfig().getString("database.username"), getConfig().getString("database.password")));
        setCore(core);
        try {
            createTables(getCore().getDatabase());
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        providers = new ServiceProvider[] {};
        registerServiceProviders(this);
        registerCommands();
        registerListeners();
    }

    @Override
    public void registerListeners() {
        registerListeners(new PluginEnableListener(this));
    }

    @Override
    public ServiceProvider[] getServiceProviders() {
        return providers;
    }

    public void registerServiceProviders(ElysiumBukkitPlugin plugin) {
        for (ServiceProvider provider : plugin.getServiceProviders()) {
            core.getServiceManager().registerServiceProvider(provider);
        }
    }

    public void initializePlugin(ElysiumBukkitPlugin elysiumBukkitPlugin) {
        elysiumBukkitPlugin.setCore(core);
        try {
            elysiumBukkitPlugin.createTables(getCore().getDatabase());
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        registerServiceProviders(elysiumBukkitPlugin);
        elysiumBukkitPlugin.registerCommands();
        elysiumBukkitPlugin.registerListeners();
    }

}
