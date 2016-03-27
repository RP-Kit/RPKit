package com.seventh_root.elysium.players.bukkit;

import com.seventh_root.elysium.api.player.PlayerProvider;
import com.seventh_root.elysium.core.database.Table;
import com.seventh_root.elysium.players.bukkit.database.table.BukkitPlayerTable;
import org.bukkit.OfflinePlayer;

public class BukkitPlayerProvider implements PlayerProvider<BukkitPlayer> {

    private final ElysiumPlayersBukkit plugin;

    public BukkitPlayerProvider(ElysiumPlayersBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public BukkitPlayer getPlayer(int id) {
        return plugin.getCore().getDatabase().getTable(BukkitPlayer.class).get(id);
    }

    public BukkitPlayer getPlayer(OfflinePlayer bukkitPlayer) {
        Table<BukkitPlayer> table = plugin.getCore().getDatabase().getTable(BukkitPlayer.class);
        if (table instanceof BukkitPlayerTable) {
            BukkitPlayerTable bukkitPlayerTable = (BukkitPlayerTable) table;
            BukkitPlayer player = bukkitPlayerTable.get(bukkitPlayer);
            if (player == null) {
                player = new BukkitPlayer(bukkitPlayer);
                addPlayer(player);
            }
            return player;
        }
        return null;
    }

    @Override
    public void addPlayer(BukkitPlayer player) {
        plugin.getCore().getDatabase().getTable(BukkitPlayer.class).insert(player);
    }

    @Override
    public void removePlayer(BukkitPlayer player) {
        plugin.getCore().getDatabase().getTable(BukkitPlayer.class).delete(player);
    }

}
