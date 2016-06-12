package com.seventh_root.elysium.characters.bukkit.character

import com.seventh_root.elysium.api.character.CharacterProvider
import com.seventh_root.elysium.api.player.ElysiumPlayer
import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit
import com.seventh_root.elysium.core.database.use
import com.seventh_root.elysium.players.bukkit.BukkitPlayer
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import java.sql.SQLException
import java.util.*

class BukkitCharacterProvider : CharacterProvider<BukkitCharacter> {

    private val plugin: ElysiumCharactersBukkit
    private val activeCharacterCacheManager: CacheManager
    private val activeCharacterCache: Cache<Int, Int>

    constructor(plugin: ElysiumCharactersBukkit) {
        this.plugin = plugin
        this.activeCharacterCacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
        activeCharacterCache = activeCharacterCacheManager.createCache("cache", CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, Int::class.javaObjectType).build())
    }

    override fun getCharacter(id: Int): BukkitCharacter? {
        return plugin.core!!.database.getTable(BukkitCharacter::class.java)!![id]
    }

    override fun getActiveCharacter(player: ElysiumPlayer): BukkitCharacter? {
        val playerId = player.id
        if (activeCharacterCache.containsKey(playerId)) {
            return getCharacter(activeCharacterCache.get(playerId) as Int)
        } else {
            try {
                var character: BukkitCharacter? = null
                plugin.core!!.database.createConnection().use { connection ->
                    connection.prepareStatement(
                            "SELECT character_id FROM player_character WHERE player_id = ?").use({ statement ->
                        statement.setInt(1, player.id)
                        val resultSet = statement.executeQuery()
                        if (resultSet.next()) {
                            val characterId = resultSet.getInt("character_id")
                            character = getCharacter(characterId)
                            activeCharacterCache.put(playerId, characterId)
                        }
                    })
                }
                return character
            } catch (exception: SQLException) {
                exception.printStackTrace()
            }
        }
        return null
    }

    override fun setActiveCharacter(player: ElysiumPlayer, character: BukkitCharacter?) {
        val oldCharacter = getActiveCharacter(player)
        if (oldCharacter != null) {
            if (player is BukkitPlayer) {
                val offlineBukkitPlayer = player.bukkitPlayer
                if (offlineBukkitPlayer.isOnline) {
                    val bukkitPlayer = offlineBukkitPlayer.player
                    oldCharacter.inventoryContents = bukkitPlayer.inventory.contents
                    oldCharacter.helmet = bukkitPlayer.inventory.helmet
                    oldCharacter.chestplate = bukkitPlayer.inventory.chestplate
                    oldCharacter.leggings = bukkitPlayer.inventory.leggings
                    oldCharacter.boots = bukkitPlayer.inventory.boots
                    oldCharacter.location = bukkitPlayer.location
                    oldCharacter.health = bukkitPlayer.health
                    oldCharacter.foodLevel = bukkitPlayer.foodLevel
                    updateCharacter(oldCharacter)
                }
            }
        }
        if (character != null) {
            try {
                plugin.core!!.database.createConnection().use { connection ->
                    connection.prepareStatement(
                            "INSERT INTO player_character(player_id, character_id) VALUES(?, ?) ON DUPLICATE KEY UPDATE character_id = VALUES(character_id)").use({ statement ->
                        statement.setInt(1, player.id)
                        statement.setInt(2, character.id)
                        statement.executeUpdate()
                    })
                }
            } catch (exception: SQLException) {
                exception.printStackTrace()
            }

            if (player is BukkitPlayer) {
                val offlineBukkitPlayer = player.bukkitPlayer
                if (offlineBukkitPlayer.isOnline) {
                    val bukkitPlayer = offlineBukkitPlayer.player
                    bukkitPlayer.inventory.contents = character.inventoryContents
                    bukkitPlayer.inventory.helmet = character.helmet
                    bukkitPlayer.inventory.chestplate = character.chestplate
                    bukkitPlayer.inventory.leggings = character.leggings
                    bukkitPlayer.inventory.boots = character.boots
                    bukkitPlayer.teleport(character.location)
                    bukkitPlayer.maxHealth = character.maxHealth
                    bukkitPlayer.health = character.health
                    bukkitPlayer.foodLevel = character.foodLevel
                }
            }
            activeCharacterCache.put(player.id, character.id)
        } else if (oldCharacter != null) {
            try {
                plugin.core!!.database.createConnection().use { connection ->
                    connection.prepareStatement(
                            "DELETE FROM player_character WHERE player_id = ? AND character_id = ?").use({ statement ->
                        statement.setInt(1, player.id)
                        statement.setInt(2, oldCharacter.id)
                        statement.executeUpdate()
                    })
                }
            } catch (exception: SQLException) {
                exception.printStackTrace()
            }
            activeCharacterCache.remove(player.id)
        }
    }

    override fun getCharacters(player: ElysiumPlayer): Collection<BukkitCharacter> {
        try {
            val characters: MutableList<BukkitCharacter> = ArrayList()
            plugin.core!!.database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id FROM bukkit_character WHERE player_id = ? ORDER BY id").use({ statement ->
                    statement.setInt(1, player.id)
                    val resultSet = statement.executeQuery()
                    while (resultSet.next()) {
                        characters.add(getCharacter(resultSet.getInt("id"))!!)
                    }

                })
            }
            return characters
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
        return emptyList()
    }

    override fun addCharacter(character: BukkitCharacter): Int {
        return plugin.core!!.database.getTable(BukkitCharacter::class.java)!!.insert(character)
    }

    override fun removeCharacter(character: BukkitCharacter) {
        val player = character.player
        if (player != null)
            setActiveCharacter(player, null)
        plugin.core!!.database.getTable(BukkitCharacter::class.java)!!.delete(character)
    }

    override fun updateCharacter(character: BukkitCharacter) {
        if (plugin.config.getBoolean("characters.delete-character-on-death") && character.isDead) {
            removeCharacter(character)
        } else {
            plugin.core!!.database.getTable(BukkitCharacter::class.java)!!.update(character)
        }
    }

}
