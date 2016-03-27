package com.seventh_root.elysium.characters.bukkit.gender;

import com.seventh_root.elysium.api.character.Gender;

public class BukkitGender implements Gender {

    private int id;
    private final String name;

    public BukkitGender(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public BukkitGender(String name) {
        this(0, name);
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
