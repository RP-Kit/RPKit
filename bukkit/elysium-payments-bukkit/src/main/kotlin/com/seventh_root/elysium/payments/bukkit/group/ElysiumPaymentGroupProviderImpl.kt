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

package com.seventh_root.elysium.payments.bukkit.group

import com.seventh_root.elysium.payments.bukkit.ElysiumPaymentsBukkit
import com.seventh_root.elysium.payments.bukkit.database.table.ElysiumPaymentGroupTable

/**
 * Payment group provider implementation.
 */
class ElysiumPaymentGroupProviderImpl(private val plugin: ElysiumPaymentsBukkit): ElysiumPaymentGroupProvider {

    override val paymentGroups: List<ElysiumPaymentGroup>
        get() = plugin.core.database.getTable(ElysiumPaymentGroupTable::class).getAll()

    override fun getPaymentGroup(id: Int): ElysiumPaymentGroup? {
        return plugin.core.database.getTable(ElysiumPaymentGroupTable::class)[id]
    }

    override fun getPaymentGroup(name: String): ElysiumPaymentGroup? {
        return plugin.core.database.getTable(ElysiumPaymentGroupTable::class).get(name)
    }

    override fun addPaymentGroup(paymentGroup: ElysiumPaymentGroup) {
        plugin.core.database.getTable(ElysiumPaymentGroupTable::class).insert(paymentGroup)
    }

    override fun removePaymentGroup(paymentGroup: ElysiumPaymentGroup) {
        plugin.core.database.getTable(ElysiumPaymentGroupTable::class).delete(paymentGroup)
    }

    override fun updatePaymentGroup(paymentGroup: ElysiumPaymentGroup) {
        plugin.core.database.getTable(ElysiumPaymentGroupTable::class).update(paymentGroup)
    }

}
