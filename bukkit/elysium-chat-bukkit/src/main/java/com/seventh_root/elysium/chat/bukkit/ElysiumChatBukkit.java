package com.seventh_root.elysium.chat.bukkit;

import com.seventh_root.elysium.api.chat.ChatChannelProvider;
import com.seventh_root.elysium.chat.bukkit.chatchannel.BukkitChatChannelProvider;
import com.seventh_root.elysium.chat.bukkit.command.chatchannel.ChatChannelCommand;
import com.seventh_root.elysium.chat.bukkit.database.table.BukkitChatChannelTable;
import com.seventh_root.elysium.chat.bukkit.listener.AsyncPlayerChatListener;
import com.seventh_root.elysium.chat.bukkit.listener.PlayerJoinListener;
import com.seventh_root.elysium.core.bukkit.plugin.ElysiumBukkitPlugin;
import com.seventh_root.elysium.core.database.Database;
import com.seventh_root.elysium.core.service.ServiceProvider;

import java.sql.SQLException;

public class ElysiumChatBukkit extends ElysiumBukkitPlugin {

    private ChatChannelProvider chatChannelProvider;
    private ServiceProvider[] serviceProviders;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        chatChannelProvider = new BukkitChatChannelProvider(this);
        serviceProviders = new ServiceProvider[] {
                chatChannelProvider
        };
    }

    @Override
    public void registerCommands() {
        getCommand("chatchannel").setExecutor(new ChatChannelCommand(this));
    }

    @Override
    public void registerListeners() {
        registerListeners(
                new AsyncPlayerChatListener(this),
                new PlayerJoinListener(this)
        );
    }

    @Override
    public ServiceProvider[] getServiceProviders() {
        return serviceProviders;
    }

    @Override
    public void createTables(Database database) throws SQLException {
        database.addTable(new BukkitChatChannelTable(this, database));
    }
}
