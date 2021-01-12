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

import com.rpkit.core.service.Service
import com.rpkit.economy.bukkit.currency.RPKCurrency
import java.time.Duration
import java.time.LocalDateTime

/**
 * Provides payment group related operations.
 */
interface RPKPaymentGroupService : Service {

    /**
     * A list of payment groups currently managed by this payment group service.
     * This is immutable, payment groups must be added and removed using [addPaymentGroup] and [removePaymentGroup].
     */
    val paymentGroups: List<RPKPaymentGroup>

    /**
     * Gets a payment group by ID.
     *
     * @param id The ID of the payment group
     * @return The payment group, or null if no payment group is found with the given ID
     */
    fun getPaymentGroup(id: RPKPaymentGroupId): RPKPaymentGroup?

    /**
     * Gets a payment group by name.
     *
     * @param name The name of the payment group
     * @return The payment group, or null if no payment group is found with the given name
     */
    fun getPaymentGroup(name: RPKPaymentGroupName): RPKPaymentGroup?

    /**
     * Adds a payment group to be tracked by this payment group service.
     *
     * @param paymentGroup The payment group to add
     */
    fun addPaymentGroup(paymentGroup: RPKPaymentGroup)

    fun createPaymentGroup(
        name: RPKPaymentGroupName,
        amount: Int,
        currency: RPKCurrency,
        interval: Duration,
        lastPaymentTime: LocalDateTime,
        balance: Int
    ): RPKPaymentGroup

    /**
     * Removes a payment group from being tracked by this payment group service.
     *
     * @param paymentGroup The payment group to remove
     */
    fun removePaymentGroup(paymentGroup: RPKPaymentGroup)

    /**
     * Updates a payment group's state in data storage.
     *
     * @param paymentGroup The payment group to update.
     */
    fun updatePaymentGroup(paymentGroup: RPKPaymentGroup)

}