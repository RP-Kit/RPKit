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

package com.rpkit.payments.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.economy.bukkit.currency.RPKCurrencyName
import com.rpkit.economy.bukkit.currency.RPKCurrencyService
import com.rpkit.payments.bukkit.RPKPaymentsBukkit
import com.rpkit.payments.bukkit.database.create
import com.rpkit.payments.bukkit.database.jooq.Tables.RPKIT_PAYMENT_GROUP
import com.rpkit.payments.bukkit.group.RPKPaymentGroup
import com.rpkit.payments.bukkit.group.RPKPaymentGroupId
import com.rpkit.payments.bukkit.group.RPKPaymentGroupImpl
import com.rpkit.payments.bukkit.group.RPKPaymentGroupName
import java.time.Duration
import java.time.temporal.ChronoUnit.MILLIS

/**
 * Represents payment group table.
 */
class RPKPaymentGroupTable(
        private val database: Database,
        private val plugin: RPKPaymentsBukkit
) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_payment_group.id.enabled")) {
        database.cacheManager.createCache(
            "rpk-payments-bukkit.rpkit_payment_group.id",
            Int::class.javaObjectType,
            RPKPaymentGroup::class.java,
            plugin.config.getLong("caching.rpkit_payment_group.id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKPaymentGroup) {
        database.create
                .insertInto(
                        RPKIT_PAYMENT_GROUP,
                        RPKIT_PAYMENT_GROUP.NAME,
                        RPKIT_PAYMENT_GROUP.AMOUNT,
                        RPKIT_PAYMENT_GROUP.CURRENCY_NAME,
                        RPKIT_PAYMENT_GROUP.INTERVAL,
                        RPKIT_PAYMENT_GROUP.LAST_PAYMENT_TIME,
                        RPKIT_PAYMENT_GROUP.BALANCE
                )
                .values(
                        entity.name.value,
                        entity.amount,
                        entity.currency?.name?.value,
                        entity.interval.toMillis(),
                        entity.lastPaymentTime,
                        entity.balance
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = RPKPaymentGroupId(id)
        cache?.set(id, entity)
    }

    fun update(entity: RPKPaymentGroup) {
        val id = entity.id ?: return
        database.create
                .update(RPKIT_PAYMENT_GROUP)
                .set(RPKIT_PAYMENT_GROUP.NAME, entity.name.value)
                .set(RPKIT_PAYMENT_GROUP.AMOUNT, entity.amount)
                .set(RPKIT_PAYMENT_GROUP.CURRENCY_NAME, entity.currency?.name?.value)
                .set(RPKIT_PAYMENT_GROUP.INTERVAL, entity.interval.toMillis())
                .set(RPKIT_PAYMENT_GROUP.LAST_PAYMENT_TIME, entity.lastPaymentTime)
                .set(RPKIT_PAYMENT_GROUP.BALANCE, entity.balance)
                .where(RPKIT_PAYMENT_GROUP.ID.eq(id.value))
                .execute()
        cache?.set(id.value, entity)
    }

    operator fun get(id: RPKPaymentGroupId): RPKPaymentGroup? {
        if (cache?.containsKey(id.value) == true) {
            return cache[id.value]
        } else {
            val result = database.create
                    .select(
                            RPKIT_PAYMENT_GROUP.NAME,
                            RPKIT_PAYMENT_GROUP.AMOUNT,
                            RPKIT_PAYMENT_GROUP.CURRENCY_NAME,
                            RPKIT_PAYMENT_GROUP.INTERVAL,
                            RPKIT_PAYMENT_GROUP.LAST_PAYMENT_TIME,
                            RPKIT_PAYMENT_GROUP.BALANCE
                    )
                    .from(RPKIT_PAYMENT_GROUP)
                    .where(RPKIT_PAYMENT_GROUP.ID.eq(id.value))
                    .fetchOne()
            val currencyService = Services[RPKCurrencyService::class.java] ?: return null
            val currencyName = result.get(RPKIT_PAYMENT_GROUP.CURRENCY_NAME)
            val currency = if (currencyName == null) null else currencyService.getCurrency(RPKCurrencyName(currencyName))
            val paymentGroup = RPKPaymentGroupImpl(
                    plugin,
                    id,
                    RPKPaymentGroupName(result.get(RPKIT_PAYMENT_GROUP.NAME)),
                    result.get(RPKIT_PAYMENT_GROUP.AMOUNT),
                    currency,
                    Duration.of(result.get(RPKIT_PAYMENT_GROUP.INTERVAL), MILLIS),
                    result.get(RPKIT_PAYMENT_GROUP.LAST_PAYMENT_TIME),
                    result.get(RPKIT_PAYMENT_GROUP.BALANCE)
            )
            cache?.set(id.value, paymentGroup)
            return paymentGroup
        }
    }

    fun get(name: RPKPaymentGroupName): RPKPaymentGroup? {
        val result = database.create
                .select(RPKIT_PAYMENT_GROUP.ID)
                .from(RPKIT_PAYMENT_GROUP)
                .where(RPKIT_PAYMENT_GROUP.NAME.eq(name.value))
                .fetchOne() ?: return null
        return get(RPKPaymentGroupId(result.get(RPKIT_PAYMENT_GROUP.ID)))
    }

    fun getAll(): List<RPKPaymentGroup> {
        val results = database.create
                .select(RPKIT_PAYMENT_GROUP.ID)
                .from(RPKIT_PAYMENT_GROUP)
                .fetch()
        return results.map { result -> get(RPKPaymentGroupId(result.get(RPKIT_PAYMENT_GROUP.ID))) }
                .filterNotNull()
    }

    fun delete(entity: RPKPaymentGroup) {
        val id = entity.id ?: return
        database.create
                .deleteFrom(RPKIT_PAYMENT_GROUP)
                .where(RPKIT_PAYMENT_GROUP.ID.eq(id.value))
                .execute()
        cache?.remove(id.value)
    }

}