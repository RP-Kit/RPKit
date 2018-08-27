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

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.economy.bukkit.RPKEconomyBukkit
import com.rpkit.economy.bukkit.currency.RPKCurrency
import com.rpkit.economy.bukkit.currency.RPKCurrencyImpl
import com.rpkit.economy.bukkit.database.jooq.rpkit.Tables.RPKIT_CURRENCY
import org.bukkit.Material
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.SQLDialect
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType
import org.jooq.util.sqlite.SQLiteDataType

/**
 * Represents the currency table.
 */
class RPKCurrencyTable(database: Database, private val plugin: RPKEconomyBukkit): Table<RPKCurrency>(database, RPKCurrency::class) {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_currency.id.enabled")) {
        database.cacheManager.createCache("rpk-economy-bukkit.rpkit_currency.id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKCurrency::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_currency.id.size"))).build())
    } else {
        null
    }

    private val nameCache = if (plugin.config.getBoolean("caching.rpkit_currency.name.enabled")) {
        database.cacheManager.createCache("rpk-economy-bukkit.rpkit_currency.name",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String::class.java, Int::class.javaObjectType,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_currency.name.size"))).build())
    } else {
        null
    }

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_CURRENCY)
                .column(RPKIT_CURRENCY.ID, if (database.dialect == SQLDialect.SQLITE) SQLiteDataType.INTEGER.identity(true) else SQLDataType.INTEGER.identity(true))
                .column(RPKIT_CURRENCY.NAME, SQLDataType.VARCHAR(256))
                .column(RPKIT_CURRENCY.NAME_SINGULAR, SQLDataType.VARCHAR(256))
                .column(RPKIT_CURRENCY.NAME_PLURAL, SQLDataType.VARCHAR(256))
                .column(RPKIT_CURRENCY.RATE, SQLDataType.DOUBLE)
                .column(RPKIT_CURRENCY.DEFAULT_AMOUNT, SQLDataType.INTEGER)
                .column(RPKIT_CURRENCY.MATERIAL, SQLDataType.VARCHAR(256))
                .constraints(
                        constraint("pk_rpkit_currency").primaryKey(RPKIT_CURRENCY.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "0.2.0")
        }
    }

    override fun insert(entity: RPKCurrency): Int {
        database.create
                .insertInto(
                        RPKIT_CURRENCY,
                        RPKIT_CURRENCY.NAME,
                        RPKIT_CURRENCY.NAME_SINGULAR,
                        RPKIT_CURRENCY.NAME_PLURAL,
                        RPKIT_CURRENCY.RATE,
                        RPKIT_CURRENCY.DEFAULT_AMOUNT,
                        RPKIT_CURRENCY.MATERIAL
                )
                .values(
                        entity.name,
                        entity.nameSingular,
                        entity.namePlural,
                        entity.rate,
                        entity.defaultAmount,
                        entity.material.toString()
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
        nameCache?.put(entity.name, id)
        return id
    }

    override fun update(entity: RPKCurrency) {
        database.create
                .update(RPKIT_CURRENCY)
                .set(RPKIT_CURRENCY.NAME, entity.name)
                .set(RPKIT_CURRENCY.NAME_SINGULAR, entity.nameSingular)
                .set(RPKIT_CURRENCY.NAME_PLURAL, entity.namePlural)
                .set(RPKIT_CURRENCY.RATE, entity.rate)
                .set(RPKIT_CURRENCY.DEFAULT_AMOUNT, entity.defaultAmount)
                .set(RPKIT_CURRENCY.MATERIAL, entity.material.toString())
                .where(RPKIT_CURRENCY.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
        nameCache?.put(entity.name, entity.id)
    }

    override fun get(id: Int): RPKCurrency? {
        if (cache?.containsKey(id) == true) {
            return cache.get(id)
        } else {
            val result = database.create
                    .select(
                            RPKIT_CURRENCY.NAME,
                            RPKIT_CURRENCY.NAME_SINGULAR,
                            RPKIT_CURRENCY.NAME_PLURAL,
                            RPKIT_CURRENCY.RATE,
                            RPKIT_CURRENCY.DEFAULT_AMOUNT,
                            RPKIT_CURRENCY.MATERIAL
                    )
                    .from(RPKIT_CURRENCY)
                    .where(RPKIT_CURRENCY.ID.eq(id))
                    .fetchOne() ?: return null
            val currency = RPKCurrencyImpl(
                    id,
                    result.get(RPKIT_CURRENCY.NAME),
                    result.get(RPKIT_CURRENCY.NAME_SINGULAR),
                    result.get(RPKIT_CURRENCY.NAME_PLURAL),
                    result.get(RPKIT_CURRENCY.RATE),
                    result.get(RPKIT_CURRENCY.DEFAULT_AMOUNT),
                    Material.getMaterial(result.get(RPKIT_CURRENCY.MATERIAL))
            )
            cache?.put(id, currency)
            nameCache?.put(currency.name, id)
            return currency
        }
    }

    /**
     * Gets a currency by name.
     * If no currency is found with the given name is found, null is returned.
     *
     * @param name The name
     * @return The currency, or null if no currency is found with the given name
     */
    fun get(name: String): RPKCurrency? {
        if (nameCache?.containsKey(name) == true) {
            return get(nameCache.get(name) as Int)
        } else {
            val result = database.create
                    .select(RPKIT_CURRENCY.ID)
                    .from(RPKIT_CURRENCY)
                    .where(RPKIT_CURRENCY.NAME.eq(name))
                    .fetchOne() ?: return null
            return get(result.get(RPKIT_CURRENCY.ID))
        }
    }

    /**
     * Gets all currencies contained in the table.
     *
     * @return A collection containing all currencies.
     */
    fun getAll(): Collection<RPKCurrency> {
        val results = database.create
                .select(RPKIT_CURRENCY.ID)
                .from(RPKIT_CURRENCY)
                .fetch()
        return results.map { result ->
            get(result.get(RPKIT_CURRENCY.ID))
        }.filterNotNull()
    }

    override fun delete(entity: RPKCurrency) {
        database.create
                .deleteFrom(RPKIT_CURRENCY)
                .where(RPKIT_CURRENCY.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
    }

}