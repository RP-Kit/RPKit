package com.seventh_root.elysium.core.bukkit.plugin;

import com.seventh_root.elysium.core.ElysiumCore;
import com.seventh_root.elysium.core.database.Database;
import com.seventh_root.elysium.core.service.ServiceProvider;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public abstract class ElysiumBukkitPlugin extends JavaPlugin {

    private ElysiumCore core;

    public void setCore(ElysiumCore core) {
        if (this.core != null) {
            getLogger().warning("There was an attempt to redefine the ElysiumCore instance.");
            return;
        }
        this.core = core;
    }

    public ElysiumCore getCore() {
        return core;
    }

    public void registerCommands() {

    }

    public void registerListeners() {

    }

    public void registerListeners(Listener... listeners) {
        for (Listener listener : listeners) {
            getServer().getPluginManager().registerEvents(listener, this);
        }
    }

    public void createTables(Database database) throws SQLException {}

    public abstract ServiceProvider[] getServiceProviders();

}
