package com.seventh_root.elysium.players.bukkit.database.table

import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.database.Table
import com.seventh_root.elysium.core.database.use
import com.seventh_root.elysium.players.bukkit.BukkitPlayer
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import java.sql.Statement.RETURN_GENERATED_KEYS
import java.util.*

class BukkitPlayerTable: Table<BukkitPlayer> {

    private val cacheManager: CacheManager
    private val preConfigured: Cache<Int, BukkitPlayer>
    private val cache: Cache<Int, BukkitPlayer>
    private val playerCacheManager: CacheManager
    private val playerPreConfigured: Cache<String, Int>
    private val playerCache: Cache<String, Int>

    constructor(database: Database): super(database, BukkitPlayer::class.java) {
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .withCache(
                        "preConfigured",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.java, BukkitPlayer::class.java)
                                .build()
                )
                .build(true)
        preConfigured = cacheManager.getCache("preConfigured", Int::class.java, BukkitPlayer::class.java)
        cache = cacheManager.createCache("cache", CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.java, BukkitPlayer::class.java).build())
        playerCacheManager = CacheManagerBuilder.newCacheManagerBuilder()
            .withCache(
                    "preConfigured",
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String::class.java, Int::class.java)
                        .build()
            )
            .build(true)
        playerPreConfigured = cacheManager.getCache("preConfigured", String::class.java, Int::class.java)
        playerCache = cacheManager.createCache("cache", CacheConfigurationBuilder.newCacheConfigurationBuilder(String::class.java, Int::class.java).build())
    }

    override fun create() {
        try {
            database.createConnection().use { connection: Connection ->
                connection.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS bukkit_player(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "minecraft_uuid VARCHAR(36)" +
                        ")").use({ statement: PreparedStatement -> statement.executeUpdate() })
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "0.1.0")
        }
    }

    override fun insert(`object`: BukkitPlayer): Int {
        try {
            var id: Int = 0
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "INSERT INTO bukkit_player(minecraft_uuid) VALUES(?)",
                        RETURN_GENERATED_KEYS).use({ statement ->
                    statement.setString(1, `object`.bukkitPlayer.uniqueId.toString())
                    statement.executeUpdate()
                    val generatedKeys = statement.generatedKeys
                    if (generatedKeys.next()) {
                        id = generatedKeys.getInt(1)
                        `object`.id = id
                        cache.put(id, `object`)
                        playerCache.put(`object`.bukkitPlayer.uniqueId.toString(), id)
                    }
                })
            }
            return id
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
        return 0
    }

    override fun update(`object`: BukkitPlayer) {
        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "UPDATE bukkit_player SET minecraft_uuid = ? WHERE id = ?").use({ statement ->
                    statement.setString(1, `object`.bukkitPlayer.uniqueId.toString())
                    statement.setInt(2, `object`.id)
                    statement.executeUpdate()
                    cache.put(`object`.id, `object`)
                    playerCache.put(`object`.bukkitPlayer.uniqueId.toString(), `object`.id)
                })
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

    }

    override fun get(id: Int): BukkitPlayer? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            try {
                var player: BukkitPlayer? = null
                database.createConnection().use { connection ->
                    connection.prepareStatement(
                            "SELECT id, minecraft_uuid FROM bukkit_player WHERE id = ?").use({ statement ->
                        statement.setInt(1, id)
                        val resultSet = statement.executeQuery()
                        if (resultSet.next()) {
                            val id1 = resultSet.getInt("id")
                            val minecraftUUID = resultSet.getString("minecraft_uuid")
                            player = BukkitPlayer(id1, Bukkit.getOfflinePlayer(UUID.fromString(minecraftUUID)))
                            cache.put(id, player)
                            playerCache.put(minecraftUUID, id)
                        }
                    })
                }
                return player
            } catch (exception: SQLException) {
                exception.printStackTrace()
            }
        }
        return null
    }

    operator fun get(bukkitPlayer: OfflinePlayer): BukkitPlayer? {
        try {
            var player: BukkitPlayer? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, minecraft_uuid FROM bukkit_player WHERE minecraft_uuid = ?").use({ statement ->
                    statement.setString(1, bukkitPlayer.uniqueId.toString())
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val id = resultSet.getInt("id")
                        val minecraftUUID = resultSet.getString("minecraft_uuid")
                        player = BukkitPlayer(resultSet.getInt("id"), Bukkit.getOfflinePlayer(UUID.fromString(resultSet.getString("minecraft_uuid"))))
                        cache.put(id, player)
                        playerCache.put(minecraftUUID, id)
                    }
                })
            }
            return player
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

        return null
    }

    override fun delete(`object`: BukkitPlayer) {
        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "DELETE FROM bukkit_player WHERE id = ?").use({ statement ->
                    statement.setInt(1, `object`.id)
                    statement.executeUpdate()
                    cache.remove(`object`.id)
                    playerCache.remove(`object`.bukkitPlayer.uniqueId.toString())
                })
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

    }

}
