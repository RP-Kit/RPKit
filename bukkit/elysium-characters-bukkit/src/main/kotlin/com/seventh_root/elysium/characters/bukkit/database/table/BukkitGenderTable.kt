package com.seventh_root.elysium.characters.bukkit.database.table

import com.seventh_root.elysium.characters.bukkit.gender.BukkitGender
import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.database.Table
import com.seventh_root.elysium.core.database.use
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import java.sql.SQLException
import java.sql.Statement.RETURN_GENERATED_KEYS

class BukkitGenderTable: Table<BukkitGender> {

    private val cacheManager: CacheManager
    private val cache: Cache<Integer, BukkitGender>
    private val nameCache: Cache<String, Integer>

    constructor(database: Database): super(database, BukkitGender::class.java) {
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
        cache = cacheManager.createCache("cache", CacheConfigurationBuilder.newCacheConfigurationBuilder(Integer::class.java, BukkitGender::class.java).build())
        nameCache = cacheManager.createCache("nameCache", CacheConfigurationBuilder.newCacheConfigurationBuilder(String::class.java, Integer::class.java).build())
    }

    override fun create() {
        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS bukkit_gender(" +
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

    override fun insert(`object`: BukkitGender): Int {
        try {
            var id = 0
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "INSERT INTO bukkit_gender(name) VALUES(?)",
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

    override fun update(`object`: BukkitGender) {
        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "UPDATE bukkit_gender SET name = ? WHERE id = ?").use({ statement ->
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

    override fun get(id: Int): BukkitGender? {
        if (cache.containsKey(id as Integer)) {
            return cache.get(id as Integer)
        } else {
            try {
                var gender: BukkitGender? = null
                database.createConnection().use { connection ->
                    connection.prepareStatement(
                            "SELECT id, name FROM bukkit_gender WHERE id = ?").use({ statement ->
                        statement.setInt(1, id)
                        val resultSet = statement.executeQuery()
                        if (resultSet.next()) {
                            val id1 = resultSet.getInt("id")
                            val name = resultSet.getString("name")
                            gender = BukkitGender(id1, name)
                            cache.put(id, gender)
                            nameCache.put(name, id1 as Integer)
                        }
                    })
                }
                return gender
            } catch (exception: SQLException) {
                exception.printStackTrace()
            }
        }
        return null
    }

    operator fun get(name: String): BukkitGender? {
        if (nameCache.containsKey(name)) {
            return get(nameCache.get(name) as Int)
        } else {
            try {
                var gender: BukkitGender? = null
                database.createConnection().use { connection ->
                    connection.prepareStatement(
                            "SELECT id, name FROM bukkit_gender WHERE name = ?").use({ statement ->
                        statement.setString(1, name)
                        val resultSet = statement.executeQuery()
                        if (resultSet.next()) {
                            val id = resultSet.getInt("id")
                            val name1 = resultSet.getString("name")
                            gender = BukkitGender(id, name1)
                            cache.put(id as Integer, gender)
                            nameCache.put(name1, id)
                        }
                    })
                }
                return gender
            } catch (exception: SQLException) {
                exception.printStackTrace()
            }
        }
        return null
    }

    override fun delete(`object`: BukkitGender) {
        try {
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "DELETE FROM bukkit_gender WHERE id = ?").use({ statement ->
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
