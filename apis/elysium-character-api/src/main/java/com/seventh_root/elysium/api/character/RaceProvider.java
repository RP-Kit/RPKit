package com.seventh_root.elysium.api.character;

import com.seventh_root.elysium.core.service.ServiceProvider;

import java.util.Collection;

public interface RaceProvider<T extends Race> extends ServiceProvider {

    T getRace(int id);

    T getRace(String name);

    Collection<? extends T> getRaces();

    void addRace(T race);

    void removeRace(T race);

}
