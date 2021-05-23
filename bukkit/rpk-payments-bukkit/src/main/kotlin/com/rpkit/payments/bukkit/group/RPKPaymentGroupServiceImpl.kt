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

package com.rpkit.payments.bukkit.group

import com.rpkit.economy.bukkit.currency.RPKCurrency
import com.rpkit.payments.bukkit.RPKPaymentsBukkit
import com.rpkit.payments.bukkit.database.table.RPKPaymentGroupTable
import com.rpkit.payments.bukkit.event.group.RPKBukkitPaymentGroupCreateEvent
import com.rpkit.payments.bukkit.event.group.RPKBukkitPaymentGroupDeleteEvent
import com.rpkit.payments.bukkit.event.group.RPKBukkitPaymentGroupUpdateEvent
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

/**
 * Payment group service implementation.
 */
class RPKPaymentGroupServiceImpl(override val plugin: RPKPaymentsBukkit) : RPKPaymentGroupService {

    override val paymentGroups: CompletableFuture<List<RPKPaymentGroup>>
        get() = plugin.database.getTable(RPKPaymentGroupTable::class.java).getAll()

    override fun getPaymentGroup(id: RPKPaymentGroupId): CompletableFuture<RPKPaymentGroup?> {
        return plugin.database.getTable(RPKPaymentGroupTable::class.java)[id]
    }

    override fun getPaymentGroup(name: RPKPaymentGroupName): CompletableFuture<RPKPaymentGroup?> {
        return plugin.database.getTable(RPKPaymentGroupTable::class.java).get(name)
    }

    override fun addPaymentGroup(paymentGroup: RPKPaymentGroup): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val event = RPKBukkitPaymentGroupCreateEvent(paymentGroup, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            plugin.database.getTable(RPKPaymentGroupTable::class.java).insert(event.paymentGroup).join()
        }
    }

    override fun createPaymentGroup(
        name: RPKPaymentGroupName,
        amount: Int,
        currency: RPKCurrency,
        interval: Duration,
        lastPaymentTime: LocalDateTime,
        balance: Int
    ): CompletableFuture<RPKPaymentGroup> {
        val paymentGroup = RPKPaymentGroupImpl(
            plugin,
            null,
            name,
            amount,
            currency,
            interval,
            lastPaymentTime,
            balance
        )
        return addPaymentGroup(paymentGroup).thenApply { paymentGroup }
    }

    override fun removePaymentGroup(paymentGroup: RPKPaymentGroup): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val event = RPKBukkitPaymentGroupDeleteEvent(paymentGroup, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            plugin.database.getTable(RPKPaymentGroupTable::class.java).delete(event.paymentGroup).join()
        }
    }

    override fun updatePaymentGroup(paymentGroup: RPKPaymentGroup): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val event = RPKBukkitPaymentGroupUpdateEvent(paymentGroup, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            plugin.database.getTable(RPKPaymentGroupTable::class.java).update(event.paymentGroup).join()
        }
    }

}
