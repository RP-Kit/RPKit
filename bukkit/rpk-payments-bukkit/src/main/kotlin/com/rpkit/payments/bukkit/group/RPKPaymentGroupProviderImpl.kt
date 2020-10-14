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

package com.rpkit.payments.bukkit.group

import com.rpkit.payments.bukkit.RPKPaymentsBukkit
import com.rpkit.payments.bukkit.database.table.RPKPaymentGroupTable
import com.rpkit.payments.bukkit.event.group.RPKBukkitPaymentGroupCreateEvent
import com.rpkit.payments.bukkit.event.group.RPKBukkitPaymentGroupDeleteEvent
import com.rpkit.payments.bukkit.event.group.RPKBukkitPaymentGroupUpdateEvent

/**
 * Payment group service implementation.
 */
class RPKPaymentGroupServiceImpl(override val plugin: RPKPaymentsBukkit) : RPKPaymentGroupService {

    override val paymentGroups: List<RPKPaymentGroup>
        get() = plugin.database.getTable(RPKPaymentGroupTable::class).getAll()

    override fun getPaymentGroup(id: Int): RPKPaymentGroup? {
        return plugin.database.getTable(RPKPaymentGroupTable::class)[id]
    }

    override fun getPaymentGroup(name: String): RPKPaymentGroup? {
        return plugin.database.getTable(RPKPaymentGroupTable::class).get(name)
    }

    override fun addPaymentGroup(paymentGroup: RPKPaymentGroup) {
        val event = RPKBukkitPaymentGroupCreateEvent(paymentGroup)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.database.getTable(RPKPaymentGroupTable::class).insert(event.paymentGroup)
    }

    override fun removePaymentGroup(paymentGroup: RPKPaymentGroup) {
        val event = RPKBukkitPaymentGroupDeleteEvent(paymentGroup)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.database.getTable(RPKPaymentGroupTable::class).delete(event.paymentGroup)
    }

    override fun updatePaymentGroup(paymentGroup: RPKPaymentGroup) {
        val event = RPKBukkitPaymentGroupUpdateEvent(paymentGroup)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.database.getTable(RPKPaymentGroupTable::class).update(event.paymentGroup)
    }

}
