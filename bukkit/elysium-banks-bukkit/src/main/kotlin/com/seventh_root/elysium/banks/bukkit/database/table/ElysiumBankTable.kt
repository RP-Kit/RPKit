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

package com.seventh_root.elysium.banks.bukkit.database.table

import com.seventh_root.elysium.banks.bukkit.ElysiumBanksBukkit
import com.seventh_root.elysium.banks.bukkit.bank.ElysiumBank
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacterProvider
import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.database.Table
import com.seventh_root.elysium.core.database.use
import com.seventh_root.elysium.economy.bukkit.currency.ElysiumCurrency
import com.seventh_root.elysium.economy.bukkit.currency.ElysiumCurrencyProvider
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.sql.Statement.RETURN_GENERATED_KEYS
import java.util.*


class ElysiumBankTable: Table<ElysiumBank> {

    private val plugin: ElysiumBanksBukkit
    private val cacheManager: CacheManager
    private val cache: Cache<Int, ElysiumBank>
    private val characterCache: Cache<Int, MutableMap<*, *>>

    constructor(database: Database, plugin: ElysiumBanksBukkit): super(database, ElysiumBank::class.java) {
        this.plugin = plugin;
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
        cache = cacheManager.createCache("cache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, ElysiumBank::class.java,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())).build())
        characterCache = cacheManager.createCache("characterCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, MutableMap::class.java,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())).build())
    }

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS elysium_bank(" +
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

    override fun insert(`object`: ElysiumBank): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO elysium_bank(character_id, currency_id, balance) VALUES(?, ?, ?)",
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
                    val currencyBanks = characterCache.get(`object`.character.id)?:mutableMapOf<Int, Int>()
                    (currencyBanks as MutableMap<Int, Int>).put(`object`.currency.id, `object`.id)
                    characterCache.put(`object`.character.id, currencyBanks)
                }
            }
        }
        return id
    }

    override fun update(`object`: ElysiumBank) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE elysium_bank SET character_id = ?, currency_id = ?, balance = ? WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, `object`.character.id)
                statement.setInt(2, `object`.currency.id)
                statement.setInt(3, `object`.balance)
                statement.setInt(4, `object`.id)
                statement.executeUpdate()
                cache.put(`object`.id, `object`)
                val currencyBanks = characterCache.get(`object`.character.id)?:mutableMapOf<Int, Int>()
                (currencyBanks as MutableMap<Int, Int>).put(`object`.currency.id, `object`.id)
                characterCache.put(`object`.character.id, currencyBanks)
            }
        }
    }

    override fun get(id: Int): ElysiumBank? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            var bank: ElysiumBank? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, character_id, currency_id, balance FROM elysium_bank WHERE id = ? LIMIT 1"
                ).use { statement ->
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        bank = ElysiumBank(
                                id = resultSet.getInt("id"),
                                character = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class).getCharacter(resultSet.getInt("character_id"))!!,
                                currency = plugin.core.serviceManager.getServiceProvider(ElysiumCurrencyProvider::class).getCurrency(resultSet.getInt("currency_id"))!!,
                                balance = resultSet.getInt("balance")
                        )
                        cache.put(id, bank)
                    }
                }
            }
            return bank
        }
    }

    fun get(character: ElysiumCharacter, currency: ElysiumCurrency): ElysiumBank {
        if (characterCache.containsKey(character.id)) {
            return get(characterCache[character.id][currency.id] as Int)!!
        } else {
            var bank: ElysiumBank? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, character_id, currency_id, balance FROM elysium_bank WHERE character_id = ? AND currency_id = ?"
                ).use { statement ->
                    statement.setInt(1, character.id)
                    statement.setInt(2, currency.id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val id = resultSet.getInt("id")
                        val characterId = resultSet.getInt("character_id")
                        val currencyId = resultSet.getInt("currency_id")
                        val balance = resultSet.getInt("balance")
                        bank = ElysiumBank(
                                id = id,
                                character = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class).getCharacter(characterId)!!,
                                currency = plugin.core.serviceManager.getServiceProvider(ElysiumCurrencyProvider::class).getCurrency(currencyId)!!,
                                balance = balance
                        )
                        val finalBank = bank!!
                        cache.put(id, finalBank)
                        val characterBanks: MutableMap<Int, Int>
                        if (characterCache.containsKey(characterId)) {
                            characterBanks = characterCache[characterId] as MutableMap<Int, Int>
                        } else {
                            characterBanks = HashMap<Int, Int>()
                        }
                        characterBanks.put(currencyId, finalBank.id)
                        characterCache.put(characterId, characterBanks)
                    }
                }
            }
            if (bank == null) {
                val finalBank = ElysiumBank(
                        character = character,
                        currency = currency,
                        balance = 0
                )
                insert(finalBank)
                bank = finalBank
            }
            val finalBank = bank!!
            return finalBank
        }
    }

    fun getTop(amount: Int = 5, currency: ElysiumCurrency): List<ElysiumCharacter> {
        val top = ArrayList<ElysiumBank>()
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id FROM elysium_bank WHERE currency_id = ? ORDER BY balance LIMIT ?"
            ).use { statement ->
                statement.setInt(1, currency.id)
                statement.setInt(2, amount)
                val resultSet = statement.executeQuery()
                while (resultSet.next()) {
                    val bank = get(resultSet.getInt("id"))
                    if (bank != null) {
                        top.add(bank)
                    }
                }
            }
        }
        return top.map { bank -> bank.character }
    }

    override fun delete(`object`: ElysiumBank) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM elysium_bank WHERE id = ?"
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