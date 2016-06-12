package com.seventh_root.elysium.shops.bukkit.database.table

import com.seventh_root.elysium.api.character.ElysiumCharacter
import com.seventh_root.elysium.characters.bukkit.character.BukkitCharacterProvider
import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.database.Table
import com.seventh_root.elysium.core.database.use
import com.seventh_root.elysium.shops.bukkit.ElysiumShopsBukkit
import com.seventh_root.elysium.shops.bukkit.shopcount.BukkitShopCount
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import java.sql.Statement.RETURN_GENERATED_KEYS


class BukkitShopCountTable : Table<BukkitShopCount> {

    private val plugin: ElysiumShopsBukkit
    private val cacheManager: CacheManager
    private val cache: Cache<Int, BukkitShopCount>
    private val characterCache: Cache<Int, Int>

    constructor(database: Database, plugin: ElysiumShopsBukkit): super(database, BukkitShopCount::class.java) {
        this.plugin = plugin
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
        cache = cacheManager.createCache("cache", CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Int::class.javaObjectType, BukkitShopCount::class.java).build())
        characterCache = cacheManager.createCache("characterCache", CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Int::class.javaObjectType, Int::class.javaObjectType).build())
    }

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS bukkit_shop_count(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "character_id INTEGER," +
                            "count INTEGER," +
                            "FOREIGN KEY(character_id) REFERENCES bukkit_character(id)" +
                    ")"
            ).use { statement ->
                statement.executeUpdate()
            }
        }
    }

    override fun insert(`object`: BukkitShopCount): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO bukkit_shop_count(character_id, count) VALUES(?, ?)",
                    RETURN_GENERATED_KEYS
            ).use { statement ->
                statement.setInt(1, `object`.character.id)
                statement.setInt(2, `object`.count)
                statement.executeUpdate()
                val generatedKeys = statement.generatedKeys
                if (generatedKeys.next()) {
                    id = generatedKeys.getInt(1)
                    `object`.id = id
                    cache.put(id, `object`)
                    characterCache.put(`object`.character.id, id)
                }
            }
        }
        return id
    }

    override fun update(`object`: BukkitShopCount) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE bukkit_shop_count SET character_id = ?, count = ? WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, `object`.character.id)
                statement.setInt(2, `object`.count)
                statement.setInt(3, `object`.id)
                statement.executeUpdate()
                cache.put(`object`.id, `object`)
                characterCache.put(`object`.character.id, `object`.id)
            }
        }
    }

    override fun get(id: Int): BukkitShopCount? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            var shopCount: BukkitShopCount? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, character_id, count FROM bukkit_shop_count WHERE id = ?"
                ).use { statement ->
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val characterId = resultSet.getInt("character_id")
                        shopCount = BukkitShopCount(
                                id,
                                plugin.core.serviceManager.getServiceProvider(BukkitCharacterProvider::class.java)
                                        .getCharacter(characterId)!!,
                                resultSet.getInt("count")
                        )
                        cache.put(id, shopCount)
                        characterCache.put(characterId, id)
                    }
                }
            }
            return shopCount
        }
    }

    fun get(character: ElysiumCharacter): BukkitShopCount? {
        if (characterCache.containsKey(character.id)) {
            return get(characterCache.get(character.id) as Int)
        } else {
            var shopCount: BukkitShopCount? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, character_id, count FROM bukkit_shop_count WHERE character_id = ?"
                ).use { statement ->
                    statement.setInt(1, character.id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val id = resultSet.getInt("id")
                        shopCount = BukkitShopCount(
                                id,
                                character,
                                resultSet.getInt("count")
                        )
                        cache.put(id, shopCount)
                        characterCache.put(character.id, id)
                    }
                }
            }
            return shopCount
        }
    }

    override fun delete(`object`: BukkitShopCount) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM bukkit_shop_count WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, `object`.id)
                statement.executeUpdate()
                cache.remove(`object`.id)
                characterCache.remove(`object`.character.id)
            }
        }
    }
}