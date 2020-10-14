/*
 * Copyright 2020 Ren Binden
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

package com.rpkit.payments.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.economy.bukkit.currency.RPKCurrencyService
import com.rpkit.payments.bukkit.RPKPaymentsBukkit
import com.rpkit.payments.bukkit.database.jooq.Tables.RPKIT_PAYMENT_GROUP
import com.rpkit.payments.bukkit.group.RPKPaymentGroup
import com.rpkit.payments.bukkit.group.RPKPaymentGroupImpl
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.time.Duration
import java.time.temporal.ChronoUnit.MILLIS

/**
 * Represents payment group table.
 */
class RPKPaymentGroupTable(
        private val database: Database,
        private val plugin: RPKPaymentsBukkit
) : Table {

    val cache = if (plugin.config.getBoolean("caching.rpkit_payment_group.id.enabled")) {
        database.cacheManager.createCache("rpk-payments-bukkit.rpkit_payment_group.id", CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Int::class.javaObjectType, RPKPaymentGroup::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_payment_group.id.size"))))
    } else {
        null
    }

    fun insert(entity: RPKPaymentGroup) {
        database.create
                .insertInto(
                        RPKIT_PAYMENT_GROUP,
                        RPKIT_PAYMENT_GROUP.NAME,
                        RPKIT_PAYMENT_GROUP.AMOUNT,
                        RPKIT_PAYMENT_GROUP.CURRENCY_ID,
                        RPKIT_PAYMENT_GROUP.INTERVAL,
                        RPKIT_PAYMENT_GROUP.LAST_PAYMENT_TIME,
                        RPKIT_PAYMENT_GROUP.BALANCE
                )
                .values(
                        entity.name,
                        entity.amount,
                        entity.currency?.id,
                        entity.interval.toMillis(),
                        entity.lastPaymentTime,
                        entity.balance
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
    }

    fun update(entity: RPKPaymentGroup) {
        database.create
                .update(RPKIT_PAYMENT_GROUP)
                .set(RPKIT_PAYMENT_GROUP.NAME, entity.name)
                .set(RPKIT_PAYMENT_GROUP.AMOUNT, entity.amount)
                .set(RPKIT_PAYMENT_GROUP.CURRENCY_ID, entity.currency?.id)
                .set(RPKIT_PAYMENT_GROUP.INTERVAL, entity.interval.toMillis())
                .set(RPKIT_PAYMENT_GROUP.LAST_PAYMENT_TIME, entity.lastPaymentTime)
                .set(RPKIT_PAYMENT_GROUP.BALANCE, entity.balance)
                .where(RPKIT_PAYMENT_GROUP.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
    }

    operator fun get(id: Int): RPKPaymentGroup? {
        if (cache?.containsKey(id) == true) {
            return cache[id]
        } else {
            val result = database.create
                    .select(
                            RPKIT_PAYMENT_GROUP.NAME,
                            RPKIT_PAYMENT_GROUP.AMOUNT,
                            RPKIT_PAYMENT_GROUP.CURRENCY_ID,
                            RPKIT_PAYMENT_GROUP.INTERVAL,
                            RPKIT_PAYMENT_GROUP.LAST_PAYMENT_TIME,
                            RPKIT_PAYMENT_GROUP.BALANCE
                    )
                    .from(RPKIT_PAYMENT_GROUP)
                    .where(RPKIT_PAYMENT_GROUP.ID.eq(id))
                    .fetchOne()
            val currencyService = Services[RPKCurrencyService::class] ?: return null
            val currencyId = result.get(RPKIT_PAYMENT_GROUP.CURRENCY_ID)
            val currency = if (currencyId == null) null else currencyService.getCurrency(currencyId)
            val paymentGroup = RPKPaymentGroupImpl(
                    plugin,
                    id,
                    result.get(RPKIT_PAYMENT_GROUP.NAME),
                    result.get(RPKIT_PAYMENT_GROUP.AMOUNT),
                    currency,
                    Duration.of(result.get(RPKIT_PAYMENT_GROUP.INTERVAL), MILLIS),
                    result.get(RPKIT_PAYMENT_GROUP.LAST_PAYMENT_TIME),
                    result.get(RPKIT_PAYMENT_GROUP.BALANCE)
            )
            cache?.put(id, paymentGroup)
            return paymentGroup
        }
    }

    fun get(name: String): RPKPaymentGroup? {
        val result = database.create
                .select(RPKIT_PAYMENT_GROUP.ID)
                .from(RPKIT_PAYMENT_GROUP)
                .where(RPKIT_PAYMENT_GROUP.NAME.eq(name))
                .fetchOne() ?: return null
        return get(result.get(RPKIT_PAYMENT_GROUP.ID))
    }

    fun getAll(): List<RPKPaymentGroup> {
        val results = database.create
                .select(RPKIT_PAYMENT_GROUP.ID)
                .from(RPKIT_PAYMENT_GROUP)
                .fetch()
        return results.map { result -> get(result.get(RPKIT_PAYMENT_GROUP.ID)) }
                .filterNotNull()
    }

    fun delete(entity: RPKPaymentGroup) {
        database.create
                .deleteFrom(RPKIT_PAYMENT_GROUP)
                .where(RPKIT_PAYMENT_GROUP.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
    }

}