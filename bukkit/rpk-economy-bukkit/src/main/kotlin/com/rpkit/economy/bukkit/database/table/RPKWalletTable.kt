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

package com.rpkit.economy.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.economy.bukkit.RPKEconomyBukkit
import com.rpkit.economy.bukkit.currency.RPKCurrency
import com.rpkit.economy.bukkit.currency.RPKCurrencyProvider
import com.rpkit.economy.bukkit.database.jooq.rpkit.Tables.RPKIT_WALLET
import com.rpkit.economy.bukkit.wallet.RPKWallet
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.SQLDialect
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType
import org.jooq.util.sqlite.SQLiteDataType

/**
 * Represents the wallet table.
 */
class RPKWalletTable(database: Database, private val plugin: RPKEconomyBukkit) : Table<RPKWallet>(database, RPKWallet::class.java) {

    private val cacheManager: CacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
    private val cache: Cache<Int, RPKWallet> = cacheManager.createCache("cache",
            CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKWallet::class.java,
                    ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())).build())

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_WALLET)
                .column(RPKIT_WALLET.ID, if (database.dialect == SQLDialect.SQLITE) SQLiteDataType.INTEGER.identity(true) else SQLDataType.INTEGER.identity(true))
                .column(RPKIT_WALLET.CHARACTER_ID, SQLDataType.INTEGER)
                .column(RPKIT_WALLET.CURRENCY_ID, SQLDataType.INTEGER)
                .column(RPKIT_WALLET.BALANCE, SQLDataType.INTEGER)
                .constraints(
                        constraint("pk_rpkit_wallet").primaryKey(RPKIT_WALLET.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "0.2.0")
        }
    }

    override fun insert(entity: RPKWallet): Int {
        database.create
                .insertInto(
                        RPKIT_WALLET,
                        RPKIT_WALLET.CHARACTER_ID,
                        RPKIT_WALLET.CURRENCY_ID,
                        RPKIT_WALLET.BALANCE
                )
                .values(
                        entity.character.id,
                        entity.currency.id,
                        entity.balance
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache.put(id, entity)
        return id
    }

    override fun update(entity: RPKWallet) {
        database.create
                .update(RPKIT_WALLET)
                .set(RPKIT_WALLET.CHARACTER_ID, entity.character.id)
                .set(RPKIT_WALLET.CURRENCY_ID, entity.currency.id)
                .set(RPKIT_WALLET.BALANCE, entity.balance)
                .where(RPKIT_WALLET.ID.eq(entity.id))
                .execute()
        cache.put(entity.id, entity)
    }

    override fun get(id: Int): RPKWallet? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            val result = database.create
                    .select(
                            RPKIT_WALLET.CHARACTER_ID,
                            RPKIT_WALLET.CURRENCY_ID,
                            RPKIT_WALLET.BALANCE
                    )
                    .from(RPKIT_WALLET)
                    .where(RPKIT_WALLET.ID.eq(id))
                    .fetchOne() ?: return null
            val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
            val characterId = result.get(RPKIT_WALLET.CHARACTER_ID)
            val character = characterProvider.getCharacter(characterId)
            val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
            val currencyId = result.get(RPKIT_WALLET.CURRENCY_ID)
            val currency = currencyProvider.getCurrency(currencyId)
            if (character != null && currency != null) {
                val wallet = RPKWallet(
                        id,
                        character,
                        currency,
                        result.get(RPKIT_WALLET.BALANCE)
                )
                cache.put(id, wallet)
                return wallet
            } else {
                database.create
                        .deleteFrom(RPKIT_WALLET)
                        .where(RPKIT_WALLET.ID.eq(id))
                        .execute()
                return null
            }
        }
    }

    fun get(character: RPKCharacter, currency: RPKCurrency): RPKWallet {
        val result = database.create
                .select(RPKIT_WALLET.ID)
                .from(RPKIT_WALLET)
                .where(RPKIT_WALLET.CHARACTER_ID.eq(character.id))
                .and(RPKIT_WALLET.CURRENCY_ID.eq(currency.id))
                .fetchOne()
        if (result == null) {
            val wallet = RPKWallet(
                    character = character,
                    currency = currency,
                    balance = currency.defaultAmount
            )
            insert(wallet)
            return wallet
        }
        var wallet = get(result.get(RPKIT_WALLET.ID))
        if (wallet == null) {
            wallet = RPKWallet(
                    character = character,
                    currency = currency,
                    balance = currency.defaultAmount
            )
            insert(wallet)
        }
        return wallet
    }

    fun getTop(amount: Int = 5, currency: RPKCurrency): List<RPKCharacter> {
        val results = database.create
                .select(RPKIT_WALLET.ID)
                .from(RPKIT_WALLET)
                .where(RPKIT_WALLET.CURRENCY_ID.eq(currency.id))
                .orderBy(RPKIT_WALLET.BALANCE.desc())
                .limit(amount)
                .fetch()
        return results
                .map { result ->
                    get(result.get(RPKIT_WALLET.ID))
                }
                .filterNotNull()
                .map(RPKWallet::character)
    }

    override fun delete(entity: RPKWallet) {
        database.create
                .deleteFrom(RPKIT_WALLET)
                .where(RPKIT_WALLET.ID.eq(entity.id))
                .execute()
        cache.remove(entity.id)
    }

}