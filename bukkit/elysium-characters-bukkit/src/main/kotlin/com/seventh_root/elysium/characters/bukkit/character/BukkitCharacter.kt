package com.seventh_root.elysium.characters.bukkit.character

import com.seventh_root.elysium.api.character.ElysiumCharacter
import com.seventh_root.elysium.api.character.Gender
import com.seventh_root.elysium.api.character.Race
import com.seventh_root.elysium.api.player.ElysiumPlayer
import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit
import com.seventh_root.elysium.characters.bukkit.gender.BukkitGenderProvider
import com.seventh_root.elysium.characters.bukkit.race.BukkitRaceProvider
import com.seventh_root.elysium.players.bukkit.BukkitPlayer
import org.apache.commons.lang.Validate
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import java.util.*

class BukkitCharacter private constructor(id: Int, player: ElysiumPlayer, name: String, gender: Gender, age: Int, race: Race, description: String, dead: Boolean, location: Location, inventoryContents: Array<ItemStack>, offhand: ItemStack?, helmet: ItemStack?, chestplate: ItemStack?, leggings: ItemStack?, boots: ItemStack?, health: Double, maxHealth: Double, mana: Int, maxMana: Int, foodLevel: Int, thirstLevel: Int) : ElysiumCharacter {

    class Builder
    @Suppress("UNCHECKED_CAST")
    constructor(private val plugin: ElysiumCharactersBukkit) {

        private var id: Int
        private var player: ElysiumPlayer?
        private var name: String
        private var gender: Gender?
        private var age: Int
        private var race: Race?
        private var description: String
        private var dead: Boolean
        private var location: Location
        private var inventoryContents: MutableList<ItemStack>
        private var offhand: ItemStack? = null
        private var helmet: ItemStack? = null
        private var chestplate: ItemStack? = null
        private var leggings: ItemStack? = null
        private var boots: ItemStack? = null
        private var health: Double
        private var maxHealth: Double
        private var mana: Int
        private var maxMana: Int
        private var foodLevel: Int
        private var thirstLevel: Int

        init {
            id = 0
            player = null
            name = plugin.config.getString("characters.defaults.name")
            val genderProvider = plugin.core!!.serviceManager.getServiceProvider(BukkitGenderProvider::class.java)
            if (plugin.config.get("characters.defaults.gender") == null) {
                gender = null
            } else {
                gender = genderProvider.getGender(plugin.config.getString("characters.defaults.gender"))
            }
            age = plugin.config.getInt("characters.defaults.age")
            val raceProvider = plugin.core!!.serviceManager.getServiceProvider(BukkitRaceProvider::class.java)
            if (plugin.config.get("characters.defaults.race") == null) {
                race = null
            } else {
                race = raceProvider.getRace(plugin.config.getString("characters.defaults.race"))
            }
            description = plugin.config.getString("characters.defaults.description")
            dead = plugin.config.getBoolean("characters.defaults.dead")
            location = Bukkit.getWorlds()[0].spawnLocation
            inventoryContents = plugin.config.getList("characters.defaults.inventory-contents") as ArrayList<ItemStack>
            offhand = plugin.config.getItemStack("characters.defaults.offhand")
            helmet = plugin.config.getItemStack("characters.defaults.helmet")
            chestplate = plugin.config.getItemStack("characters.defaults.chestplate")
            leggings = plugin.config.getItemStack("characters.defaults.leggings")
            boots = plugin.config.getItemStack("characters.defaults.boots")
            health = plugin.config.getInt("characters.defaults.health").toDouble()
            maxHealth = plugin.config.getInt("characters.defaults.max-health").toDouble()
            mana = plugin.config.getInt("characters.defaults.mana")
            maxMana = plugin.config.getInt("characters.defaults.max-mana")
            foodLevel = plugin.config.getInt("characters.defaults.food-level")
            thirstLevel = plugin.config.getInt("characters.defaults.thirst-level")
        }

        fun id(id: Int): Builder {
            this.id = id
            return this
        }

        fun player(player: ElysiumPlayer): Builder {
            if (player is BukkitPlayer) {
                this.player = player
                if (name == "")
                    name = player.name + "'s character"
            }
            return this
        }

        fun name(name: String): Builder {
            this.name = name
            return this
        }

        fun gender(gender: Gender): Builder {
            this.gender = gender
            return this
        }

        fun age(age: Int): Builder {
            this.age = age
            return this
        }

        fun race(race: Race): Builder {
            this.race = race
            return this
        }

        fun description(description: String): Builder {
            this.description = description
            return this
        }

        fun dead(dead: Boolean): Builder {
            this.dead = dead
            return this
        }

        fun location(location: Location): Builder {
            this.location = location
            return this
        }

        fun inventoryContents(inventoryContents: Array<ItemStack>?): Builder {
            Validate.notNull(inventoryContents)
            Validate.isTrue(inventoryContents!!.size == 36)
            this.inventoryContents = Arrays.asList(*inventoryContents)
            return this
        }

        fun inventoryItem(item: ItemStack): Builder {
            inventoryContents.add(item)
            return this
        }

        fun offhand(offhand: ItemStack?): Builder {
            this.offhand = offhand
            return this
        }

        fun helmet(helmet: ItemStack?): Builder {
            this.helmet = helmet
            return this
        }

        fun chestplate(chestplate: ItemStack?): Builder {
            this.chestplate = chestplate
            return this
        }

        fun leggings(leggings: ItemStack?): Builder {
            this.leggings = leggings
            return this
        }

        fun boots(boots: ItemStack?): Builder {
            this.boots = boots
            return this
        }

        fun health(health: Double): Builder {
            this.health = health
            return this
        }

        fun maxHealth(maxHealth: Double): Builder {
            this.maxHealth = maxHealth
            return this
        }

        fun mana(mana: Int): Builder {
            this.mana = mana
            return this
        }

        fun maxMana(maxMana: Int): Builder {
            this.maxMana = maxMana
            return this
        }

        fun foodLevel(foodLevel: Int): Builder {
            this.foodLevel = foodLevel
            return this
        }

        fun thirstLevel(thirstLevel: Int): Builder {
            this.thirstLevel = thirstLevel
            return this
        }

        fun build(): BukkitCharacter {
            val character = BukkitCharacter(
                    id,
                    player!!,
                    name,
                    gender!!,
                    age,
                    race!!,
                    description,
                    dead,
                    location,
                    inventoryContents.toTypedArray(),
                    offhand,
                    helmet,
                    chestplate,
                    leggings,
                    boots,
                    health,
                    maxHealth,
                    mana,
                    maxMana,
                    foodLevel,
                    thirstLevel
            )
            return character
        }

    }

    override var id: Int = id
    override var player: ElysiumPlayer = player
        set(player) {
            Validate.isTrue(player is BukkitPlayer)
            this.player = player
        }
    override var name: String = name
    override var gender: Gender = gender
    override var age: Int = age
    override var race: Race = race
    override var description: String = description
        set(description) {
            this.description = description
            if (this.description.length > 1024) {
                this.description = this.description.substring(0, 1021) + "..."
            }
        }
    override var isDead: Boolean = dead
    var location: Location = location
    var inventoryContents: Array<ItemStack> = inventoryContents
    var offhand: ItemStack? = offhand
    var helmet: ItemStack? = helmet
    var chestplate: ItemStack? = chestplate
    var leggings: ItemStack? = leggings
    var boots: ItemStack? = boots
    override var health: Double = health
    override var maxHealth: Double = maxHealth
    override var mana: Int = mana
    override var maxMana: Int = maxMana
    override var foodLevel: Int = foodLevel
    override var thirstLevel: Int = thirstLevel

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val character = other as BukkitCharacter?

        if (id != character!!.id) return false
        if (age != character.age) return false
        if (isDead != character.isDead) return false
        if (java.lang.Double.compare(character.health, health) != 0) return false
        if (java.lang.Double.compare(character.maxHealth, maxHealth) != 0) return false
        if (mana != character.mana) return false
        if (maxMana != character.maxMana) return false
        if (foodLevel != character.foodLevel) return false
        if (thirstLevel != character.thirstLevel) return false
        if (player != character.player) return false
        if (name != character.name) return false
        if (gender != character.gender) return false
        if (race != character.race) return false
        if (description != character.description) return false
        if (location != character.location) return false
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(inventoryContents, character.inventoryContents)) return false
        if (if (offhand != null) offhand != character.offhand else character.offhand != null) return false
        if (if (helmet != null) helmet != character.helmet else character.helmet != null) return false
        if (if (chestplate != null) chestplate != character.chestplate else character.chestplate != null) return false
        if (if (leggings != null) leggings != character.leggings else character.leggings != null) return false
        return if (boots != null) boots == character.boots else character.boots == null

    }

    override fun hashCode(): Int {
        var result: Int
        var temp: Long
        result = id
        result = 31 * result + player.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + gender.hashCode()
        result = 31 * result + age
        result = 31 * result + race.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + if (isDead) 1 else 0
        result = 31 * result + location.hashCode()
        result = 31 * result + Arrays.hashCode(inventoryContents)
        result = 31 * result + if (offhand != null) offhand!!.hashCode() else 0
        result = 31 * result + if (helmet != null) helmet!!.hashCode() else 0
        result = 31 * result + if (chestplate != null) chestplate!!.hashCode() else 0
        result = 31 * result + if (leggings != null) leggings!!.hashCode() else 0
        result = 31 * result + if (boots != null) boots!!.hashCode() else 0
        temp = java.lang.Double.doubleToLongBits(health)
        result = 31 * result + (temp xor temp.ushr(32)).toInt()
        temp = java.lang.Double.doubleToLongBits(maxHealth)
        result = 31 * result + (temp xor temp.ushr(32)).toInt()
        result = 31 * result + mana
        result = 31 * result + maxMana
        result = 31 * result + foodLevel
        result = 31 * result + thirstLevel
        return result
    }

}
