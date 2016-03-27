package com.seventh_root.elysium.api.player;

import com.seventh_root.elysium.core.service.ServiceProvider;

public interface PlayerProvider<T extends ElysiumPlayer> extends ServiceProvider {

    T getPlayer(int id);

    void addPlayer(T player);

    void removePlayer(T player);

}
