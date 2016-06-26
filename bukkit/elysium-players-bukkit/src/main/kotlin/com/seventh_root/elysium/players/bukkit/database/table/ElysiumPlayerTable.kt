/*
 * Copyright 2016 Ross Binden
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.seventh_root.elysium.players.bukkit.database.table

import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.database.Table
import com.seventh_root.elysium.core.database.use
import com.seventh_root.elysium.players.bukkit.ElysiumPlayersBukkit
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayer
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerImpl
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.pircbotx.User
import java.sql.Connection
import java.sql.Statement.RETURN_GENERATED_KEYS
import java.sql.Types.VARCHAR
import java.util.*

class ElysiumPlayerTable: Table<ElysiumPlayer> {

    private val cacheManager: CacheManager
    private val cache: Cache<Int, ElysiumPlayer>
    private val nameCache: Cache<String, Int>
    private val minecraftCache: Cache<String, Int>
    private val ircCache: Cache<String, Int>

    constructor(plugin: ElysiumPlayersBukkit, database: Database): super(database, ElysiumPlayer::class.java) {
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
        cache = cacheManager.createCache("cache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, ElysiumPlayer::class.java,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())).build())
        nameCache = cacheManager.createCache("nameCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String::class.java, Int::class.javaObjectType,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())).build())
        minecraftCache = cacheManager.createCache("minecraftCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String::class.java, Int::class.javaObjectType,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())).build())
        ircCache = cacheManager.createCache("ircCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String::class.java, Int::class.javaObjectType,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())).build())
    }

    override fun create() {
        database.createConnection().use { connection: Connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS elysium_player(" +
                        "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                        "name VARCHAR(256)," +
                        "minecraft_uuid VARCHAR(36)," +
                        "irc_nick VARCHAR(256)" +
                    ")").use { statement ->
                statement.executeUpdate()
            }
        }
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "0.3.0")
        }
        if (database.getTableVersion(this) == "0.1.0") {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "ALTER TABLE elysium_player ADD COLUMN name AFTER id"
                )
                connection.prepareStatement(
                        "ALTER TABLE elysium_player ADD COLUMN irc_nick VARCHAR(256)"
                )
            }
            database.setTableVersion(this, "0.3.0")
        }
    }

    override fun insert(entity: ElysiumPlayer): Int {
        var id: Int = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO elysium_player(name, minecraft_uuid, irc_nick) VALUES(?, ?, ?)",
                    RETURN_GENERATED_KEYS).use { statement ->
                statement.setString(1, entity.name)
                val bukkitPlayer = entity.bukkitPlayer
                if (bukkitPlayer != null) {
                    statement.setString(2, bukkitPlayer.uniqueId.toString())
                } else {
                    statement.setNull(2, VARCHAR)
                }
                val ircNick = entity.ircNick
                if (ircNick != null) {
                    statement.setString(3, ircNick)
                } else {
                    statement.setNull(3, VARCHAR)
                }
                statement.executeUpdate()
                val generatedKeys = statement.generatedKeys
                if (generatedKeys.next()) {
                    id = generatedKeys.getInt(1)
                    entity.id = id
                    cache.put(id, entity)
                    nameCache.put(entity.name, id)
                    if (bukkitPlayer != null) {
                        minecraftCache.put(bukkitPlayer.uniqueId.toString(), id)
                    }
                    if (ircNick != null) {
                        ircCache.put(ircNick, id)
                    }
                }
            }
        }
        return id
    }

    override fun update(entity: ElysiumPlayer) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE elysium_player SET name = ?, minecraft_uuid = ?, irc_nick = ? WHERE id = ?").use { statement ->
                statement.setString(1, entity.name)
                val bukkitPlayer = entity.bukkitPlayer
                if (bukkitPlayer != null) {
                    statement.setString(2, bukkitPlayer.uniqueId.toString())
                } else {
                    statement.setNull(2, VARCHAR)
                }
                val ircNick = entity.ircNick
                if (ircNick != null) {
                    statement.setString(3, ircNick)
                } else {
                    statement.setNull(3, VARCHAR)
                }
                statement.setInt(4, entity.id)
                statement.executeUpdate()
                cache.put(entity.id, entity)
                nameCache.put(entity.name, entity.id)
                if (bukkitPlayer != null) {
                    minecraftCache.put(bukkitPlayer.uniqueId.toString(), entity.id)
                }
                if (ircNick != null) {
                    ircCache.put(ircNick, entity.id)
                }
            }
        }
    }

    override fun get(id: Int): ElysiumPlayer? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            var player: ElysiumPlayer? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, name, minecraft_uuid, irc_nick FROM elysium_player WHERE id = ?").use { statement ->
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val name = resultSet.getString("name")
                        val minecraftUUID = resultSet.getString("minecraft_uuid")
                        val ircNick = resultSet.getString("irc_nick")
                        player = ElysiumPlayerImpl(
                                id,
                                name,
                                if (minecraftUUID == null) null else Bukkit.getOfflinePlayer(UUID.fromString(minecraftUUID)),
                                ircNick
                        )
                        cache.put(id, player)
                        if (minecraftUUID != null) {
                            minecraftCache.put(minecraftUUID, id)
                        }
                        if (ircNick != null) {
                            ircCache.put(ircNick, id)
                        }
                    }
                }
            }
            return player
        }
    }

    fun get(name: String): ElysiumPlayer? {
        if (nameCache.containsKey(name)) {
            return get(nameCache.get(name))
        } else {
            var player: ElysiumPlayer? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, name, minecraft_uuid, irc_nick FROM elysium_player WHERE name = ?"
                ).use { statement ->
                    statement.setString(1, name)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val id = resultSet.getInt("id")
                        val minecraftUUID = resultSet.getString("minecraft_uuid")
                        val ircNick = resultSet.getString("irc_nick")
                        player = ElysiumPlayerImpl(
                                id,
                                name,
                                if (minecraftUUID == null) null else Bukkit.getOfflinePlayer(UUID.fromString(minecraftUUID)),
                                ircNick
                        )
                        cache.put(id, player)
                        nameCache.put(name, id)
                        if (minecraftUUID != null) {
                            minecraftCache.put(minecraftUUID, id)
                        }
                        if (ircNick != null) {
                            ircCache.put(ircNick, id)
                        }
                    }
                }
            }
            return player
        }
    }

    fun get(bukkitPlayer: OfflinePlayer): ElysiumPlayer? {
        if (minecraftCache.containsKey(bukkitPlayer.uniqueId.toString())) {
            return get(minecraftCache[bukkitPlayer.uniqueId.toString()])
        } else {
            var player: ElysiumPlayer? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, name, minecraft_uuid, irc_nick FROM elysium_player WHERE minecraft_uuid = ?").use { statement ->
                    statement.setString(1, bukkitPlayer.uniqueId.toString())
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val id = resultSet.getInt("id")
                        val name = resultSet.getString("name")
                        val minecraftUUID = resultSet.getString("minecraft_uuid")
                        val ircNick = resultSet.getString("irc_nick")
                        player = ElysiumPlayerImpl(
                                id,
                                name,
                                if (minecraftUUID == null) null else Bukkit.getOfflinePlayer(UUID.fromString(minecraftUUID)),
                                ircNick
                        )
                        cache.put(id, player)
                        nameCache.put(name, id)
                        if (minecraftUUID != null) {
                            minecraftCache.put(minecraftUUID, id)
                        }
                        if (ircNick != null) {
                            ircCache.put(ircNick, id)
                        }
                    }
                }
            }
            return player
        }
    }

    fun get(ircUser: User): ElysiumPlayer? {
        if (ircCache.containsKey(ircUser.nick)) {
            return get(ircCache.get(ircUser.nick))
        } else {
            var player: ElysiumPlayer? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, name, minecraft_uuid, irc_nick FROM elysium_player WHERE irc_nick = ?"
                ).use { statement ->
                    statement.setString(1, ircUser.nick)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val id = resultSet.getInt("id")
                        val name = resultSet.getString("name")
                        val minecraftUUID = resultSet.getString("minecraft_uuid")
                        val ircNick = resultSet.getString("irc_nick")
                        player = ElysiumPlayerImpl(
                                id,
                                name,
                                if (minecraftUUID == null) null else Bukkit.getOfflinePlayer(UUID.fromString(minecraftUUID)),
                                ircNick
                        )
                        cache.put(id, player)
                        nameCache.put(name, id)
                        if (minecraftUUID != null) {
                            minecraftCache.put(minecraftUUID, id)
                        }
                        if (ircNick != null) {
                            ircCache.put(ircNick, id)
                        }
                    }
                }
            }
            return player
        }
    }

    override fun delete(entity: ElysiumPlayer) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM elysium_player WHERE id = ?").use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
                cache.remove(entity.id)
                nameCache.remove(entity.name)
                val bukkitPlayer = entity.bukkitPlayer
                if (bukkitPlayer != null) {
                    minecraftCache.remove(bukkitPlayer.uniqueId.toString())
                }
                val ircNick = entity.ircNick
                if (ircNick != null) {
                    ircCache.remove(ircNick)
                }
            }
        }

    }

}
