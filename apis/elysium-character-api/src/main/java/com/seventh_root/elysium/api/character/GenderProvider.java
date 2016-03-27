package com.seventh_root.elysium.api.character;

import com.seventh_root.elysium.core.service.ServiceProvider;

import java.util.Collection;

public interface GenderProvider<T extends Gender> extends ServiceProvider {

    T getGender(int id);

    T getGender(String name);

    Collection<? extends T> getGenders();

    void addGender(T gender);

    void removeGender(T gender);

}
