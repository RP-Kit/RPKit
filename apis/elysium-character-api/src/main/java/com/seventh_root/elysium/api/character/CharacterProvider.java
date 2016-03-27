package com.seventh_root.elysium.api.character;

import com.seventh_root.elysium.api.player.ElysiumPlayer;
import com.seventh_root.elysium.core.service.ServiceProvider;

import java.util.Collection;

public interface CharacterProvider<T extends ElysiumCharacter> extends ServiceProvider {

    T getCharacter(int id);

    T getActiveCharacter(ElysiumPlayer player);

    void setActiveCharacter(ElysiumPlayer player, T character);

    Collection<? extends T> getCharacters(ElysiumPlayer player);

    int addCharacter(T character);

    void removeCharacter(T character);

    void updateCharacter(T character);

}
