/*
 * Copyright 2021 Ren Binden
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
import com.rpkit.economy.bukkit.currency.RPKCurrencyId
import com.rpkit.economy.bukkit.currency.RPKCurrencyImpl
import com.rpkit.economy.bukkit.currency.RPKCurrencyName
import com.rpkit.economy.bukkit.database.create
import com.rpkit.economy.bukkit.database.jooq.Tables.RPKIT_CURRENCY
import org.bukkit.Material

/**
 * Represents the currency table.
 */
class RPKCurrencyTable(
        private val database: Database,
        plugin: RPKEconomyBukkit
) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_currency.id.enabled")) {
        database.cacheManager.createCache(
            "rpk-economy-bukkit.rpkit_currency.id",
            Int::class.javaObjectType,
            RPKCurrency::class.java,
            plugin.config.getLong("caching.rpkit_currency.id.size")
        )
    } else {
        null
    }

    private val nameCache = if (plugin.config.getBoolean("caching.rpkit_currency.name.enabled")) {
        database.cacheManager.createCache(
            "rpk-economy-bukkit.rpkit_currency.name",
            String::class.java,
            Int::class.javaObjectType,
            plugin.config.getLong("caching.rpkit_currency.name.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKCurrency) {
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
                        entity.name.value,
                        entity.nameSingular,
                        entity.namePlural,
                        entity.rate,
                        entity.defaultAmount,
                        entity.material.toString()
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = RPKCurrencyId(id)
        cache?.set(id, entity)
        nameCache?.set(entity.name.value, id)
    }

    fun update(entity: RPKCurrency) {
        val id = entity.id ?: return
        database.create
                .update(RPKIT_CURRENCY)
                .set(RPKIT_CURRENCY.NAME, entity.name.value)
                .set(RPKIT_CURRENCY.NAME_SINGULAR, entity.nameSingular)
                .set(RPKIT_CURRENCY.NAME_PLURAL, entity.namePlural)
                .set(RPKIT_CURRENCY.RATE, entity.rate)
                .set(RPKIT_CURRENCY.DEFAULT_AMOUNT, entity.defaultAmount)
                .set(RPKIT_CURRENCY.MATERIAL, entity.material.toString())
                .where(RPKIT_CURRENCY.ID.eq(id.value))
                .execute()
        cache?.set(id.value, entity)
        nameCache?.set(entity.name.value, id.value)
    }

    operator fun get(id: Int): RPKCurrency? {
        if (cache?.containsKey(id) == true) {
            return cache[id]
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
                    RPKCurrencyId(id),
                    RPKCurrencyName(result.get(RPKIT_CURRENCY.NAME)),
                    result.get(RPKIT_CURRENCY.NAME_SINGULAR),
                    result.get(RPKIT_CURRENCY.NAME_PLURAL),
                    result.get(RPKIT_CURRENCY.RATE),
                    result.get(RPKIT_CURRENCY.DEFAULT_AMOUNT),
                    Material.getMaterial(result.get(RPKIT_CURRENCY.MATERIAL))
                            ?: Material.getMaterial(result.get(RPKIT_CURRENCY.MATERIAL), true)
                            ?: Material.AIR
            )
            cache?.set(id, currency)
            nameCache?.set(currency.name.value, id)
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
    operator fun get(name: String): RPKCurrency? {
        if (nameCache?.containsKey(name) == true) {
            return get(nameCache[name] as Int)
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

    fun delete(entity: RPKCurrency) {
        val id = entity.id ?: return
        database.create
                .deleteFrom(RPKIT_CURRENCY)
                .where(RPKIT_CURRENCY.ID.eq(id.value))
                .execute()
        cache?.remove(id.value)
    }

}