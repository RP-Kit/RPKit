package com.seventh_root.elysium.characters.bukkit.character;

import com.seventh_root.elysium.api.character.ElysiumCharacter;
import com.seventh_root.elysium.api.character.Gender;
import com.seventh_root.elysium.api.character.Race;
import com.seventh_root.elysium.api.player.ElysiumPlayer;
import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit;
import com.seventh_root.elysium.characters.bukkit.gender.BukkitGenderProvider;
import com.seventh_root.elysium.characters.bukkit.race.BukkitRaceProvider;
import com.seventh_root.elysium.players.bukkit.BukkitPlayer;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class BukkitCharacter implements ElysiumCharacter {

    public static class Builder {

        private ElysiumCharactersBukkit plugin;

        private int id;
        private ElysiumPlayer player;
        private String name;
        private Gender gender;
        private int age;
        private Race race;
        private String description;
        private boolean dead;
        private Location location;
        private List<ItemStack> inventoryContents;
        private ItemStack helmet;
        private ItemStack chestplate;
        private ItemStack leggings;
        private ItemStack boots;
        private double health;
        private double maxHealth;
        private int mana;
        private int maxMana;
        private int foodLevel;
        private int thirstLevel;

        @SuppressWarnings("unchecked")
        public Builder(ElysiumCharactersBukkit plugin) {
            this.plugin = plugin;
            id = 0;
            player = null;
            name = plugin.getConfig().getString("characters.defaults.name");
            BukkitGenderProvider genderProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitGenderProvider.class);
            if (plugin.getConfig().get("characters.defaults.gender") == null) {
                gender = null;
            } else {
                gender = genderProvider.getGender(plugin.getConfig().getString("characters.defaults.gender"));
            }
            age = plugin.getConfig().getInt("characters.defaults.age");
            BukkitRaceProvider raceProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitRaceProvider.class);
            if (plugin.getConfig().get("characters.defaults.race") == null) {
                race = null;
            } else {
                race = raceProvider.getRace(plugin.getConfig().getString("characters.defaults.race"));
            }
            description = plugin.getConfig().getString("characters.defaults.description");
            dead = plugin.getConfig().getBoolean("characters.defaults.dead");
            location = Bukkit.getWorlds().get(0).getSpawnLocation();
            inventoryContents = (List<ItemStack>) plugin.getConfig().<ItemStack>getList("characters.defaults.inventory-contents");
            helmet = plugin.getConfig().getItemStack("characters.defaults.helmet");
            chestplate = plugin.getConfig().getItemStack("characters.defaults.chestplate");
            leggings = plugin.getConfig().getItemStack("characters.defaults.leggings");
            boots = plugin.getConfig().getItemStack("characters.defaults.boots");
            health = plugin.getConfig().getInt("characters.defaults.health");
            maxHealth = plugin.getConfig().getInt("characters.defaults.max-health");
            mana = plugin.getConfig().getInt("characters.defaults.mana");
            maxMana = plugin.getConfig().getInt("characters.defaults.max-mana");
            foodLevel = plugin.getConfig().getInt("characters.defaults.food-level");
            thirstLevel = plugin.getConfig().getInt("characters.defaults.thirst-level");
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder player(ElysiumPlayer player) {
            if (player instanceof BukkitPlayer) {
                this.player = player;
                if (name.equals(""))
                    name = player.getName() + "'s character";
            }
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder gender(Gender gender) {
            this.gender = gender;
            return this;
        }

        public Builder age(int age) {
            this.age = age;
            return this;
        }

        public Builder race(Race race) {
            this.race = race;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder dead(boolean dead) {
            this.dead = dead;
            return this;
        }

        public Builder location(Location location) {
            this.location =  location;
            return this;
        }

        public Builder inventoryContents(ItemStack[] inventoryContents) {
            Validate.isTrue(inventoryContents.length == 36);
            this.inventoryContents = Arrays.asList(inventoryContents);
            return this;
        }

        public Builder inventoryItem(ItemStack item) {
            inventoryContents.add(item);
            return this;
        }

        public Builder helmet(ItemStack helmet) {
            this.helmet = helmet;
            return this;
        }

        public Builder chestplate(ItemStack chestplate) {
            this.chestplate = chestplate;
            return this;
        }

        public Builder leggings(ItemStack leggings) {
            this.leggings = leggings;
            return this;
        }

        public Builder boots(ItemStack boots) {
            this.boots = boots;
            return this;
        }

        public Builder health(double health) {
            this.health = health;
            return this;
        }

        public Builder maxHealth(double maxHealth) {
            this.maxHealth = maxHealth;
            return this;
        }

        public Builder mana(int mana) {
            this.mana = mana;
            return this;
        }

        public Builder maxMana(int maxMana) {
            this.maxMana = maxMana;
            return this;
        }

        public Builder foodLevel(int foodLevel) {
            this.foodLevel = foodLevel;
            return this;
        }

        public Builder thirstLevel(int thirstLevel) {
            this.thirstLevel = thirstLevel;
            return this;
        }

        public BukkitCharacter build() {
            BukkitCharacter character = new BukkitCharacter(plugin);
            character.setId(id);
            character.setPlayer(player);
            character.setName(name);
            character.setGender(gender);
            character.setAge(age);
            character.setRace(race);
            character.setDescription(description);
            character.setDead(dead);
            character.setLocation(location);
            character.setInventoryContents(inventoryContents.toArray(new ItemStack[36]));
            character.setHelmet(helmet);
            character.setChestplate(chestplate);
            character.setLeggings(leggings);
            character.setBoots(boots);
            character.setHealth(health);
            character.setMaxHealth(maxHealth);
            character.setMana(mana);
            character.setMaxMana(maxMana);
            character.setFoodLevel(foodLevel);
            character.setThirstLevel(thirstLevel);
            return character;
        }

    }

    private ElysiumCharactersBukkit plugin;

    private int id;
    private ElysiumPlayer player;
    private String name;
    private Gender gender;
    private int age;
    private Race race;
    private String description;
    private boolean dead;
    private Location location;
    private ItemStack[] inventoryContents;
    private ItemStack helmet;
    private ItemStack chestplate;
    private ItemStack leggings;
    private ItemStack boots;
    private double health;
    private double maxHealth;
    private int mana;
    private int maxMana;
    private int foodLevel;
    private int thirstLevel;
    
    private BukkitCharacter(ElysiumCharactersBukkit plugin) {
        this.plugin = plugin;
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
    public ElysiumPlayer getPlayer() {
        return player;
    }

    @Override
    public void setPlayer(ElysiumPlayer player) {
        Validate.isTrue(player instanceof BukkitPlayer);
        this.player = player;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Gender getGender() {
        return gender;
    }

    @Override
    public void setGender(Gender gender) {
        this.gender = gender;
    }

    @Override
    public int getAge() {
        return age;
    }

    @Override
    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public Race getRace() {
        return race;
    }

    @Override
    public void setRace(Race race) {
        this.race = race;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
        if (this.description.length() > 1024) {
            this.description = this.description.substring(0, 1021) + "...";
        }
    }

    @Override
    public boolean isDead() {
        return dead;
    }

    @Override
    public void setDead(boolean dead) {
        this.dead = dead;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public ItemStack[] getInventoryContents() {
        return inventoryContents;
    }

    public void setInventoryContents(ItemStack[] inventoryContents) {
        this.inventoryContents = inventoryContents;
    }

    public ItemStack getHelmet() {
        return helmet;
    }

    public void setHelmet(ItemStack helmet) {
        this.helmet = helmet;
    }

    public ItemStack getChestplate() {
        return chestplate;
    }

    public void setChestplate(ItemStack chestplate) {
        this.chestplate = chestplate;
    }

    public ItemStack getLeggings() {
        return leggings;
    }

    public void setLeggings(ItemStack leggings) {
        this.leggings = leggings;
    }

    public ItemStack getBoots() {
        return boots;
    }

    public void setBoots(ItemStack boots) {
        this.boots = boots;
    }

    @Override
    public double getHealth() {
        return health;
    }

    @Override
    public void setHealth(double health) {
        this.health = health;
    }

    @Override
    public double getMaxHealth() {
        return maxHealth;
    }

    @Override
    public void setMaxHealth(double maxHealth) {
        this.maxHealth = maxHealth;
    }

    @Override
    public int getMana() {
        return mana;
    }

    @Override
    public void setMana(int mana) {
        this.mana = mana;
    }

    @Override
    public int getMaxMana() {
        return maxMana;
    }

    @Override
    public void setMaxMana(int maxMana) {
        this.maxMana = maxMana;
    }

    @Override
    public int getFoodLevel() {
        return foodLevel;
    }

    @Override
    public void setFoodLevel(int foodLevel) {
        this.foodLevel = foodLevel;
    }

    @Override
    public int getThirstLevel() {
        return thirstLevel;
    }

    @Override
    public void setThirstLevel(int thirstLevel) {
        this.thirstLevel = thirstLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BukkitCharacter character = (BukkitCharacter) o;

        if (id != character.id) return false;
        if (age != character.age) return false;
        if (dead != character.dead) return false;
        if (Double.compare(character.health, health) != 0) return false;
        if (Double.compare(character.maxHealth, maxHealth) != 0) return false;
        if (mana != character.mana) return false;
        if (maxMana != character.maxMana) return false;
        if (foodLevel != character.foodLevel) return false;
        if (thirstLevel != character.thirstLevel) return false;
        if (player != null ? !player.equals(character.player) : character.player != null) return false;
        if (name != null ? !name.equals(character.name) : character.name != null) return false;
        if (gender != null ? !gender.equals(character.gender) : character.gender != null) return false;
        if (race != null ? !race.equals(character.race) : character.race != null) return false;
        if (description != null ? !description.equals(character.description) : character.description != null)
            return false;
        if (location != null ? !location.equals(character.location) : character.location != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(inventoryContents, character.inventoryContents)) return false;
        if (helmet != null ? !helmet.equals(character.helmet) : character.helmet != null) return false;
        if (chestplate != null ? !chestplate.equals(character.chestplate) : character.chestplate != null) return false;
        if (leggings != null ? !leggings.equals(character.leggings) : character.leggings != null) return false;
        return boots != null ? boots.equals(character.boots) : character.boots == null;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = id;
        result = 31 * result + (player != null ? player.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (gender != null ? gender.hashCode() : 0);
        result = 31 * result + age;
        result = 31 * result + (race != null ? race.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (dead ? 1 : 0);
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(inventoryContents);
        result = 31 * result + (helmet != null ? helmet.hashCode() : 0);
        result = 31 * result + (chestplate != null ? chestplate.hashCode() : 0);
        result = 31 * result + (leggings != null ? leggings.hashCode() : 0);
        result = 31 * result + (boots != null ? boots.hashCode() : 0);
        temp = Double.doubleToLongBits(health);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(maxHealth);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + mana;
        result = 31 * result + maxMana;
        result = 31 * result + foodLevel;
        result = 31 * result + thirstLevel;
        return result;
    }

}
