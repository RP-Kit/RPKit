package com.seventh_root.elysium.chat.bukkit.database.table

import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit
import com.seventh_root.elysium.chat.bukkit.chatchannel.BukkitChatChannel
import com.seventh_root.elysium.chat.bukkit.chatchannel.BukkitChatChannelProvider
import com.seventh_root.elysium.chat.bukkit.chatchannel.ChatChannelSpeaker
import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.database.Table
import com.seventh_root.elysium.core.database.use
import com.seventh_root.elysium.players.bukkit.player.BukkitPlayerProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayer
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.sql.Statement.RETURN_GENERATED_KEYS


class ChatChannelSpeakerTable: Table<ChatChannelSpeaker> {

    private val plugin: ElysiumChatBukkit
    private val cacheManager: CacheManager
    private val cache: Cache<Int, ChatChannelSpeaker>
    private val chatChannelCache: Cache<Int, MutableList<*>>
    private val playerCache: Cache<Int, Int>

    constructor(plugin: ElysiumChatBukkit, database: Database): super(database, ChatChannelSpeaker::class.java) {
        this.plugin = plugin
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
        cache = cacheManager.createCache("cache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, ChatChannelSpeaker::class.java,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())).build())
        chatChannelCache = cacheManager.createCache("chatChannelCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, MutableList::class.javaObjectType,
                        ResourcePoolsBuilder.heap(20L)).build())
        playerCache = cacheManager.createCache("playerCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, Int::class.javaObjectType,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())).build())
    }

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS chat_channel_speaker(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "player_id INTEGER," +
                            "chat_channel_id INTEGER," +
                            "FOREIGN KEY(player_id) REFERENCES bukkit_player(id)," +
                            "FOREIGN KEY(chat_channel_id) REFERENCES bukkit_chat_channel(id)" +
                            ")").use { statement ->
                statement.executeUpdate()
            }
        }
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "0.3.0")
        }
    }

    override fun insert(`object`: ChatChannelSpeaker): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO chat_channel_speaker(player_id, chat_channel_id) VALUES(?, ?)",
                    RETURN_GENERATED_KEYS
            ).use { statement ->
                statement.setInt(1, `object`.player.id)
                statement.setInt(2, `object`.chatChannel.id)
                statement.executeUpdate()
                val generatedKeys = statement.generatedKeys
                if (generatedKeys.next()) {
                    id = generatedKeys.getInt(1)
                    `object`.id = id
                    cache.put(id, `object`)
                    val chatChannelSpeakers = chatChannelCache.get(`object`.chatChannel.id) as? MutableList<Int>?:mutableListOf<Int>()
                    if (!chatChannelSpeakers.contains(id)) {
                        chatChannelSpeakers.add(id)
                    }
                    chatChannelCache.put(`object`.chatChannel.id, chatChannelSpeakers)
                    playerCache.put(`object`.player.id, `object`.id)
                }
            }
        }
        return id
    }

    override fun update(`object`: ChatChannelSpeaker) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE chat_channel_speaker SET chat_channel_id = ?, player_id = ? WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, `object`.chatChannel.id)
                statement.setInt(2, `object`.player.id)
                statement.setInt(3, `object`.id)
                statement.executeUpdate()
                cache.put(`object`.id, `object`)
                val chatChannelSpeakers = chatChannelCache.get(`object`.chatChannel.id) as? MutableList<Int>?:mutableListOf<Int>()
                if (!chatChannelSpeakers.contains(`object`.id)) {
                    chatChannelSpeakers.add(`object`.id)
                }
                chatChannelCache.put(`object`.chatChannel.id, chatChannelSpeakers)
                playerCache.put(`object`.player.id, `object`.id)
            }
        }
    }

    override fun get(id: Int): ChatChannelSpeaker? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            var chatChannelSpeaker: ChatChannelSpeaker? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, chat_channel_id, player_id FROM chat_channel_speaker WHERE id = ?"
                ).use { statement ->
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val finalChatChannelSpeaker = ChatChannelSpeaker(
                                resultSet.getInt("id"),
                                plugin.core.serviceManager.getServiceProvider(BukkitChatChannelProvider::class.java).getChatChannel(resultSet.getInt("chat_channel_id"))!!,
                                plugin.core.serviceManager.getServiceProvider(BukkitPlayerProvider::class.java).getPlayer(resultSet.getInt("player_id"))!!
                        )
                        chatChannelSpeaker = finalChatChannelSpeaker
                        cache.put(id, finalChatChannelSpeaker)
                        val chatChannelSpeakers = chatChannelCache.get(finalChatChannelSpeaker.chatChannel.id) as? MutableList<Int>?:mutableListOf<Int>()
                        if (!chatChannelSpeakers.contains(finalChatChannelSpeaker.id)) {
                            chatChannelSpeakers.add(finalChatChannelSpeaker.id)
                        }
                        chatChannelCache.put(finalChatChannelSpeaker.chatChannel.id, chatChannelSpeakers)
                        playerCache.put(finalChatChannelSpeaker.player.id, finalChatChannelSpeaker.id)
                    }
                }
            }
            return chatChannelSpeaker
        }
    }

    fun get(player: ElysiumPlayer): ChatChannelSpeaker? {
        if (playerCache.containsKey(player.id)) {
            return get(playerCache.get(player.id))
        } else {
            var chatChannelSpeaker: ChatChannelSpeaker? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, chat_channel_id, player_id FROM chat_channel_speaker WHERE player_id = ?"
                ).use { statement ->
                    statement.setInt(1, player.id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val id = resultSet.getInt("id")
                        val finalChatChannelSpeaker = ChatChannelSpeaker(
                                id,
                                plugin.core.serviceManager.getServiceProvider(BukkitChatChannelProvider::class.java).getChatChannel(resultSet.getInt("chat_channel_id"))!!,
                                plugin.core.serviceManager.getServiceProvider(BukkitPlayerProvider::class.java).getPlayer(resultSet.getInt("player_id"))!!
                        )
                        chatChannelSpeaker = finalChatChannelSpeaker
                        cache.put(id, finalChatChannelSpeaker)
                        val chatChannelSpeakers = chatChannelCache.get(finalChatChannelSpeaker.chatChannel.id) as? MutableList<Int>?:mutableListOf<Int>()
                        if (!chatChannelSpeakers.contains(finalChatChannelSpeaker.id)) {
                            chatChannelSpeakers.add(finalChatChannelSpeaker.id)
                        }
                        chatChannelCache.put(finalChatChannelSpeaker.chatChannel.id, chatChannelSpeakers)
                        playerCache.put(finalChatChannelSpeaker.player.id, finalChatChannelSpeaker.id)
                    }
                }
            }
            return chatChannelSpeaker
        }
    }

    fun get(chatChannel: BukkitChatChannel): List<ChatChannelSpeaker> {
        if (chatChannelCache.containsKey(chatChannel.id)) {
            return (chatChannelCache.get(chatChannel.id) as MutableList<Int>).map { speakerId -> get(speakerId)!! }
        } else {
            val chatChannelSpeakers = mutableListOf<ChatChannelSpeaker>()
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, chat_channel_id, player_id FROM chat_channel_speaker WHERE chat_channel_id = ?"
                ).use { statement ->
                    statement.setInt(1, chatChannel.id)
                    val resultSet = statement.executeQuery()
                    while (resultSet.next()) {
                        chatChannelSpeakers.add(get(resultSet.getInt("id"))!!)
                    }
                }
            }
            return chatChannelSpeakers
        }
    }

    override fun delete(`object`: ChatChannelSpeaker) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM chat_channel_speaker WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, `object`.id)
                statement.executeUpdate()
                cache.remove(`object`.id)
                chatChannelCache.remove(`object`.chatChannel.id)
                playerCache.remove(`object`.player.id)
            }
        }
    }
}