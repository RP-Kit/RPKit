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

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterId
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.payments.bukkit.RPKPaymentsBukkit
import com.rpkit.payments.bukkit.database.create
import com.rpkit.payments.bukkit.database.jooq.Tables.RPKIT_PAYMENT_NOTIFICATION
import com.rpkit.payments.bukkit.group.RPKPaymentGroupId
import com.rpkit.payments.bukkit.group.RPKPaymentGroupService
import com.rpkit.payments.bukkit.notification.RPKPaymentNotification
import com.rpkit.payments.bukkit.notification.RPKPaymentNotificationId
import com.rpkit.payments.bukkit.notification.RPKPaymentNotificationImpl

/**
 * Represents payment notification table.
 */
class RPKPaymentNotificationTable(
        private val database: Database,
        plugin: RPKPaymentsBukkit
) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_payment_notification.id.enabled")) {
        database.cacheManager.createCache(
            "rpk-payments-bukkit.rpkit_payment_notification.id",
            Int::class.javaObjectType,
            RPKPaymentNotification::class.java,
            plugin.config.getLong("caching.rpkit_payment_notification.id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKPaymentNotification) {
        val groupId = entity.group.id ?: return
        val toId = entity.to.id ?: return
        val characterId = entity.character.id ?: return
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
                        groupId.value,
                        toId.value,
                        characterId.value,
                        entity.date,
                        entity.text
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = RPKPaymentNotificationId(id)
        cache?.set(id, entity)
    }

    fun update(entity: RPKPaymentNotification) {
        val groupId = entity.group.id ?: return
        val toId = entity.to.id ?: return
        val characterId = entity.character.id ?: return
        val id = entity.id ?: return
        database.create
                .update(RPKIT_PAYMENT_NOTIFICATION)
                .set(RPKIT_PAYMENT_NOTIFICATION.GROUP_ID, groupId.value)
                .set(RPKIT_PAYMENT_NOTIFICATION.TO_ID, toId.value)
                .set(RPKIT_PAYMENT_NOTIFICATION.CHARACTER_ID, characterId.value)
                .set(RPKIT_PAYMENT_NOTIFICATION.DATE, entity.date)
                .set(RPKIT_PAYMENT_NOTIFICATION.TEXT, entity.text)
                .where(RPKIT_PAYMENT_NOTIFICATION.ID.eq(id.value))
                .execute()
        cache?.set(id.value, entity)
    }

    operator fun get(id: RPKPaymentNotificationId): RPKPaymentNotification? {
        if (cache?.containsKey(id.value) == true) {
            return cache[id.value]
        }
        val result = database.create
            .select(
                RPKIT_PAYMENT_NOTIFICATION.GROUP_ID,
                RPKIT_PAYMENT_NOTIFICATION.TO_ID,
                RPKIT_PAYMENT_NOTIFICATION.CHARACTER_ID,
                RPKIT_PAYMENT_NOTIFICATION.DATE,
                RPKIT_PAYMENT_NOTIFICATION.TEXT
            )
            .from(RPKIT_PAYMENT_NOTIFICATION)
            .where(RPKIT_PAYMENT_NOTIFICATION.ID.eq(id.value))
            .fetchOne() ?: return null
        val paymentGroupService = Services[RPKPaymentGroupService::class.java] ?: return null
        val paymentGroupId = result.get(RPKIT_PAYMENT_NOTIFICATION.GROUP_ID)
        val paymentGroup = paymentGroupService.getPaymentGroup(RPKPaymentGroupId(paymentGroupId))
        val characterService = Services[RPKCharacterService::class.java] ?: return null
        val toId = result.get(RPKIT_PAYMENT_NOTIFICATION.TO_ID)
        val to = characterService.getCharacter(RPKCharacterId(toId))
        val characterId = result.get(RPKIT_PAYMENT_NOTIFICATION.CHARACTER_ID)
        val character = characterService.getCharacter(RPKCharacterId(characterId))
        if (paymentGroup != null && to != null && character != null) {
            val paymentNotification = RPKPaymentNotificationImpl(
                id,
                paymentGroup,
                to,
                character,
                result.get(RPKIT_PAYMENT_NOTIFICATION.DATE),
                result.get(RPKIT_PAYMENT_NOTIFICATION.TEXT)
            )
            cache?.set(id.value, paymentNotification)
            return paymentNotification
        } else {
            database.create
                .deleteFrom(RPKIT_PAYMENT_NOTIFICATION)
                .where(RPKIT_PAYMENT_NOTIFICATION.ID.eq(id.value))
                .execute()
            return null
        }
    }

    fun getAll(): List<RPKPaymentNotification> {
        val results = database.create
                .select(RPKIT_PAYMENT_NOTIFICATION.ID)
                .from(RPKIT_PAYMENT_NOTIFICATION)
                .fetch()
        return results.map { result -> get(RPKPaymentNotificationId(result.get(RPKIT_PAYMENT_NOTIFICATION.ID))) }
                .filterNotNull()
    }

    fun get(character: RPKCharacter): List<RPKPaymentNotification> {
        val characterId = character.id ?: return emptyList()
        val results = database.create
                .select(RPKIT_PAYMENT_NOTIFICATION.ID)
                .from(RPKIT_PAYMENT_NOTIFICATION)
                .where(RPKIT_PAYMENT_NOTIFICATION.CHARACTER_ID.eq(characterId.value))
                .fetch()
        return results.map { result -> get(RPKPaymentNotificationId(result.get(RPKIT_PAYMENT_NOTIFICATION.ID))) }
                .filterNotNull()
    }

    fun delete(entity: RPKPaymentNotification) {
        val id = entity.id ?: return
        database.create
                .deleteFrom(RPKIT_PAYMENT_NOTIFICATION)
                .where(RPKIT_PAYMENT_NOTIFICATION.ID.eq(id.value))
                .execute()
        cache?.remove(id.value)
    }

}