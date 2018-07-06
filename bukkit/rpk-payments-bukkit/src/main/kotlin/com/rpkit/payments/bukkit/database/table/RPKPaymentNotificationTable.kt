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

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.payments.bukkit.RPKPaymentsBukkit
import com.rpkit.payments.bukkit.database.jooq.rpkit.Tables.RPKIT_PAYMENT_NOTIFICATION
import com.rpkit.payments.bukkit.group.RPKPaymentGroupProvider
import com.rpkit.payments.bukkit.notification.RPKPaymentNotification
import com.rpkit.payments.bukkit.notification.RPKPaymentNotificationImpl
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.SQLDialect
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType
import org.jooq.util.sqlite.SQLiteDataType
import java.sql.Date

/**
 * Represents payment notification table.
 */
class RPKPaymentNotificationTable(
        database: Database,
        private val plugin: RPKPaymentsBukkit
): Table<RPKPaymentNotification>(database, RPKPaymentNotification::class) {

    private val cache = database.cacheManager.createCache("rpk-payments-bukkit.rpkit_payment_notification.id", CacheConfigurationBuilder
            .newCacheConfigurationBuilder(Int::class.javaObjectType, RPKPaymentNotification::class.java,
                    ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())))

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_PAYMENT_NOTIFICATION)
                .column(RPKIT_PAYMENT_NOTIFICATION.ID, if (database.dialect == SQLDialect.SQLITE) SQLiteDataType.INTEGER.identity(true) else SQLDataType.INTEGER.identity(true))
                .column(RPKIT_PAYMENT_NOTIFICATION.GROUP_ID, SQLDataType.INTEGER)
                .column(RPKIT_PAYMENT_NOTIFICATION.TO_ID, SQLDataType.INTEGER)
                .column(RPKIT_PAYMENT_NOTIFICATION.CHARACTER_ID, SQLDataType.INTEGER)
                .column(RPKIT_PAYMENT_NOTIFICATION.DATE, SQLDataType.DATE)
                .column(RPKIT_PAYMENT_NOTIFICATION.TEXT, SQLDataType.VARCHAR(1024))
                .constraints(
                        constraint("pk_rpkit_payment_notification").primaryKey(RPKIT_PAYMENT_NOTIFICATION.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "0.4.0")
        }
    }

    override fun insert(entity: RPKPaymentNotification): Int {
        database.create
                .insertInto(
                        RPKIT_PAYMENT_NOTIFICATION,
                        RPKIT_PAYMENT_NOTIFICATION.GROUP_ID,
                        RPKIT_PAYMENT_NOTIFICATION.TO_ID,
                        RPKIT_PAYMENT_NOTIFICATION.CHARACTER_ID,
                        RPKIT_PAYMENT_NOTIFICATION.DATE,
                        RPKIT_PAYMENT_NOTIFICATION.TEXT
                )
                .values(
                        entity.group.id,
                        entity.to.id,
                        entity.character.id,
                        Date(entity.date),
                        entity.text
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache.put(id, entity)
        return id
    }

    override fun update(entity: RPKPaymentNotification) {
        database.create
                .update(RPKIT_PAYMENT_NOTIFICATION)
                .set(RPKIT_PAYMENT_NOTIFICATION.GROUP_ID, entity.group.id)
                .set(RPKIT_PAYMENT_NOTIFICATION.TO_ID, entity.to.id)
                .set(RPKIT_PAYMENT_NOTIFICATION.CHARACTER_ID, entity.character.id)
                .set(RPKIT_PAYMENT_NOTIFICATION.DATE, Date(entity.date))
                .set(RPKIT_PAYMENT_NOTIFICATION.TEXT, entity.text)
                .where(RPKIT_PAYMENT_NOTIFICATION.ID.eq(entity.id))
                .execute()
        cache.put(entity.id, entity)
    }

    override fun get(id: Int): RPKPaymentNotification? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            val result = database.create
                    .select(
                            RPKIT_PAYMENT_NOTIFICATION.GROUP_ID,
                            RPKIT_PAYMENT_NOTIFICATION.TO_ID,
                            RPKIT_PAYMENT_NOTIFICATION.CHARACTER_ID,
                            RPKIT_PAYMENT_NOTIFICATION.DATE,
                            RPKIT_PAYMENT_NOTIFICATION.TEXT
                    )
                    .from(RPKIT_PAYMENT_NOTIFICATION)
                    .where(RPKIT_PAYMENT_NOTIFICATION.ID.eq(id))
                    .fetchOne() ?: return null
            val paymentGroupProvider = plugin.core.serviceManager.getServiceProvider(RPKPaymentGroupProvider::class)
            val paymentGroupId = result.get(RPKIT_PAYMENT_NOTIFICATION.GROUP_ID)
            val paymentGroup = paymentGroupProvider.getPaymentGroup(paymentGroupId)
            val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
            val toId = result.get(RPKIT_PAYMENT_NOTIFICATION.TO_ID)
            val to = characterProvider.getCharacter(toId)
            val characterId = result.get(RPKIT_PAYMENT_NOTIFICATION.CHARACTER_ID)
            val character = characterProvider.getCharacter(characterId)
            if (paymentGroup != null && to != null && character != null) {
                val paymentNotification = RPKPaymentNotificationImpl(
                        id,
                        paymentGroup,
                        to,
                        character,
                        result.get(RPKIT_PAYMENT_NOTIFICATION.DATE).time,
                        result.get(RPKIT_PAYMENT_NOTIFICATION.TEXT)
                )
                cache.put(id, paymentNotification)
                return paymentNotification
            } else {
                database.create
                        .deleteFrom(RPKIT_PAYMENT_NOTIFICATION)
                        .where(RPKIT_PAYMENT_NOTIFICATION.ID.eq(id))
                        .execute()
                return null
            }
        }
    }

    fun getAll(): List<RPKPaymentNotification> {
        val results = database.create
                .select(RPKIT_PAYMENT_NOTIFICATION.ID)
                .from(RPKIT_PAYMENT_NOTIFICATION)
                .fetch()
        return results.map { result -> get(result.get(RPKIT_PAYMENT_NOTIFICATION.ID)) }
                .filterNotNull()
    }

    fun get(character: RPKCharacter): List<RPKPaymentNotification> {
        val results = database.create
                .select(RPKIT_PAYMENT_NOTIFICATION.ID)
                .from(RPKIT_PAYMENT_NOTIFICATION)
                .where(RPKIT_PAYMENT_NOTIFICATION.CHARACTER_ID.eq(character.id))
                .fetch()
        return results.map { result -> get(result.get(RPKIT_PAYMENT_NOTIFICATION.ID)) }
                .filterNotNull()
    }

    override fun delete(entity: RPKPaymentNotification) {
        database.create
                .deleteFrom(RPKIT_PAYMENT_NOTIFICATION)
                .where(RPKIT_PAYMENT_NOTIFICATION.ID.eq(entity.id))
                .execute()
        cache.remove(entity.id)
    }

}