package com.seventh_root.elysium.characters.bukkit.race;

import com.seventh_root.elysium.api.character.Race;

public class BukkitRace implements Race {

    private int id;
    private final String name;

    public BukkitRace(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public BukkitRace(String name) {
        this.name = name;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

}
