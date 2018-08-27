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

package com.rpkit.players.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.database.jooq.rpkit.Tables.RPKIT_PLAYER
import com.rpkit.players.bukkit.player.RPKPlayer
import com.rpkit.players.bukkit.player.RPKPlayerImpl
import org.bukkit.OfflinePlayer
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.SQLDialect
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType
import org.jooq.util.sqlite.SQLiteDataType
import org.pircbotx.User
import java.net.InetAddress
import java.util.*

/**
 * Represents the player table.
 */
class RPKPlayerTable(plugin: RPKPlayersBukkit, database: Database): Table<RPKPlayer>(database, RPKPlayer::class.java) {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_player.id.enabled")) {
        database.cacheManager.createCache("rpk-players-bukkit.rpkit_player.id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKPlayer::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_player.id.size"))).build())
    } else {
        null
    }

    private val nameCache = if (plugin.config.getBoolean("caching.rpkit_player.name.enabled")) {
        database.cacheManager.createCache("rpk-players-bukkit.rpkit_player.name",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String::class.java, Int::class.javaObjectType,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_player.name.size"))).build())
    } else {
        null
    }

    private val minecraftCache = if (plugin.config.getBoolean("caching.rpkit_player.minecraft_uuid.enabled")) {
        database.cacheManager.createCache("rpk-players-bukkit.rpkit_player.minecraft_uuid",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String::class.java, Int::class.javaObjectType,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_player.minecraft_uuid.size"))).build())
    } else {
        null
    }

    private val ircCache = if (plugin.config.getBoolean("caching.rpkit_player.irc_nick.enabled")) {
        database.cacheManager.createCache("rpk-players-bukkit.rpkit_player.irc_nick",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String::class.java, Int::class.javaObjectType,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_player.irc_nick.size"))).build())
    } else {
        null
    }

    private val ipCache = if (plugin.config.getBoolean("caching.rpkit_player.last_known_ip.enabled")) {
        database.cacheManager.createCache("rpk-players-bukkit.rpkit_player.last_known_ip",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String::class.java, Int::class.javaObjectType,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_player.last_known_ip.size"))).build())
    } else {
        null
    }

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_PLAYER)
                .column(RPKIT_PLAYER.ID, if (database.dialect == SQLDialect.SQLITE) SQLiteDataType.INTEGER.identity(true) else SQLDataType.INTEGER.identity(true))
                .column(RPKIT_PLAYER.NAME, SQLDataType.VARCHAR(256))
                .column(RPKIT_PLAYER.MINECRAFT_UUID, SQLDataType.VARCHAR(36))
                .column(RPKIT_PLAYER.IRC_NICK, SQLDataType.VARCHAR(256))
                .column(RPKIT_PLAYER.LAST_KNOWN_IP, SQLDataType.VARCHAR(256))
                .constraints(
                        constraint("pk_rpkit_player").primaryKey(RPKIT_PLAYER.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "0.4.0")
        }
        if (database.getTableVersion(this) == "0.1.0") {
            database.create
                    .alterTable(RPKIT_PLAYER)
                    .addColumn(RPKIT_PLAYER.NAME, SQLDataType.VARCHAR(256))
                    .execute()
            database.create
                    .alterTable(RPKIT_PLAYER)
                    .addColumn(RPKIT_PLAYER.IRC_NICK, SQLDataType.VARCHAR(256))
                    .execute()
            database.setTableVersion(this, "0.3.0")
        }
        if (database.getTableVersion(this) == "0.3.0") {
            database.create
                    .alterTable(RPKIT_PLAYER)
                    .addColumn(RPKIT_PLAYER.LAST_KNOWN_IP, SQLDataType.VARCHAR(256))
                    .execute()
            database.setTableVersion(this, "0.4.0")
        }
    }

    override fun insert(entity: RPKPlayer): Int {
        database.create
                .insertInto(
                        RPKIT_PLAYER,
                        RPKIT_PLAYER.NAME,
                        RPKIT_PLAYER.MINECRAFT_UUID,
                        RPKIT_PLAYER.IRC_NICK,
                        RPKIT_PLAYER.LAST_KNOWN_IP
                )
                .values(
                        entity.name,
                        entity.bukkitPlayer?.uniqueId?.toString(),
                        entity.ircNick,
                        entity.lastKnownIP
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
        nameCache?.put(entity.name, id)
        val bukkitPlayer = entity.bukkitPlayer
        if (bukkitPlayer != null) {
            minecraftCache?.put(bukkitPlayer.uniqueId.toString(), id)
        }
        val ircNick = entity.ircNick
        if (ircNick != null) {
            ircCache?.put(ircNick, id)
        }
        return id
    }

    override fun update(entity: RPKPlayer) {
        database.create
                .update(RPKIT_PLAYER)
                .set(RPKIT_PLAYER.NAME, entity.name)
                .set(RPKIT_PLAYER.MINECRAFT_UUID, entity.bukkitPlayer?.uniqueId?.toString())
                .set(RPKIT_PLAYER.IRC_NICK, entity.ircNick)
                .set(RPKIT_PLAYER.LAST_KNOWN_IP, entity.lastKnownIP)
                .where(RPKIT_PLAYER.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
        nameCache?.put(entity.name, entity.id)
        val bukkitPlayer = entity.bukkitPlayer
        if (bukkitPlayer != null) {
            minecraftCache?.put(bukkitPlayer.uniqueId.toString(), entity.id)
        }
        val ircNick = entity.ircNick
        if (ircNick != null) {
            ircCache?.put(ircNick, entity.id)
        }
        val lastKnownIP = entity.lastKnownIP
        if (lastKnownIP != null) {
            ipCache?.put(lastKnownIP, entity.id)
        }
    }

    override fun get(id: Int): RPKPlayer? {
        if (cache?.containsKey(id) == true) {
            return cache.get(id)
        } else {
            val result = database.create
                    .select(
                            RPKIT_PLAYER.NAME,
                            RPKIT_PLAYER.MINECRAFT_UUID,
                            RPKIT_PLAYER.IRC_NICK,
                            RPKIT_PLAYER.LAST_KNOWN_IP
                    )
                    .from(RPKIT_PLAYER)
                    .where(RPKIT_PLAYER.ID.eq(id))
                    .fetchOne() ?: return null
            val name = result.get(RPKIT_PLAYER.NAME)
            val minecraftUUID = result.get(RPKIT_PLAYER.MINECRAFT_UUID)
            val ircNick = result.get(RPKIT_PLAYER.IRC_NICK)
            val lastKnownIP = result.get(RPKIT_PLAYER.LAST_KNOWN_IP)
            val player = RPKPlayerImpl(
                    id,
                    name,
                    if (minecraftUUID == null) null else UUID.fromString(minecraftUUID),
                    ircNick,
                    lastKnownIP
            )
            cache?.put(id, player)
            nameCache?.put(name, id)
            if (minecraftUUID != null) {
                minecraftCache?.put(minecraftUUID, id)
            }
            if (ircNick != null) {
                ircCache?.put(ircNick, id)
            }
            if (lastKnownIP != null) {
                ipCache?.put(lastKnownIP, id)
            }
            return player
        }
    }

    /**
     * Gets a player by name.
     * If no player is found with the given name, null is returned.
     *
     * @param name The name of the player
     * @return The player, or null if no player is found with the given name
     */
    fun get(name: String): RPKPlayer? {
        if (nameCache?.containsKey(name) == true) {
            return get(nameCache.get(name))
        } else {
            val result = database.create
                    .select(RPKIT_PLAYER.ID)
                    .from(RPKIT_PLAYER)
                    .where(RPKIT_PLAYER.NAME.eq(name))
                    .fetchOne() ?: return null
            return get(result.get(RPKIT_PLAYER.ID))
        }
    }

    /**
     * Gets a player by Bukkit player instance.
     * If no player is found linked to the given Bukkit player, null is returned.
     *
     * @param bukkitPlayer The Bukkit player instance
     * @return The player, or null if no player is found linked to the Bukkit player instance
     */
    fun get(bukkitPlayer: OfflinePlayer): RPKPlayer? {
        if (minecraftCache?.containsKey(bukkitPlayer.uniqueId.toString()) == true) {
            return get(minecraftCache[bukkitPlayer.uniqueId.toString()])
        } else {
            val result = database.create
                    .select(RPKIT_PLAYER.ID)
                    .from(RPKIT_PLAYER)
                    .where(RPKIT_PLAYER.MINECRAFT_UUID.eq(bukkitPlayer.uniqueId.toString()))
                    .fetchOne() ?: return null
            return get(result.get(RPKIT_PLAYER.ID))
        }
    }


    /**
     * Gets a player by the IRC user instance.
     * If no player is found linked to the given IRC user, null is returned.
     *
     * @param ircUser The IRC user instance
     * @return The player, or null if no player is found linked to the IRC user instance
     */
    fun get(ircUser: User): RPKPlayer? {
        if (ircCache?.containsKey(ircUser.nick) == true) {
            return get(ircCache.get(ircUser.nick))
        } else {
            val result = database.create
                    .select(RPKIT_PLAYER.ID)
                    .from(RPKIT_PLAYER)
                    .where(RPKIT_PLAYER.IRC_NICK.eq(ircUser.nick))
                    .fetchOne() ?: return null
            return get(result.get(RPKIT_PLAYER.ID))
        }
    }

    /**
     * Gets a player by an IP address.
     * If no player last used the given IP address, null is returned.
     *
     * @param lastKnownIP The last known IP
     * @return The player that last used the IP, or null if no player is found that last used the IP
     */
    fun get(lastKnownIP: InetAddress): RPKPlayer? {
        if (ipCache?.containsKey(lastKnownIP.hostAddress) == true) {
            return get(ipCache.get(lastKnownIP.hostAddress))!!
        } else {
            val result = database.create
                    .select(RPKIT_PLAYER.ID)
                    .from(RPKIT_PLAYER)
                    .where(RPKIT_PLAYER.LAST_KNOWN_IP.eq(lastKnownIP.toString()))
                    .fetchOne() ?: return null
            return get(result.get(RPKIT_PLAYER.ID))
        }
    }

    override fun delete(entity: RPKPlayer) {
        database.create
                .deleteFrom(RPKIT_PLAYER)
                .where(RPKIT_PLAYER.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
        nameCache?.remove(entity.name)
        val bukkitPlayer = entity.bukkitPlayer
        if (bukkitPlayer != null) {
            minecraftCache?.remove(bukkitPlayer.uniqueId.toString())
        }
        val ircNick = entity.ircNick
        if (ircNick != null) {
            ircCache?.remove(ircNick)
        }
        val lastKnownIP = entity.lastKnownIP
        if (lastKnownIP != null) {
            ipCache?.remove(lastKnownIP)
        }
    }

}
