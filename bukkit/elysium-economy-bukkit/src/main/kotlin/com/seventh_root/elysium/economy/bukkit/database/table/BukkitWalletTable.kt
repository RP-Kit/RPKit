package com.seventh_root.elysium.economy.bukkit.database.table

import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacterProvider
import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.database.Table
import com.seventh_root.elysium.core.database.use
import com.seventh_root.elysium.economy.bukkit.ElysiumEconomyBukkit
import com.seventh_root.elysium.economy.bukkit.currency.ElysiumCurrency
import com.seventh_root.elysium.economy.bukkit.currency.ElysiumCurrencyProvider
import com.seventh_root.elysium.economy.bukkit.wallet.BukkitWallet
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.sql.Statement.RETURN_GENERATED_KEYS
import java.util.*


class BukkitWalletTable: Table<BukkitWallet> {

    private val plugin: ElysiumEconomyBukkit
    private val cacheManager: CacheManager
    private val cache: Cache<Int, BukkitWallet>
    private val characterCache: Cache<Int, MutableMap<*, *>>

    constructor(database: Database, plugin: ElysiumEconomyBukkit): super(database, BukkitWallet::class.java) {
        this.plugin = plugin;
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
        cache = cacheManager.createCache("cache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, BukkitWallet::class.java,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())).build())
        characterCache = cacheManager.createCache("characterCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, MutableMap::class.java,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())).build())
    }

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS elysium_wallet (" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "character_id INTEGER," +
                            "currency_id INTEGER," +
                            "balance INTEGER," +
                            "FOREIGN KEY(character_id) REFERENCES elysium_character(id) ON DELETE CASCADE ON UPDATE CASCADE," +
                            "FOREIGN KEY(currency_id) REFERENCES elysium_currency(id) ON DELETE CASCADE ON UPDATE CASCADE" +
                            ")"
            ).use { statement ->
                statement.executeUpdate()
            }
        }
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "0.2.0")
        }
    }

    override fun insert(`object`: BukkitWallet): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO elysium_wallet(character_id, currency_id, balance) VALUES(?, ?, ?)",
                    RETURN_GENERATED_KEYS
            ).use { statement ->
                statement.setInt(1, `object`.character.id)
                statement.setInt(2, `object`.currency.id)
                statement.setInt(3, `object`.balance)
                statement.executeUpdate()
                val generatedKeys = statement.generatedKeys
                if (generatedKeys.next()) {
                    id = generatedKeys.getInt(1)
                    `object`.id = id
                    cache.put(id, `object`)
                    val currencyWallets = characterCache.get(`object`.character.id)?:mutableMapOf<Int, Int>()
                    (currencyWallets as MutableMap<Int, Int>).put(`object`.currency.id, `object`.id)
                    characterCache.put(`object`.character.id, currencyWallets)
                }
            }
        }
        return id
    }

    override fun update(`object`: BukkitWallet) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE elysium_wallet SET character_id = ?, currency_id = ?, balance = ? WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, `object`.character.id)
                statement.setInt(2, `object`.currency.id)
                statement.setInt(3, `object`.balance)
                statement.setInt(4, `object`.id)
                statement.executeUpdate()
                cache.put(`object`.id, `object`)
                val currencyWallets = characterCache.get(`object`.character.id)?:mutableMapOf<Int, Int>()
                (currencyWallets as MutableMap<Int, Int>).put(`object`.currency.id, `object`.id)
                characterCache.put(`object`.character.id, currencyWallets)
            }
        }
    }

    override fun get(id: Int): BukkitWallet? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            var wallet: BukkitWallet? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, character_id, currency_id, balance FROM elysium_wallet WHERE id = ? LIMIT 1"
                ).use { statement ->
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        wallet = BukkitWallet(
                                id = resultSet.getInt("id"),
                                character = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class.java).getCharacter(resultSet.getInt("character_id"))!!,
                                currency = plugin.core.serviceManager.getServiceProvider(ElysiumCurrencyProvider::class.java).getCurrency(resultSet.getInt("currency_id"))!!,
                                balance = resultSet.getInt("balance")
                        )
                        cache.put(id, wallet)
                    }
                }
            }
            return wallet
        }
    }

    fun get(character: ElysiumCharacter, currency: ElysiumCurrency): BukkitWallet {
        if (characterCache.containsKey(character.id)) {
            return get(characterCache[character.id][currency.id] as Int)!!
        } else {
            var wallet: BukkitWallet? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, character_id, currency_id, balance FROM elysium_wallet WHERE character_id = ? AND currency_id = ?"
                ).use { statement ->
                    statement.setInt(1, character.id)
                    statement.setInt(2, currency.id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val id = resultSet.getInt("id")
                        val characterId = resultSet.getInt("character_id")
                        val currencyId = resultSet.getInt("currency_id")
                        val balance = resultSet.getInt("balance")
                        wallet = BukkitWallet(
                                id = id,
                                character = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class.java).getCharacter(characterId)!!,
                                currency = plugin.core.serviceManager.getServiceProvider(ElysiumCurrencyProvider::class.java).getCurrency(currencyId)!!,
                                balance = balance
                        )
                        val finalWallet = wallet!!
                        cache.put(id, finalWallet)
                        val characterWallets: MutableMap<Int, Int>
                        if (characterCache.containsKey(characterId)) {
                            characterWallets = characterCache[characterId] as MutableMap<Int, Int>
                        } else {
                            characterWallets = HashMap<Int, Int>()
                        }
                        characterWallets.put(currencyId, finalWallet.id)
                        characterCache.put(characterId, characterWallets)
                    }
                }
            }
            if (wallet == null) {
                val finalWallet = BukkitWallet(
                        character = character,
                        currency = currency,
                        balance = currency.defaultAmount
                )
                insert(finalWallet)
                wallet = finalWallet
            }
            val finalWallet = wallet!!
            return finalWallet
        }
    }

    fun getTop(amount: Int = 5, currency: ElysiumCurrency): List<ElysiumCharacter> {
        val top = ArrayList<BukkitWallet>()
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id FROM elysium_wallet WHERE currency_id = ? ORDER BY balance LIMIT ?"
            ).use { statement ->
                statement.setInt(1, currency.id)
                statement.setInt(2, amount)
                val resultSet = statement.executeQuery()
                while (resultSet.next()) {
                    val wallet = get(resultSet.getInt("id"))
                    if (wallet != null) {
                        top.add(wallet)
                    }
                }
            }
        }
        return top.map { wallet -> wallet.character }
    }

    override fun delete(`object`: BukkitWallet) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM elysium_wallet WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, `object`.id)
                statement.executeUpdate()
                cache.remove(`object`.id)
                val characterId = `object`.character.id
                characterCache[characterId].remove(`object`.currency.id)
                if (characterCache[characterId].isEmpty()) {
                    characterCache.remove(characterId)
                }
            }
        }
    }

}