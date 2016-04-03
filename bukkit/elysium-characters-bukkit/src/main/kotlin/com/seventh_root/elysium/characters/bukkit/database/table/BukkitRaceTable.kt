package com.seventh_root.elysium.characters.bukkit.database.table

import com.seventh_root.elysium.characters.bukkit.race.BukkitRace
import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.database.Table
import com.seventh_root.elysium.core.database.use
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import java.sql.SQLException
import java.sql.Statement.RETURN_GENERATED_KEYS

class BukkitRaceTable: Table<BukkitRace> {

    private val cacheManager: CacheManager
    private val preConfigured: Cache<Integer, BukkitRace>
    private val cache: Cache<Integer, BukkitRace>

    private val nameCacheManager: CacheManager
    private val namePreConfigured: Cache<String, Integer>
    private val nameCache: Cache<String, Integer>

    constructor(database: Database): super(database, BukkitRace::class.java) {
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .withCache(
                        "preConfigured",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(Integer::class.java, BukkitRace::class.java)
                                .build()
                )
                .build(true)
        preConfigured = cacheManager.getCache("preConfigured", Integer::class.java, BukkitRace::class.java)
        cache = cacheManager.createCache("cache", CacheConfigurationBuilder.newCacheConfigurationBuilder(Integer::class.java, BukkitRace::class.java).build())
        nameCacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .withCache(
                        "preConfigured",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(String::class.java, Integer::class.java)
                                .build()
                )
                .build(true)
        namePreConfigured = nameCacheManager.getCache("preConfigured", String::class.java, Integer::class.java)
        nameCache = nameCacheManager.createCache("cache", CacheConfigurationBuilder.newCacheConfigurationBuilder(String::class.java, Integer::class.java).build())
    }

    override fun create() {
        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS bukkit_race(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "name VARCHAR(256)" +
                        ")").use({ statement -> statement.executeUpdate() })
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "0.1.0")
        }
    }

    override fun insert(`object`: BukkitRace): Int {
        try {
            var id = 0
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "INSERT INTO bukkit_race(name) VALUES(?)",
                        RETURN_GENERATED_KEYS).use({ statement ->
                    statement.setString(1, `object`.name)
                    statement.executeUpdate()
                    val generatedKeys = statement.generatedKeys
                    if (generatedKeys.next()) {
                        id = generatedKeys.getInt(1)
                        `object`.id = id
                        cache.put(id as Integer, `object`)
                        nameCache.put(`object`.name, id as Integer)
                    }
                })
            }
            return id
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
        return 0
    }

    override fun update(`object`: BukkitRace) {
        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "UPDATE bukkit_race SET name = ? WHERE id = ?").use({ statement ->
                    statement.setString(1, `object`.name)
                    statement.setInt(2, `object`.id)
                    statement.executeUpdate()
                    cache.put(`object`.id as Integer, `object`)
                    nameCache.put(`object`.name, `object`.id as Integer)
                })
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
    }

    override fun get(id: Int): BukkitRace? {
        if (cache.containsKey(id as Integer)) {
            return cache.get(id)
        } else {
            try {
                var race: BukkitRace? = null
                database.createConnection().use { connection ->
                    connection.prepareStatement(
                            "SELECT id, name FROM bukkit_race WHERE id = ?").use({ statement ->
                        statement.setInt(1, id)
                        val resultSet = statement.executeQuery()
                        if (resultSet.next()) {
                            val id1 = resultSet.getInt("id")
                            val name = resultSet.getString("name")
                            race = BukkitRace(id1, name)
                            cache.put(id, race)
                            nameCache.put(name, id1 as Integer)
                        }
                    })
                }
                return race
            } catch (exception: SQLException) {
                exception.printStackTrace()
            }
        }
        return null
    }

    operator fun get(name: String): BukkitRace? {
        if (nameCache.containsKey(name)) {
            return get(nameCache.get(name) as Int)
        } else {
            try {
                var race: BukkitRace? = null
                database.createConnection().use { connection ->
                    connection.prepareStatement(
                            "SELECT id, name FROM bukkit_race WHERE name = ?").use({ statement ->
                        statement.setString(1, name)
                        val resultSet = statement.executeQuery()
                        if (resultSet.next()) {
                            val id = resultSet.getInt("id")
                            val name1 = resultSet.getString("name")
                            race = BukkitRace(id, name1)
                            cache.put(id as Integer, race)
                            nameCache.put(name1, id)
                        }
                    })
                }
                return race
            } catch (exception: SQLException) {
                exception.printStackTrace()
            }
        }
        return null
    }

    override fun delete(`object`: BukkitRace) {
        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "DELETE FROM bukkit_race WHERE id = ?").use({ statement ->
                    statement.setInt(1, `object`.id)
                    statement.executeUpdate()
                    cache.remove(`object`.id as Integer)
                    nameCache.remove(`object`.name)
                })
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
    }

}
