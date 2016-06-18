package com.seventh_root.elysium.economy.bukkit.database.table

import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.database.Table
import com.seventh_root.elysium.core.database.use
import com.seventh_root.elysium.economy.bukkit.ElysiumEconomyBukkit
import com.seventh_root.elysium.economy.bukkit.currency.BukkitCurrency
import org.bukkit.Material
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.sql.Statement.RETURN_GENERATED_KEYS
import java.util.*


class BukkitCurrencyTable: Table<BukkitCurrency> {

    private val plugin: ElysiumEconomyBukkit
    private val cacheManager: CacheManager
    private val cache: Cache<Int, BukkitCurrency>
    private val nameCache: Cache<String, Int>

    constructor(database: Database, plugin: ElysiumEconomyBukkit): super(database, BukkitCurrency::class.java) {
        this.plugin = plugin;
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .build(true)
        cache = cacheManager.createCache("cache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, BukkitCurrency::class.java,
                        ResourcePoolsBuilder.heap(5L)).build())
        nameCache = cacheManager.createCache("nameCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String::class.java, Int::class.javaObjectType,
                        ResourcePoolsBuilder.heap(5L)).build())
    }

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS bukkit_currency(" +
                        "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                        "name VARCHAR(256)," +
                        "name_singular VARCHAR(256)," +
                        "name_plural VARCHAR(256)," +
                        "rate DOUBLE," +
                        "default_amount INTEGER," +
                        "material VARCHAR(256)" +
                    ")"
            ).use { statement ->
                statement.executeUpdate()
            }
        }
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "0.2.0")
        }
    }

    override fun insert(`object`: BukkitCurrency): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO bukkit_currency(name, name_singular, name_plural, rate, default_amount, material) VALUES(?, ?, ?, ?, ?, ?)",
                    RETURN_GENERATED_KEYS
            ).use { statement ->
                statement.setString(1, `object`.name)
                statement.setString(2, `object`.nameSingular)
                statement.setString(3, `object`.namePlural)
                statement.setDouble(4, `object`.rate)
                statement.setInt(5, `object`.defaultAmount)
                statement.setString(6, `object`.material.name)
                statement.executeUpdate()
                val generatedKeys = statement.generatedKeys
                if (generatedKeys.next()) {
                    id = generatedKeys.getInt(1)
                    `object`.id = id
                    cache.put(id, `object`)
                    nameCache.put(`object`.name, id)
                }
            }
        }
        return id
    }

    override fun update(`object`: BukkitCurrency) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE bukkit_currency SET name = ?, name_singular = ?, name_plural = ?, rate = ?, default_amount = ?, material = ? WHERE id = ?"
            ).use { statement ->
                statement.setString(1, `object`.name)
                statement.setString(2, `object`.nameSingular)
                statement.setString(3, `object`.namePlural)
                statement.setDouble(4, `object`.rate)
                statement.setInt(5, `object`.defaultAmount)
                statement.setString(6, `object`.material.name)
                statement.setInt(7, `object`.id)
                statement.executeUpdate()
                cache.put(`object`.id, `object`)
                nameCache.put(`object`.name, `object`.id)
            }
        }
    }

    override fun get(id: Int): BukkitCurrency? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            var currency: BukkitCurrency? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, name, name_singular, name_plural, rate, default_amount, material FROM bukkit_currency WHERE id = ?"
                ).use { statement ->
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        currency = BukkitCurrency(
                                resultSet.getInt("id"),
                                resultSet.getString("name"),
                                resultSet.getString("name_singular"),
                                resultSet.getString("name_plural"),
                                resultSet.getDouble("rate"),
                                resultSet.getInt("default_amount"),
                                Material.getMaterial(resultSet.getString("material"))
                        )
                        if (currency != null) {
                            val finalCurrency = currency!!
                            cache.put(id, finalCurrency)
                            nameCache.put(finalCurrency.name, id)
                        }
                    }
                }
            }
            return currency
        }
    }

    fun get(name: String): BukkitCurrency? {
        if (nameCache.containsKey(name)) {
            return get(nameCache.get(name) as Int)
        } else {
            var currency: BukkitCurrency? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, name, name_singular, name_plural, rate, default_amount, material FROM bukkit_currency WHERE name = ?"
                ).use { statement ->
                    statement.setString(1, name)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        currency = BukkitCurrency(
                                resultSet.getInt("id"),
                                resultSet.getString("name"),
                                resultSet.getString("name_singular"),
                                resultSet.getString("name_plural"),
                                resultSet.getDouble("rate"),
                                resultSet.getInt("default_amount"),
                                Material.getMaterial(resultSet.getString("material"))
                        )
                        if (currency != null) {
                            val finalCurrency = currency!!
                            cache.put(finalCurrency.id, finalCurrency)
                            nameCache.put(finalCurrency.name, finalCurrency.id)
                        }
                    }
                }
            }
            return currency
        }
    }

    fun getAll(): Collection<BukkitCurrency> {
        val currencies = ArrayList<BukkitCurrency>()
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id FROM bukkit_currency"
            ).use { statement ->
                val resultSet = statement.executeQuery()
                while (resultSet.next()) {
                    val currency = get(resultSet.getInt("id"))
                    if (currency != null) currencies.add(currency)
                }
            }
        }
        return currencies
    }

    override fun delete(`object`: BukkitCurrency) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM bukkit_currency WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, `object`.id)
                statement.executeUpdate()
                if (cache.containsKey(`object`.id)) {
                    cache.remove(`object`.id)
                }
            }
        }
    }

}