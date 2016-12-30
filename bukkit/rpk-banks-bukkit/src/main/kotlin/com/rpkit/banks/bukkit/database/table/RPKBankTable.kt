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

package com.rpkit.banks.bukkit.database.table

import com.rpkit.banks.bukkit.RPKBanksBukkit
import com.rpkit.banks.bukkit.bank.RPKBank
import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.database.use
import com.rpkit.economy.bukkit.currency.RPKCurrency
import com.rpkit.economy.bukkit.currency.RPKCurrencyProvider
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.sql.Statement.RETURN_GENERATED_KEYS
import java.util.*

/**
 * Represents the bank table.
 */
class RPKBankTable: Table<RPKBank> {

    private val plugin: RPKBanksBukkit
    private val cacheManager: CacheManager
    private val cache: Cache<Int, RPKBank>
    private val characterCache: Cache<Int, MutableMap<*, *>>

    constructor(database: Database, plugin: RPKBanksBukkit): super(database, RPKBank::class.java) {
        this.plugin = plugin;
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
        cache = cacheManager.createCache("cache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKBank::class.java,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())).build())
        characterCache = cacheManager.createCache("characterCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, MutableMap::class.java,
                        ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())).build())
    }

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS rpkit_bank(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "character_id INTEGER," +
                            "currency_id INTEGER," +
                            "balance INTEGER" +
                    ")"
            ).use { statement ->
                statement.executeUpdate()
            }
        }

    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "0.2.0")
        }
    }

    override fun insert(entity: RPKBank): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO rpkit_bank(character_id, currency_id, balance) VALUES(?, ?, ?)",
                    RETURN_GENERATED_KEYS
            ).use { statement ->
                statement.setInt(1, entity.character.id)
                statement.setInt(2, entity.currency.id)
                statement.setInt(3, entity.balance)
                statement.executeUpdate()
                val generatedKeys = statement.generatedKeys
                if (generatedKeys.next()) {
                    id = generatedKeys.getInt(1)
                    entity.id = id
                    cache.put(id, entity)
                    val currencyBanks = characterCache.get(entity.character.id)?:mutableMapOf<Int, Int>()
                    @Suppress("UNCHECKED_CAST") (currencyBanks as MutableMap<Int, Int>).put(entity.currency.id, entity.id)
                    characterCache.put(entity.character.id, currencyBanks)
                }
            }
        }
        return id
    }

    override fun update(entity: RPKBank) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE rpkit_bank SET character_id = ?, currency_id = ?, balance = ? WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.character.id)
                statement.setInt(2, entity.currency.id)
                statement.setInt(3, entity.balance)
                statement.setInt(4, entity.id)
                statement.executeUpdate()
                cache.put(entity.id, entity)
                val currencyBanks = characterCache.get(entity.character.id)?:mutableMapOf<Int, Int>()
                @Suppress("UNCHECKED_CAST") (currencyBanks as MutableMap<Int, Int>).put(entity.currency.id, entity.id)
                characterCache.put(entity.character.id, currencyBanks)
            }
        }
    }

    override fun get(id: Int): RPKBank? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            var bank: RPKBank? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, character_id, currency_id, balance FROM rpkit_bank WHERE id = ? LIMIT 1"
                ).use { statement ->
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        bank = RPKBank(
                                id = resultSet.getInt("id"),
                                character = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class).getCharacter(resultSet.getInt("character_id"))!!,
                                currency = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class).getCurrency(resultSet.getInt("currency_id"))!!,
                                balance = resultSet.getInt("balance")
                        )
                        cache.put(id, bank)
                    }
                }
            }
            return bank
        }
    }

    /**
     * Gets the bank account for the given character in the given currency.
     * If no account exists, one will be created.
     *
     * @param character The character to get the account for
     * @param currency The currency which the account should be in
     * @return The account of the character in the currency
     */
    fun get(character: RPKCharacter, currency: RPKCurrency): RPKBank {
        if (characterCache.containsKey(character.id)) {
            return get(characterCache[character.id][currency.id] as Int)!!
        } else {
            var bank: RPKBank? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, character_id, currency_id, balance FROM rpkit_bank WHERE character_id = ? AND currency_id = ?"
                ).use { statement ->
                    statement.setInt(1, character.id)
                    statement.setInt(2, currency.id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val id = resultSet.getInt("id")
                        val characterId = resultSet.getInt("character_id")
                        val currencyId = resultSet.getInt("currency_id")
                        val balance = resultSet.getInt("balance")
                        bank = RPKBank(
                                id = id,
                                character = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class).getCharacter(characterId)!!,
                                currency = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class).getCurrency(currencyId)!!,
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
                val finalBank = RPKBank(
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

    /**
     * Gets the characters with the highest balance in the given currency.
     *
     * @param amount The amount of characters to retrieve
     * @param currency The currency to
     * @return A list of characters with the highest balance in the given currency
     */
    fun getTop(amount: Int = 5, currency: RPKCurrency): List<RPKCharacter> {
        val top = ArrayList<RPKBank>()
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id FROM rpkit_bank WHERE currency_id = ? ORDER BY balance LIMIT ?"
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

    override fun delete(entity: RPKBank) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM rpkit_bank WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
                cache.remove(entity.id)
                val characterId = entity.character.id
                characterCache[characterId].remove(entity.currency.id)
                if (characterCache[characterId].isEmpty()) {
                    characterCache.remove(characterId)
                }
            }
        }
    }

}