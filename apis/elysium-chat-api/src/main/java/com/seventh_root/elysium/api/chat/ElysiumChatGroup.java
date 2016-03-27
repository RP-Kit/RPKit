package com.seventh_root.elysium.api.chat;

import com.seventh_root.elysium.api.player.ElysiumPlayer;
import com.seventh_root.elysium.core.database.TableRow;

import java.util.Collection;

public interface ElysiumChatGroup extends TableRow {

    String getName();

    void setName(String name);

    Collection<? extends ElysiumPlayer> getPlayers();

    void addPlayer(ElysiumPlayer player);

    void removePlayer(ElysiumPlayer player);

    Collection<? extends ElysiumPlayer> getInvited();

    void invite(ElysiumPlayer player);

    void uninvite(ElysiumPlayer player);

    void sendMessage(ElysiumPlayer sender, String message);

}
