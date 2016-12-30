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

package com.rpkit.payments.bukkit.notification

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.payments.bukkit.RPKPaymentsBukkit
import com.rpkit.payments.bukkit.database.table.RPKPaymentNotificationTable


class RPKPaymentNotificationProviderImpl(private val plugin: RPKPaymentsBukkit): RPKPaymentNotificationProvider {

    override val notifications: List<RPKPaymentNotification>
        get() = plugin.core.database.getTable(RPKPaymentNotificationTable::class).getAll()

    override fun getPaymentNotification(id: Int): RPKPaymentNotification? {
        return plugin.core.database.getTable(RPKPaymentNotificationTable::class)[id]
    }

    override fun getPaymentNotificationsFor(character: RPKCharacter): List<RPKPaymentNotification> {
        return plugin.core.database.getTable(RPKPaymentNotificationTable::class).get(character)
    }

    override fun addPaymentNotification(paymentNotification: RPKPaymentNotification) {
        plugin.core.database.getTable(RPKPaymentNotificationTable::class).insert(paymentNotification)
    }

    override fun removePaymentNotification(paymentNotification: RPKPaymentNotification) {
        plugin.core.database.getTable(RPKPaymentNotificationTable::class).delete(paymentNotification)
    }

    override fun updatePaymentNotification(paymentNotification: RPKPaymentNotification) {
        plugin.core.database.getTable(RPKPaymentNotificationTable::class).update(paymentNotification)
    }

}