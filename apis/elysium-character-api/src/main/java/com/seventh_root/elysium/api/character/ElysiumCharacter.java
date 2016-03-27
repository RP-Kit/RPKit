package com.seventh_root.elysium.api.character;

import com.seventh_root.elysium.api.player.ElysiumPlayer;
import com.seventh_root.elysium.core.database.TableRow;

public interface ElysiumCharacter extends TableRow {

    ElysiumPlayer getPlayer();

    void setPlayer(ElysiumPlayer player);

    String getName();

    void setName(String name);

    Gender getGender();

    void setGender(Gender gender);

    int getAge();

    void setAge(int age);

    Race getRace();

    void setRace(Race race);

    String getDescription();

    void setDescription(String description);

    boolean isDead();

    void setDead(boolean dead);

    double getHealth();

    void setHealth(double health);

    double getMaxHealth();

    void setMaxHealth(double maxHealth);

    int getMana();

    void setMana(int mana);

    int getMaxMana();

    void setMaxMana(int maxMana);

    int getFoodLevel();

    void setFoodLevel(int foodLevel);

    int getThirstLevel();

    void setThirstLevel(int thirstLevel);

}
