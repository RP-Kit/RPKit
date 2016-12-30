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

package com.rpkit.payments.bukkit.group

import com.rpkit.payments.bukkit.RPKPaymentsBukkit
import com.rpkit.payments.bukkit.database.table.RPKPaymentGroupTable

/**
 * Payment group provider implementation.
 */
class RPKPaymentGroupProviderImpl(private val plugin: RPKPaymentsBukkit): RPKPaymentGroupProvider {

    override val paymentGroups: List<RPKPaymentGroup>
        get() = plugin.core.database.getTable(RPKPaymentGroupTable::class).getAll()

    override fun getPaymentGroup(id: Int): RPKPaymentGroup? {
        return plugin.core.database.getTable(RPKPaymentGroupTable::class)[id]
    }

    override fun getPaymentGroup(name: String): RPKPaymentGroup? {
        return plugin.core.database.getTable(RPKPaymentGroupTable::class).get(name)
    }

    override fun addPaymentGroup(paymentGroup: RPKPaymentGroup) {
        plugin.core.database.getTable(RPKPaymentGroupTable::class).insert(paymentGroup)
    }

    override fun removePaymentGroup(paymentGroup: RPKPaymentGroup) {
        plugin.core.database.getTable(RPKPaymentGroupTable::class).delete(paymentGroup)
    }

    override fun updatePaymentGroup(paymentGroup: RPKPaymentGroup) {
        plugin.core.database.getTable(RPKPaymentGroupTable::class).update(paymentGroup)
    }

}
