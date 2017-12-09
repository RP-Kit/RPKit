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

package com.rpkit.payments.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.economy.bukkit.currency.RPKCurrencyProvider
import com.rpkit.payments.bukkit.RPKPaymentsBukkit
import com.rpkit.payments.bukkit.database.jooq.rpkit.Tables.RPKIT_PAYMENT_GROUP
import com.rpkit.payments.bukkit.group.RPKPaymentGroup
import com.rpkit.payments.bukkit.group.RPKPaymentGroupImpl
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.SQLDialect
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType
import org.jooq.util.sqlite.SQLiteDataType
import java.sql.Timestamp

/**
 * Represents payment group table.
 */
class RPKPaymentGroupTable(database: Database, private val plugin: RPKPaymentsBukkit): Table<RPKPaymentGroup>(database, RPKPaymentGroup::class) {

    val cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
    val cache = cacheManager.createCache("cache", CacheConfigurationBuilder
            .newCacheConfigurationBuilder(Int::class.javaObjectType, RPKPaymentGroup::class.java, ResourcePoolsBuilder.heap(20L)))

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_PAYMENT_GROUP)
                .column(RPKIT_PAYMENT_GROUP.ID, if (database.dialect == SQLDialect.SQLITE) SQLiteDataType.INTEGER.identity(true) else SQLDataType.INTEGER.identity(true))
                .column(RPKIT_PAYMENT_GROUP.NAME, SQLDataType.VARCHAR(256))
                .column(RPKIT_PAYMENT_GROUP.AMOUNT, SQLDataType.INTEGER)
                .column(RPKIT_PAYMENT_GROUP.CURRENCY_ID, SQLDataType.INTEGER.nullable(true))
                .column(RPKIT_PAYMENT_GROUP.INTERVAL, SQLDataType.BIGINT)
                .column(RPKIT_PAYMENT_GROUP.LAST_PAYMENT_TIME, SQLDataType.TIMESTAMP)
                .column(RPKIT_PAYMENT_GROUP.BALANCE, SQLDataType.INTEGER)
                .constraints(
                        constraint("pk_rpkit_payment_group").primaryKey(RPKIT_PAYMENT_GROUP.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "0.4.0")
        }
    }

    override fun insert(entity: RPKPaymentGroup): Int {
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
                        entity.interval,
                        Timestamp(entity.lastPaymentTime),
                        entity.balance
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache.put(id, entity)
        return id
    }

    override fun update(entity: RPKPaymentGroup) {
        database.create
                .update(RPKIT_PAYMENT_GROUP)
                .set(RPKIT_PAYMENT_GROUP.NAME, entity.name)
                .set(RPKIT_PAYMENT_GROUP.AMOUNT, entity.amount)
                .set(RPKIT_PAYMENT_GROUP.CURRENCY_ID, entity.currency?.id)
                .set(RPKIT_PAYMENT_GROUP.INTERVAL, entity.interval)
                .set(RPKIT_PAYMENT_GROUP.LAST_PAYMENT_TIME, Timestamp(entity.lastPaymentTime))
                .set(RPKIT_PAYMENT_GROUP.BALANCE, entity.balance)
                .where(RPKIT_PAYMENT_GROUP.ID.eq(entity.id))
                .execute()
        cache.put(entity.id, entity)
    }

    override fun get(id: Int): RPKPaymentGroup? {
        if (cache.containsKey(id)) {
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
            val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
            val currencyId = result.get(RPKIT_PAYMENT_GROUP.CURRENCY_ID)
            val currency = if (currencyId == null) null else currencyProvider.getCurrency(currencyId)
            val paymentGroup = RPKPaymentGroupImpl(
                    plugin,
                    id,
                    result.get(RPKIT_PAYMENT_GROUP.NAME),
                    result.get(RPKIT_PAYMENT_GROUP.AMOUNT),
                    currency,
                    result.get(RPKIT_PAYMENT_GROUP.INTERVAL),
                    result.get(RPKIT_PAYMENT_GROUP.LAST_PAYMENT_TIME).time,
                    result.get(RPKIT_PAYMENT_GROUP.BALANCE)
            )
            cache.put(id, paymentGroup)
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

    override fun delete(entity: RPKPaymentGroup) {
        database.create
                .deleteFrom(RPKIT_PAYMENT_GROUP)
                .where(RPKIT_PAYMENT_GROUP.ID.eq(entity.id))
                .execute()
        cache.remove(entity.id)
    }

}