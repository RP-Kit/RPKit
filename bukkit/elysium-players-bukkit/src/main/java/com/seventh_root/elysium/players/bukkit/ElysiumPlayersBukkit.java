package com.seventh_root.elysium.players.bukkit;

import com.seventh_root.elysium.api.player.PlayerProvider;
import com.seventh_root.elysium.core.bukkit.plugin.ElysiumBukkitPlugin;
import com.seventh_root.elysium.core.database.Database;
import com.seventh_root.elysium.core.service.ServiceProvider;
import com.seventh_root.elysium.players.bukkit.database.table.BukkitPlayerTable;

import java.sql.SQLException;

public class ElysiumPlayersBukkit extends ElysiumBukkitPlugin {

    private PlayerProvider playerProvider;
    private ServiceProvider[] providers;

    @Override
    public void onEnable() {
        playerProvider = new BukkitPlayerProvider(this);
        providers = new ServiceProvider[] {
                playerProvider
        };
    }

    @Override
    public void createTables(Database database) throws SQLException {
        database.addTable(new BukkitPlayerTable(database));
    }

    @Override
    public ServiceProvider[] getServiceProviders() {
        return providers;
    }

}
