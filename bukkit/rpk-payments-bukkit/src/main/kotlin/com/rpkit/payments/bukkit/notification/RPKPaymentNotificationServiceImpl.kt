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

package com.rpkit.payments.bukkit.notification

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.payments.bukkit.RPKPaymentsBukkit
import com.rpkit.payments.bukkit.database.table.RPKPaymentNotificationTable
import com.rpkit.payments.bukkit.event.notification.RPKBukkitPaymentNotificationCreateEvent
import com.rpkit.payments.bukkit.event.notification.RPKBukkitPaymentNotificationDeleteEvent
import com.rpkit.payments.bukkit.event.notification.RPKBukkitPaymentNotificationUpdateEvent
import com.rpkit.payments.bukkit.group.RPKPaymentGroup
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture


class RPKPaymentNotificationServiceImpl(override val plugin: RPKPaymentsBukkit) : RPKPaymentNotificationService {

    override val notifications: CompletableFuture<List<RPKPaymentNotification>>
        get() = plugin.database.getTable(RPKPaymentNotificationTable::class.java).getAll()

    override fun getPaymentNotification(id: RPKPaymentNotificationId): CompletableFuture<RPKPaymentNotification?> {
        return plugin.database.getTable(RPKPaymentNotificationTable::class.java)[id]
    }

    override fun getPaymentNotificationsFor(character: RPKCharacter): CompletableFuture<List<RPKPaymentNotification>> {
        return plugin.database.getTable(RPKPaymentNotificationTable::class.java).get(character)
    }

    override fun addPaymentNotification(paymentNotification: RPKPaymentNotification) {
        val event = RPKBukkitPaymentNotificationCreateEvent(paymentNotification)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.database.getTable(RPKPaymentNotificationTable::class.java).insert(event.paymentNotification)
    }

    override fun createPaymentNotification(
        group: RPKPaymentGroup,
        to: RPKCharacter,
        character: RPKCharacter,
        date: LocalDateTime,
        text: String
    ): RPKPaymentNotification {
        val paymentNotification = RPKPaymentNotificationImpl(
            null,
            group,
            to,
            character,
            date,
            text
        )
        addPaymentNotification(paymentNotification)
        return paymentNotification
    }

    override fun removePaymentNotification(paymentNotification: RPKPaymentNotification) {
        val event = RPKBukkitPaymentNotificationDeleteEvent(paymentNotification)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.database.getTable(RPKPaymentNotificationTable::class.java).delete(event.paymentNotification)
    }

    override fun updatePaymentNotification(paymentNotification: RPKPaymentNotification) {
        val event = RPKBukkitPaymentNotificationUpdateEvent(paymentNotification)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.database.getTable(RPKPaymentNotificationTable::class.java).update(event.paymentNotification)
    }

}