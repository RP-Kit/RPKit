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
import com.seventh_root.elysium.core.service.ServiceProvider

/**
 * Provides payment notification related operations.
 */
interface ElysiumPaymentNotificationProvider: ServiceProvider {

    /**
     * A list of payment notifications managed by this payment notification provider.
     * This is immutable, payment groups must be added and removed using [addPaymentNotification] and [removePaymentNotification]
     */
    val notifications: List<ElysiumPaymentNotification>

    /**
     * Gets a payment notification by ID.
     *
     * @param id The ID of the payment notification
     * @return The payment notification, or null if no payment notification is found with the given ID
     */
    fun getPaymentNotification(id: Int): ElysiumPaymentNotification?

    /**
     * Gets all payment notifications to a character.
     *
     * @param character The character to get payment notifications for
     * @return A list of all payment notifications sent to the character
     */
    fun getPaymentNotificationsFor(character: ElysiumCharacter): List<ElysiumPaymentNotification>

    /**
     * Adds a payment notification to be tracked by this payment notification provider.
     *
     * @param paymentNotification The payment notification to add
     */
    fun addPaymentNotification(paymentNotification: ElysiumPaymentNotification)

    /**
     * Removes a payment notification from being tracked by this payment notification provider.
     *
     * @param paymentNotification The payment notification to remove
     */
    fun removePaymentNotification(paymentNotification: ElysiumPaymentNotification)

    /**
     * Updates a payment notification's state in data storage.
     *
     * @param paymentNotification The payment notification to update
     */
    fun updatePaymentNotification(paymentNotification: ElysiumPaymentNotification)

}