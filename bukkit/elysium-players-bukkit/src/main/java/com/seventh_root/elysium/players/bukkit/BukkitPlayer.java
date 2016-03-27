package com.seventh_root.elysium.players.bukkit;

import com.seventh_root.elysium.api.player.ElysiumPlayer;
import com.seventh_root.elysium.core.database.TableRow;
import org.bukkit.OfflinePlayer;

public class BukkitPlayer implements TableRow, ElysiumPlayer {

    private int id;
    private OfflinePlayer bukkitPlayer;

    public BukkitPlayer(int id, OfflinePlayer bukkitPlayer) {
        this.id = id;
        this.bukkitPlayer = bukkitPlayer;
    }

    public BukkitPlayer(OfflinePlayer bukkitPlayer) {
        this(0, bukkitPlayer);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public OfflinePlayer getBukkitPlayer() {
        return bukkitPlayer;
    }

    public void setBukkitPlayer(OfflinePlayer bukkitPlayer) {
        this.bukkitPlayer = bukkitPlayer;
    }

    @Override
    public String getName() {
        return bukkitPlayer.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BukkitPlayer that = (BukkitPlayer) o;
        return getId() == that.getId();
    }

}
