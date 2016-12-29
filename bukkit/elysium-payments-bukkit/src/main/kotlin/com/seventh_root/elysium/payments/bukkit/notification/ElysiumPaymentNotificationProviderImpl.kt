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

package com.seventh_root.elysium.payments.bukkit.notification

import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter
import com.seventh_root.elysium.payments.bukkit.ElysiumPaymentsBukkit
import com.seventh_root.elysium.payments.bukkit.database.table.ElysiumPaymentNotificationTable


class ElysiumPaymentNotificationProviderImpl(private val plugin: ElysiumPaymentsBukkit): ElysiumPaymentNotificationProvider {

    override val notifications: List<ElysiumPaymentNotification>
        get() = plugin.core.database.getTable(ElysiumPaymentNotificationTable::class).getAll()

    override fun getPaymentNotification(id: Int): ElysiumPaymentNotification? {
        return plugin.core.database.getTable(ElysiumPaymentNotificationTable::class)[id]
    }

    override fun getPaymentNotificationsFor(character: ElysiumCharacter): List<ElysiumPaymentNotification> {
        return plugin.core.database.getTable(ElysiumPaymentNotificationTable::class).get(character)
    }

    override fun addPaymentNotification(paymentNotification: ElysiumPaymentNotification) {
        plugin.core.database.getTable(ElysiumPaymentNotificationTable::class).insert(paymentNotification)
    }

    override fun removePaymentNotification(paymentNotification: ElysiumPaymentNotification) {
        plugin.core.database.getTable(ElysiumPaymentNotificationTable::class).delete(paymentNotification)
    }

    override fun updatePaymentNotification(paymentNotification: ElysiumPaymentNotification) {
        plugin.core.database.getTable(ElysiumPaymentNotificationTable::class).update(paymentNotification)
    }

}