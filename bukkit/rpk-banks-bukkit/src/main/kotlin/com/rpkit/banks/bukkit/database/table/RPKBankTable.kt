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
import com.rpkit.banks.bukkit.database.jooq.rpkit.Tables.RPKIT_BANK
import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.economy.bukkit.currency.RPKCurrency
import com.rpkit.economy.bukkit.currency.RPKCurrencyProvider
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.SQLDialect
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType
import org.jooq.util.sqlite.SQLiteDataType

/**
 * Represents the bank table.
 */
class RPKBankTable(database: Database, private val plugin: RPKBanksBukkit): Table<RPKBank>(database, RPKBank::class) {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_bank.id.enabled")) {
        database.cacheManager.createCache("rpk-banks-bukkit.rpkit_bank.id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKBank::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_bank.id.size"))).build())
    } else {
        null
    }

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_BANK)
                .column(RPKIT_BANK.ID, if (database.dialect == SQLDialect.SQLITE) SQLiteDataType.INTEGER.identity(true) else SQLDataType.INTEGER.identity(true))
                .column(RPKIT_BANK.CHARACTER_ID, SQLDataType.INTEGER)
                .column(RPKIT_BANK.CURRENCY_ID, SQLDataType.INTEGER)
                .column(RPKIT_BANK.BALANCE, SQLDataType.INTEGER)
                .constraints(
                        constraint("pk_rpkit_bank").primaryKey(RPKIT_BANK.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "0.2.0")
        }
    }

    override fun insert(entity: RPKBank): Int {
        database.create
                .insertInto(
                        RPKIT_BANK,
                        RPKIT_BANK.CHARACTER_ID,
                        RPKIT_BANK.CURRENCY_ID,
                        RPKIT_BANK.BALANCE
                )
                .values(
                        entity.character.id,
                        entity.currency.id,
                        entity.balance
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
        return id
    }

    override fun update(entity: RPKBank) {
        database.create
                .update(RPKIT_BANK)
                .set(RPKIT_BANK.CHARACTER_ID, entity.character.id)
                .set(RPKIT_BANK.CURRENCY_ID, entity.currency.id)
                .set(RPKIT_BANK.BALANCE, entity.balance)
                .where(RPKIT_BANK.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
    }

    override fun get(id: Int): RPKBank? {
        if (cache?.containsKey(id) == true) {
            return cache.get(id)
        } else {
            val result = database.create
                    .select(
                            RPKIT_BANK.CHARACTER_ID,
                            RPKIT_BANK.CURRENCY_ID,
                            RPKIT_BANK.BALANCE
                    )
                    .from(RPKIT_BANK)
                    .where(RPKIT_BANK.ID.eq(id))
                    .fetchOne() ?: return null
            val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
            val characterId = result.get(RPKIT_BANK.CHARACTER_ID)
            val character = characterProvider.getCharacter(characterId)
            val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
            val currencyId = result.get(RPKIT_BANK.CURRENCY_ID)
            val currency = currencyProvider.getCurrency(currencyId)
            if (character != null && currency != null) {
                val bank = RPKBank(
                        id,
                        character,
                        currency,
                        result.get(RPKIT_BANK.BALANCE)
                )
                cache?.put(id, bank)
                return bank
            } else {
                database.create
                        .deleteFrom(RPKIT_BANK)
                        .where(RPKIT_BANK.ID.eq(id))
                        .execute()
                return null
            }
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
        val result = database.create
                .select(RPKIT_BANK.ID)
                .from(RPKIT_BANK)
                .where(RPKIT_BANK.CHARACTER_ID.eq(character.id))
                .and(RPKIT_BANK.CURRENCY_ID.eq(currency.id))
                .fetchOne()
        var bank = if (result == null) null else get(result.get(RPKIT_BANK.ID))
        if (bank == null) {
            bank = RPKBank(
                    character = character,
                    currency = currency,
                    balance = 0
            )
            insert(bank)
        }
        return bank
    }

    /**
     * Gets the characters with the highest balance in the given currency.
     *
     * @param amount The amount of characters to retrieve
     * @param currency The currency to
     * @return A list of characters with the highest balance in the given currency
     */
    fun getTop(amount: Int = 5, currency: RPKCurrency): List<RPKCharacter> {
        val results = database.create
                .select(RPKIT_BANK.ID)
                .from(RPKIT_BANK)
                .where(RPKIT_BANK.CURRENCY_ID.eq(currency.id))
                .orderBy(RPKIT_BANK.BALANCE.desc())
                .limit(amount)
                .fetch()
        return results
                .map { result ->
                    get(result.get(RPKIT_BANK.ID))
                }
                .filterNotNull()
                .map { bank ->
                    bank.character
                }
    }

    override fun delete(entity: RPKBank) {
        database.create
                .deleteFrom(RPKIT_BANK)
                .where(RPKIT_BANK.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
    }

}